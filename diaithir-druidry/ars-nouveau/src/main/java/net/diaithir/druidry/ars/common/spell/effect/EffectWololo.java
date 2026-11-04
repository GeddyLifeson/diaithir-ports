package net.diaithir.druidry.ars.common.spell.effect;

import net.diaithir.druidry.ars.api.ANFakePlayer;
import net.diaithir.druidry.ars.api.item.inv.InteractType;
import net.diaithir.druidry.ars.api.item.inv.InventoryManager;
import net.diaithir.druidry.ars.api.item.inv.SlotReference;
import net.diaithir.druidry.ars.api.spell.*;
import net.diaithir.druidry.ars.api.spell.wrapped_caster.TileCaster;
import net.diaithir.druidry.ars.api.util.IWololoable;
import net.diaithir.druidry.ars.client.particle.ParticleColor;
import net.diaithir.druidry.ars.common.crafting.recipes.IDyeable;
import net.diaithir.druidry.ars.common.entity.debug.FixedStack;
import net.diaithir.druidry.ars.common.spell.augment.AugmentRandomize;
import net.diaithir.druidry.ars.common.spell.augment.AugmentSensitive;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import net.minecraft.world.item.crafting.CraftingInput;

public class EffectWololo extends AbstractEffect {
    public static EffectWololo INSTANCE = new EffectWololo();

    public EffectWololo() {
        super("wololo", "Wololo");
    }

    public static int MAX_RECIPE_CACHE = 16;
    public static FixedStack<CraftingRecipe> recipeCache = new FixedStack<>(MAX_RECIPE_CACHE);

    @Override
    public void onResolveEntity(EntityHitResult rayTraceResult, Level world, @NotNull LivingEntity shooter, SpellStats spellStats, SpellContext spellContext, SpellResolver resolver) {

        Player player = ANFakePlayer.getPlayer((ServerLevel) world);

        ItemStack dyeStack = getDye(shooter, spellStats, spellContext, player);
        if (dyeStack.isEmpty()) return;
        net.minecraft.world.item.DyeColor dyeColor = dyeStack.get(DataComponents.DYE);
        if (dyeColor == null) return;

        if (rayTraceResult.getEntity() instanceof ItemEntity itemEntity) {
            if (itemEntity.getItem().getItem() instanceof IDyeable iDyeable)
                iDyeable.onDye(itemEntity.getItem(), dyeColor);
            else if (itemEntity.getItem().getItem() instanceof BlockItem blockItem) {
                ItemStack result = getDyedResult((ServerLevel) world, makeContainer(dyeStack, blockItem));
                result.setCount(itemEntity.getItem().getCount());
                if (!result.isEmpty() && result.getItem() instanceof BlockItem) {
                    itemEntity.setItem(result);
                }
            }
        } else if (rayTraceResult.getEntity() instanceof LivingEntity living) {
            if (living instanceof Sheep sheep)
                sheep.setColor(dyeColor);
            else if (spellStats.isSensitive() || living instanceof ArmorStand) {
                for (ItemStack armorStack : net.diaithir.druidry.ars.common.util.PortUtil.armorSlots(living)) {
                    if (!armorStack.isEmpty()) {
                        var dyeComponent = armorStack.get(DataComponents.DYED_COLOR);
                        if (dyeComponent != null) {
                            armorStack.set(DataComponents.DYED_COLOR, new DyedItemColor(dyeColor.getTextureDiffuseColor()));
                        } else if (armorStack.getItem() instanceof IDyeable iDyeable) {
                            iDyeable.onDye(armorStack, dyeColor);
                        }
                    }
                }
            } else if (living instanceof Mob mob) {
                player.setItemInHand(InteractionHand.MAIN_HAND, dyeStack);
                mob.interact(player, InteractionHand.MAIN_HAND, mob.position());
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            }
        }
        world.playSound(null, rayTraceResult.getEntity().getX(), rayTraceResult.getEntity().getY(), rayTraceResult.getEntity().getZ(), SoundEvents.EVOKER_PREPARE_WOLOLO, SoundSource.PLAYERS, spellContext.getSpell().sound().getVolume(), spellContext.getSpell().sound().getPitch());
    }

    @NotNull
    private ItemStack getDye(@NotNull LivingEntity shooter, SpellStats spellStats, SpellContext spellContext, Player player) {

        if (spellContext.getCaster() instanceof TileCaster) {
            InventoryManager manager = spellContext.getCaster().getInvManager();
            SlotReference reference = manager.findItem(i -> i.getItem() instanceof DyeItem, InteractType.EXTRACT);
            if (!reference.isEmpty()) {
                return reference.getHandler().getStackInSlot(reference.getSlot());
            }
        } else if (isRealPlayer(shooter)) {
            ItemStack stack = player.getOffhandItem();
            if (stack.getItem() instanceof DyeItem) {
                return stack;
            }
        }

        Item dye = spellStats.isRandomized() ? getRandomDye(shooter.getRandom()) : getDyeItemFromSpell(spellContext);

        return dye.getDefaultInstance();
    }

