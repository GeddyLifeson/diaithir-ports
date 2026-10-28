package com.gregtechceu.gtceu.common.item.behavior;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.blockentity.IPaintable;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IDurabilityBar;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.pipenet.IPipeNode;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.BreadthFirstBlockSearch;
import com.gregtechceu.gtceu.utils.GradientUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ARGB;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.util.TriPredicate;




import com.google.common.collect.ImmutableMap;



import it.unimi.dsi.fastutil.ints.IntIntPair;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class ColorSprayBehaviour implements IDurabilityBar, IInteractionItem, IAddInformation {

    // vanilla
    private static final ImmutableMap<DyeColor, Block> GLASS_MAP;
    private static final ImmutableMap<DyeColor, Block> GLASS_PANE_MAP;
    private static final ImmutableMap<DyeColor, Block> TERRACOTTA_MAP;
    private static final ImmutableMap<DyeColor, Block> WOOL_MAP;
    private static final ImmutableMap<DyeColor, Block> CARPET_MAP;
    private static final ImmutableMap<DyeColor, Block> CONCRETE_MAP;
    private static final ImmutableMap<DyeColor, Block> CONCRETE_POWDER_MAP;
    private static final ImmutableMap<DyeColor, Block> SHULKER_BOX_MAP;
    private static final ImmutableMap<DyeColor, Block> CANDLE_MAP;

    private static Block getBlock(DyeColor color, String postfix) {
        Identifier id = Identifier.withDefaultNamespace(color.getSerializedName() + "_" + postfix);
        return BuiltInRegistries.BLOCK.getValue(id);
    }

    static {
        ImmutableMap.Builder<DyeColor, Block> glassBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<DyeColor, Block> glassPaneBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<DyeColor, Block> terracottaBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<DyeColor, Block> woolBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<DyeColor, Block> carpetBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<DyeColor, Block> concreteBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<DyeColor, Block> concretePowderBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<DyeColor, Block> shulkerBoxBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<DyeColor, Block> candleBuilder = ImmutableMap.builder();

        for (DyeColor color : DyeColor.values()) {
            glassBuilder.put(color, getBlock(color, "stained_glass"));
            glassPaneBuilder.put(color, getBlock(color, "stained_glass_pane"));
            terracottaBuilder.put(color, getBlock(color, "terracotta"));
            woolBuilder.put(color, getBlock(color, "wool"));
            carpetBuilder.put(color, getBlock(color, "carpet"));
            concreteBuilder.put(color, getBlock(color, "concrete"));
            concretePowderBuilder.put(color, getBlock(color, "concrete_powder"));
            shulkerBoxBuilder.put(color, getBlock(color, "shulker_box"));
            candleBuilder.put(color, getBlock(color, "candle"));
        }
        GLASS_MAP = glassBuilder.build();
        GLASS_PANE_MAP = glassPaneBuilder.build();
        TERRACOTTA_MAP = terracottaBuilder.build();
        WOOL_MAP = woolBuilder.build();
        CARPET_MAP = carpetBuilder.build();
        CONCRETE_MAP = concreteBuilder.build();
        CONCRETE_POWDER_MAP = concretePowderBuilder.build();
        SHULKER_BOX_MAP = shulkerBoxBuilder.build();
        CANDLE_MAP = candleBuilder.build();
    }

    private final Supplier<ItemStack> empty;
    private final DyeColor color;
    public final int totalUses;
    private final IntIntPair durabilityBarColors;

    public ColorSprayBehaviour(Supplier<ItemStack> empty, int totalUses, int color) {
        this.empty = empty;
        DyeColor[] colors = DyeColor.values();
        this.color = color >= colors.length || color < 0 ? null : colors[color];
        // default to a gray color if this.color is null (like for solvent spray)
        int colorValue = this.color == null ? 0x969696 : this.color.getTextColor();
        this.totalUses = totalUses;
        this.durabilityBarColors = GradientUtil.getGradient(colorValue, 10);
    }

    @Override
    public float getDurabilityForDisplay(ItemStack stack) {
        return (float) getUsesLeft(stack) / totalUses;
    }

    @Override
    public int getMaxDurability(ItemStack stack) {
        return totalUses;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float f = Math.max(0.0F, getDurabilityForDisplay(stack));
        return mixColors(f, durabilityBarColors.leftInt(), durabilityBarColors.rightInt());
    }

    @Nullable
    @Override
    public IntIntPair getDurabilityColorsForDisplay(ItemStack itemStack) {
        return durabilityBarColors;
    }

    private static int mixColors(float ratio, int... colors) {
        int r = 0, g = 0, b = 0;
        ratio = ratio * (1.0f / colors.length);
        for (int color : colors) {
            r += ARGB.red(color) * ratio;
            g += ARGB.green(color) * ratio;
            b += ARGB.blue(color) * ratio;
        }
        return ARGB.color(255, r, g, b);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        int remainingUses = getUsesLeft(stack);
        if (color != null) {
            tooltipComponents
                    .add(Component.translatable("behaviour.paintspray." + this.color.getSerializedName() + ".tooltip"));
        } else {
            tooltipComponents.add(Component.translatable("behaviour.paintspray.solvent.tooltip"));
        }
        tooltipComponents.add(Component.translatable("behaviour.paintspray.uses", remainingUses));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        if (player == null) {
            return InteractionResult.PASS;
        }

        int maxBlocksToRecolor = player.isShiftKeyDown() ? ConfigHolder.INSTANCE.tools.sprayCanChainLength : 1;

        BlockEntity first = level.getBlockEntity(pos);
        if (first == null || !handleSpecialBlockEntities(first, maxBlocksToRecolor, context)) {
            handleBlocks(pos, maxBlocksToRecolor, context);
        }
        GTSoundEntries.SPRAY_CAN_TOOL.play(level, null, player.position(), 1.0f, 1.0f);
        return InteractionResult.SUCCESS;
    }

    private static boolean paintPaintable(IPaintable paintable, DyeColor color) {
        if (color == null) {
            if (!paintable.isPainted()) {
                return false;
            }
            paintable.setPaintingColor(IPaintable.UNPAINTED_COLOR);
        } else if (paintable.getPaintingColor() != color.getMapColor().col) {
            paintable.setPaintingColor(color.getMapColor().col);
        } else {
            return false;
        }
        return true;
    }

    @SuppressWarnings({ "rawtypes", "DataFlowIssue" })
    private boolean handleSpecialBlockEntities(BlockEntity first, int limit, UseOnContext context) { return false; /* ported stub */ }

    private void handleBlocks(BlockPos start, int limit, UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return;
        }
        ItemStack stack = context.getItemInHand();
        var collected = BreadthFirstBlockSearch
                .conditionalBlockPosSearch(start,
                        (parent, child) -> parent == null ||
                                level.getBlockState(child).is(level.getBlockState(parent).getBlock()),
                        limit, limit * 6);
        for (var pos : collected) {
            if (!tryPaintBlock(level, pos)) {
                break;
            }
            if (!useItemDurability(player, context.getHand(), stack, empty.get())) {
                break;
            }
        }
    }

    private <T extends IPaintable> void paintPaintables(Set<T> paintables, UseOnContext context) {
        for (var c : paintables) {
            if (!paintPaintable(c, color)) {
                continue;
            }
            // noinspection DataFlowIssue
            if (!useItemDurability(context.getPlayer(), context.getHand(), context.getItemInHand(), ItemStack.EMPTY)) {
                break;
            }
        }
    }

    private boolean tryPaintBlock(Level level, BlockPos pos) {
        BlockState blockState = level.getBlockState(pos);
        Block block = blockState.getBlock();
        if (color == null) {
            return tryStripBlockColor(level, pos, block);
        }
        return recolorBlockState(level, pos, color) || tryPaintSpecialBlock(level, pos, block);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static boolean recolorBlockState(Level level, BlockPos pos, DyeColor color) {
        BlockState state = level.getBlockState(pos);
        for (Property property : state.getProperties()) {
            if (property.getValueClass() == DyeColor.class) {
                level.setBlockAndUpdate(pos, state.setValue(property, color));
                return true;
            }
        }
        return false;
    }

    private boolean tryPaintSpecialBlock(Level world, BlockPos pos, Block block) { return false; /* ported stub */ }

    private static boolean recolorBlockNoState(Map<DyeColor, Block> map, DyeColor color, Level world, BlockPos pos) {
        return recolorBlockNoState(map, color, world, pos, null);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static boolean recolorBlockNoState(Map<DyeColor, Block> map, @Nullable DyeColor color,
                                               Level level, BlockPos pos, Block defaultBlock) {
        Block newBlock = map.getOrDefault(color, defaultBlock);
        if (newBlock == Blocks.AIR) newBlock = defaultBlock;

        BlockState old = level.getBlockState(pos);
        if (newBlock != null && newBlock != old.getBlock()) {
            BlockState state = newBlock.defaultBlockState();
            for (Property property : old.getProperties()) {
                if (!state.hasProperty(property)) continue;
                state.setValue(property, old.getValue(property));
            }
            level.setBlockAndUpdate(pos, state);
            return true;
        }
        return false;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static boolean tryStripBlockColor(Level world, BlockPos pos, Block block) { return false; /* ported stub */ }

    public boolean useItemDurability(Player player, InteractionHand hand, ItemStack stack, ItemStack replacementStack) {
        int usesLeft = getUsesLeft(stack);
        if (!player.isCreative()) {
            if (--usesLeft <= 0) {
                if (replacementStack.isEmpty()) {
                    // if replacement stack is empty, just shrink resulting stack
                    stack.shrink(1);
                } else {
                    // otherwise, update held item to replacement stack
                    player.setItemInHand(hand, replacementStack);
                }
                return false;
            }
            setUsesLeft(stack, usesLeft);
        }
        return true;
    }

    public final int getUsesLeft(ItemStack stack) {
        return totalUses - stack.getOrDefault(DataComponents.DAMAGE, 0);
    }

    public void setUsesLeft(ItemStack itemStack, int usesLeft) {
        itemStack.set(DataComponents.DAMAGE, totalUses - usesLeft);
    }

    private static final TriPredicate<IPaintable, IPaintable, Direction> paintablePredicate = (parent, child, dir) -> {
        if (parent == null) return true;
        if (!parent.getClass().equals(child.getClass())) {
            return false;
        }
        return parent.getPaintingColor() == child.getPaintingColor();
    };

    @SuppressWarnings("rawtypes")
    private static final TriPredicate<IPipeNode, IPipeNode, Direction> gtPipePredicate = (parent, child, direction) -> {
        if (parent == null) return true;
        if (!paintablePredicate.test(parent, child, direction)) {
            return false;
        }
        return parent.isConnected(direction) && child.isConnected(direction.getOpposite());
    };

    private static class AE2CallWrapper {/* ported: removed */

        static boolean isColorable(BlockEntity be) { return false; /* ported stub */ }/* ported: removed */
    }

    private static boolean recolorCreateSail(Level world, BlockPos pos, @Nullable DyeColor color) { return false; /* ported stub */ }
}
