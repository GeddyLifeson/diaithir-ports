package com.github.alexmodguy.alexscaves.server.item;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.server.block.ACBlockRegistry;
import com.github.alexmodguy.alexscaves.server.block.blockentity.BeholderBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class OccultGemItem extends Item {

    public static final TicketController TICKET_CONTROLLER = new TicketController(
            Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "occult_gem"));

    public OccultGemItem() {
        super(com.github.alexmodguy.alexscaves.server.misc.ACProps.item());
    }

    public static boolean isActive(ItemStack itemStack) {
        CompoundTag compoundtag = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return !compoundtag.isEmpty() && (compoundtag.contains("BeholderDimension") || compoundtag.contains("BeholderPos"));
    }

    private static Optional<ResourceKey<Level>> getBeholderDimension(CompoundTag tag) {
        return Level.RESOURCE_KEY_CODEC.parse(NbtOps.INSTANCE, tag.get("BeholderDimension")).result();
    }

    @Nullable
    public static GlobalPos getBeholderPosition(CompoundTag tag) {
        boolean flag = tag.contains("BeholderPos");
        boolean flag1 = tag.contains("BeholderDimension");
        if (flag && flag1) {
            Optional<ResourceKey<Level>> optional = getBeholderDimension(tag);
            if (optional.isPresent()) {
                Optional<BlockPos> blockposOpt = tag.read("BeholderPos", BlockPos.CODEC);
                if (blockposOpt.isPresent()) {
                    return GlobalPos.of(optional.get(), blockposOpt.get());
                }
            }
        }

        return null;
    }

    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (isActive(itemstack)) {
            CompoundTag tag = itemstack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            GlobalPos globalPos = getBeholderPosition(tag);
            if (globalPos != null && globalPos.dimension() != null && globalPos.dimension().equals(level.dimension()) && !level.isClientSide() && level instanceof ServerLevel serverLevel) {
                ServerLevel dimensionLevel = serverLevel.getServer().getLevel(globalPos.dimension());
                if (dimensionLevel != null) {
                    loadChunksAround(dimensionLevel, player.getUUID(), globalPos.pos(), true);
                    BlockState blockState = dimensionLevel.getBlockState(globalPos.pos());
                    if (blockState.is(ACBlockRegistry.BEHOLDER.get())) {
                        if(dimensionLevel.getBlockEntity(globalPos.pos()) instanceof BeholderBlockEntity blockEntity){
                            blockEntity.startObserving(dimensionLevel, player);
                        }
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }


    private static void loadChunksAround(ServerLevel serverLevel, UUID ticket, BlockPos center, boolean load) {
        ChunkPos chunkPos = ChunkPos.containing(center);
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                TICKET_CONTROLLER.forceChunk(serverLevel, center, chunkPos.x() + i, chunkPos.z() + j, load, true);
            }
        }
    }


    public void inventoryTick(ItemStack itemStack, net.minecraft.server.level.ServerLevel level, Entity entity, @javax.annotation.Nullable net.minecraft.world.entity.EquipmentSlot slotArg) {
        if (!level.isClientSide()) {
            if (isActive(itemStack)) {
                CompoundTag compoundtag = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                if (compoundtag.contains("BeholderTracked") && !compoundtag.getBooleanOr("BeholderTracked", false)) {
                    return;
                }
                Optional<ResourceKey<Level>> optional = getBeholderDimension(compoundtag);
                if (optional.isPresent() && optional.get() == level.dimension() && compoundtag.contains("BeholderPos")) {
                    Optional<BlockPos> blockposOpt = compoundtag.read("BeholderPos", BlockPos.CODEC);
                    if (blockposOpt.isPresent()) {
                        BlockPos blockpos = blockposOpt.get();
                        boolean flag = false;
                        if ((entity.tickCount + entity.getId()) % 20 == 0) {
                            if (level.isLoaded(blockpos) && !level.getBlockState(blockpos).is(ACBlockRegistry.BEHOLDER.get())) {
                                flag = true;
                            }
                        }
                        if (!level.isInWorldBounds(blockpos) || flag) {
                            compoundtag.remove("BeholderPos");
                            compoundtag.remove("BeholderDimension");
                            itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(compoundtag));
                        }
                    }
                }
            }
        }
    }

    public InteractionResult useOn(UseOnContext context) {
        BlockPos blockpos = context.getClickedPos();
        Level level = context.getLevel();
        if (!level.getBlockState(blockpos).is(ACBlockRegistry.BEHOLDER.get())) {
            return super.useOn(context);
        } else {
            level.playSound((Player) null, blockpos, SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS, 1.0F, 1.0F);
            Player player = context.getPlayer();
            ItemStack itemstack = context.getItemInHand();
            boolean flag = !player.getAbilities().instabuild && itemstack.getCount() == 1;
            if (flag) {
                CompoundTag existingTag = itemstack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                this.addBeholderTags(level.dimension(), blockpos, existingTag);
                itemstack.set(DataComponents.CUSTOM_DATA, CustomData.of(existingTag));
            } else {
                ItemStack itemstack1 = new ItemStack(ACItemRegistry.OCCULT_GEM.get(), 1);
                CompoundTag compoundtag = itemstack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                this.addBeholderTags(level.dimension(), blockpos, compoundtag);
                itemstack1.set(DataComponents.CUSTOM_DATA, CustomData.of(compoundtag));
                if (!player.getAbilities().instabuild) {
                    itemstack.shrink(1);
                }

                if (!player.getInventory().add(itemstack1)) {
                    player.drop(itemstack1, false);
                }
            }

            return InteractionResult.SUCCESS;
        }
    }

    private void addBeholderTags(ResourceKey<Level> levelResourceKey, BlockPos blockPos, CompoundTag tag) {
        tag.store("BeholderPos", BlockPos.CODEC, blockPos);
        Level.RESOURCE_KEY_CODEC.encodeStart(NbtOps.INSTANCE, levelResourceKey).result().ifPresent((p_40731_) -> {
            tag.put("BeholderDimension", p_40731_);
        });
        tag.putBoolean("BeholderTracked", true);
    }

    public boolean isFoil(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.isEmpty() && tag.contains("BeholderPos")) {
            Optional<ResourceKey<Level>> optional = getBeholderDimension(tag);
            Optional<BlockPos> blockposOpt = tag.read("BeholderPos", BlockPos.CODEC);
            return optional.isPresent() && blockposOpt.isPresent();
        }
        return super.isFoil(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> tooltip, TooltipFlag flagIn) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.isEmpty() && tag.contains("BeholderPos")) {
            Optional<ResourceKey<Level>> optional = getBeholderDimension(tag);
            Optional<BlockPos> blockposOpt = tag.read("BeholderPos", BlockPos.CODEC);
            if (optional.isPresent() && blockposOpt.isPresent()) {
                BlockPos blockpos = blockposOpt.get();
                Component untranslated = Component.translatable("item.alexscaves.occult_gem.desc", blockpos.getX(), blockpos.getY(), blockpos.getZ()).withStyle(ChatFormatting.GRAY);
                tooltip.accept(untranslated);
            }
        }
        super.appendHoverText(stack, context, display, tooltip, flagIn);
    }
}