    @Override
    public void onResolveBlock(BlockHitResult rayTraceResult, Level world, @NotNull LivingEntity shooter, SpellStats spellStats, SpellContext spellContext, SpellResolver resolver) {
        BlockPos blockPos = rayTraceResult.getBlockPos();
        BlockEntity blockEntity = world.getBlockEntity(blockPos);
        if (blockEntity instanceof IWololoable tileToDye) {
            ParticleColor color = spellStats.isRandomized() ? ParticleColor.makeRandomColor(255, 255, 255, shooter.getRandom()) : spellContext.getSpell().color();
            tileToDye.setColor(color);
        } else {
            ItemStack dyeStack = getDye(shooter, spellStats, spellContext, ANFakePlayer.getPlayer((ServerLevel) world));
            if (dyeStack.isEmpty()) return;
            if (blockEntity instanceof SignBlockEntity sign) {
                var player = getPlayer(shooter, (ServerLevel) world);
                ((DyeItem) dyeStack.getItem()).tryApplyToSign(world, sign, sign.isFacingFrontText(player), dyeStack, player);
            } else {
                // Try block + dye
                Block hitBlock = world.getBlockState(blockPos).getBlock();
                if (hitBlock == Blocks.AIR) return;
                ItemStack result = getDyedResult((ServerLevel) world, makeContainer(dyeStack, hitBlock));
                BlockItem blockItem;
                if (result.isEmpty() || !(result.getItem() instanceof BlockItem)) {
                    // Try blocks surrounding the dye
                    result = getDyedResult((ServerLevel) world, makeContainer8(dyeStack, hitBlock));
                    if (result.isEmpty() || !(result.getItem() instanceof BlockItem)) return;
                }
                blockItem = (BlockItem) result.getItem();
                world.setBlockAndUpdate(blockPos, blockItem.getBlock().defaultBlockState());
            }
        }
    }

    @NotNull
    private ItemStack getDyedResult(ServerLevel world, CraftingInput input) {
        Optional<CraftingRecipe> recipe = recipeCache.stream().filter(craftingRecipe -> craftingRecipe.matches(input, world)).findFirst();
        if (recipe.isPresent()) {
            recipeCache.add(recipe.get());
            return recipe.get().assemble(input);
        }
        var found = net.diaithir.druidry.ars.common.util.PortUtil.recipeManager().getRecipeFor(RecipeType.CRAFTING, input, world);
        found.ifPresent(holder -> recipeCache.add(holder.value()));
        return found.map(holder -> holder.value().assemble(input)).orElse(ItemStack.EMPTY);
    }

    private static CraftingInput makeContainer(ItemStack dye, ItemLike blockToDye) {
        return CraftingInput.of(2, 1, List.of(dye.copyWithCount(1), new ItemStack(blockToDye)));
    }

    private static CraftingInput makeContainer8(ItemStack dye, Block blockToDye) {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            items.add(i == 4 ? dye.copyWithCount(1) : new ItemStack(blockToDye));
        }
        return CraftingInput.of(3, 3, items);
    }

    private Item getRandomDye(RandomSource random) {
        return (Item) vanillaColors.values().toArray()[random.nextInt(vanillaColors.size())];
    }

    private static Item getDyeItemFromSpell(SpellContext spellContext) {
        ParticleColor spellColor = spellContext.getSpell().color();

        ParticleColor targetColor = vanillaColors.keySet().stream().min(Comparator.comparingDouble(d -> d.euclideanDistance(spellColor))).orElse(ParticleColor.WHITE);
        return vanillaColors.get(targetColor);
    }

    @Override
    protected int getDefaultManaCost() {
        return 30;
    }

    /**
     * Returns the set of augments that this spell part can be enhanced by.
     * Mods should use {@link AbstractSpellPart#compatibleAugments} for addon-supported augments.
     *
     * @see AbstractSpellPart#augmentSetOf(AbstractAugment...) for easy syntax to make the Set.
     * This should not be accessed directly, but can be overridden.
     */
    @Override
    protected @NotNull Set<AbstractAugment> getCompatibleAugments() {
        return augmentSetOf(AugmentRandomize.INSTANCE, AugmentSensitive.INSTANCE);
    }

    public static Map<ParticleColor, Item> vanillaColors = new HashMap<>();

    static {
        vanillaColors.put(ParticleColor.WHITE, Items.WHITE_DYE);
        vanillaColors.put(ParticleColor.RED, Items.RED_DYE);
        vanillaColors.put(ParticleColor.GREEN, Items.GREEN_DYE);
        vanillaColors.put(ParticleColor.BLUE, Items.BLUE_DYE);
        vanillaColors.put(ParticleColor.YELLOW, Items.YELLOW_DYE);
        vanillaColors.put(ParticleColor.PURPLE, Items.PURPLE_DYE);
        vanillaColors.put(ParticleColor.CYAN, Items.CYAN_DYE);
        vanillaColors.put(ParticleColor.ORANGE, Items.ORANGE_DYE);
        vanillaColors.put(ParticleColor.MAGENTA, Items.MAGENTA_DYE);
        vanillaColors.put(ParticleColor.LIGHT_BLUE, Items.LIGHT_BLUE_DYE);
        vanillaColors.put(ParticleColor.LIME, Items.LIME_DYE);
        vanillaColors.put(ParticleColor.PINK, Items.PINK_DYE);
        vanillaColors.put(ParticleColor.GRAY, Items.GRAY_DYE);
        vanillaColors.put(ParticleColor.LIGHT_GRAY, Items.LIGHT_GRAY_DYE);
        vanillaColors.put(ParticleColor.BROWN, Items.BROWN_DYE);
        vanillaColors.put(ParticleColor.BLACK, Items.BLACK_DYE);
    }

    @Override
    public String getBookDescription() {
        return "Changes the color of an entity or compatible block to the color of the spell.";
    }
}
