package dev.shadowsoffire.apothic_enchanting.objects;

import java.util.function.Consumer;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import dev.shadowsoffire.apothic_enchanting.ApothicEnchanting;
import dev.shadowsoffire.apothic_enchanting.Ench;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Spawner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.common.Tags;

public class EnderLeadItem extends Item {

    private static final Logger LOGGER = LogUtils.getLogger();

    protected final Type type;

    public EnderLeadItem(Item.Properties properties, Type type) {
        super(properties);
        this.type = type;
    }

    public boolean canLeash(Entity target) {
        return target instanceof Mob
            && target.isAlive()
            && !target.is(Tags.EntityTypes.BOSSES)
            && !target.is(Tags.EntityTypes.CAPTURING_NOT_SUPPORTED)
            && !target.isRemoved()
            && !target.isPassenger()
            && target.getType().canSerialize()
            && this.type.test(target);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand usedHand) {
        if (canLeash(target) && !containsEntity(stack)) {
            if (!player.level().isClientSide()) {
                try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(LOGGER)) {
                    TagValueOutput output = TagValueOutput.createWithContext(reporter, player.level().registryAccess());
                    if (target.saveAsPassenger(output)) {
                        CompoundTag tag = output.buildResult();
                        stack.set(DataComponents.ENTITY_DATA, TypedEntityData.of(target.getType(), tag));
                        stack.set(Ench.Components.LEASHED_ENTITY_TYPE, target.getType());
                        stack.set(Ench.Components.LEASHED_ENTITY_NAME, target.getDisplayName());
                        target.discard();
                    }
                }
            }
            playSound(player);
            return InteractionResult.SUCCESS;
        }
        return super.interactLivingEntity(stack, player, target, usedHand);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult useOn(UseOnContext ctx) {
        ItemStack stack = ctx.getItemInHand();
        if (!containsEntity(stack)) {
            return InteractionResult.PASS;
        }

        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        Direction side = ctx.getClickedFace();
        BlockState state = level.getBlockState(pos);
        Player player = ctx.getPlayer();
        InteractionHand hand = ctx.getHand();

        playSound(player);

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }
        else {
            if (this.type == Type.REINFORCED && level.getBlockEntity(pos) instanceof Spawner spawner) {
                EntityType<?> type = getType(stack);
                if (type.builtInRegistryHolder().is(Ench.Tags.BLACKLISTED_FROM_SPAWNERS)) {
                    return InteractionResult.FAIL;
                }
                spawner.setEntityId(type, level.getRandom());
                level.sendBlockUpdated(pos, state, state, 3);
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                stack.hurtAndBreak(1, player, hand);
                stack.remove(Ench.Components.LEASHED_ENTITY_TYPE);
                stack.remove(Ench.Components.LEASHED_ENTITY_NAME);
                stack.remove(DataComponents.ENTITY_DATA);
                return InteractionResult.CONSUME;
            }
            else {
                BlockPos spawnPos;
                if (state.getCollisionShape(level, pos).isEmpty()) {
                    spawnPos = pos;
                }
                else {
                    spawnPos = pos.relative(side);
                }

                EntityType<?> type = getType(stack);
                TypedEntityData<EntityType<?>> data = stack.get(DataComponents.ENTITY_DATA);
                if (data == null) {
                    return InteractionResult.FAIL;
                }

                CompoundTag tag = data.copyTagWithoutId().copy();
                tag.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(type).toString());

                try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(LOGGER)) {
                    ValueInput input = TagValueInput.create(reporter, serverLevel.registryAccess(), tag);
                    Entity entity = EntityType.loadEntityRecursive(input, level, EntitySpawnReason.DISPENSER, e -> e);
                    if (entity != null) {
                        entity.snapTo((double) spawnPos.getX() + 0.5D, (double) spawnPos.getY(), (double) spawnPos.getZ() + 0.5D, 0.0F, 0.0F);
                        level.addFreshEntity(entity);
                    }
                }

                level.gameEvent(player, GameEvent.ENTITY_PLACE, spawnPos);
                stack.hurtAndBreak(1, player, hand);
                stack.remove(Ench.Components.LEASHED_ENTITY_TYPE);
                stack.remove(Ench.Components.LEASHED_ENTITY_NAME);
                stack.remove(DataComponents.ENTITY_DATA);

                return InteractionResult.CONSUME;
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        EntityType<?> type = getType(stack);
        if (type != null) {
            Component name = type.getDescription();
            tooltip.accept(ApothicEnchanting.lang("info", "leashed_entity", name).withStyle(ChatFormatting.GRAY));
        }
        else {
            tooltip.accept(ApothicEnchanting.lang("info", "leash_no_entity").withStyle(ChatFormatting.GRAY));
        }
        tooltip.accept(Component.translatable(this.getDescriptionId() + ".desc").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public Component getName(ItemStack stack) {
        EntityType<?> type = getType(stack);
        if (type != null) {
            Component name = stack.getOrDefault(Ench.Components.LEASHED_ENTITY_NAME, type.getDescription());
            MobCategory cat = type.getCategory();
            ChatFormatting color = switch (cat) {
                case AMBIENT, CREATURE -> ChatFormatting.DARK_GREEN;
                case MONSTER -> ChatFormatting.RED;
                case WATER_AMBIENT, UNDERGROUND_WATER_CREATURE, WATER_CREATURE, AXOLOTLS -> ChatFormatting.BLUE;
                default -> ChatFormatting.WHITE;
            };
            return ApothicEnchanting.lang("info", "leashed_entity_title", super.getName(stack), Component.translatable("%s", name).withStyle(color));
        }
        return super.getName(stack);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return containsEntity(stack);
    }

    private static void playSound(Player player) {
        player.level().playSound(player, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.AMBIENT, 1, 1);
    }

    @Nullable
    private static EntityType<?> getType(ItemStack stack) {
        return stack.get(Ench.Components.LEASHED_ENTITY_TYPE);
    }

    private static boolean containsEntity(ItemStack stack) {
        return getType(stack) != null;
    }

    public static enum Type implements Predicate<Entity> {
        FLIMSY(e -> e instanceof Animal || e instanceof AmbientCreature || e instanceof WaterAnimal),
        NORMAL(Mob.class::isInstance),
        REINFORCED(Mob.class::isInstance);

        private Predicate<Entity> predicate;

        private Type(Predicate<Entity> predicate) {
            this.predicate = predicate;
        }

        @Override
        public boolean test(Entity input) {
            return this.predicate.test(input);
        }
    }

}
