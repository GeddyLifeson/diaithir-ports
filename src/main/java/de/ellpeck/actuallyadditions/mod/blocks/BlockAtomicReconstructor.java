/*
 * This file ("BlockAtomicReconstructor.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.blocks;

import java.util.function.Consumer;

import net.minecraft.world.item.component.TooltipDisplay;

import net.minecraft.core.Direction;

import de.ellpeck.actuallyadditions.api.lens.ILensItem;
import de.ellpeck.actuallyadditions.mod.blocks.base.FullyDirectionalBlock;
import de.ellpeck.actuallyadditions.mod.blocks.blockhuds.IBlockHud;
import de.ellpeck.actuallyadditions.mod.blocks.blockhuds.ReconstructorHud;
import de.ellpeck.actuallyadditions.mod.components.ActuallyComponents;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityAtomicReconstructor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.NumberFormat;
import java.util.List;

public class BlockAtomicReconstructor extends FullyDirectionalBlock.Container implements IHudDisplay {
    private static final IBlockHud HUD = new ReconstructorHud();
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    public static final int NAME_FLAVOR_AMOUNTS_1 = 12;
    public static final int NAME_FLAVOR_AMOUNTS_2 = 14;

    public BlockAtomicReconstructor() {
        super(ActuallyBlocks.defaultPickProps(10.0F, 80F));
    }

    @Override
    protected InteractionResult useItemOn(ItemStack pStack, BlockState pState, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult pHitResult) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (this.tryToggleRedstone(world, pos, player)) {
            return InteractionResult.SUCCESS;
        }
        if (!world.isClientSide()) {
            TileEntityAtomicReconstructor reconstructor = (TileEntityAtomicReconstructor) world.getBlockEntity(pos);
            if (reconstructor != null) {
                if (!heldItem.isEmpty()) {
                    Item item = heldItem.getItem();
                    if (item instanceof ILensItem && reconstructor.inv.getStackInSlot(0).isEmpty()) {
                        ItemStack toPut = heldItem.copy();
                        toPut.setCount(1);
                        reconstructor.inv.setStackInSlot(0, toPut);
                        if (!player.isCreative()) {
                            heldItem.shrink(1);
                        }
                        return InteractionResult.CONSUME;
                    } else if (ItemStack.isSameItem(heldItem, reconstructor.inv.getStackInSlot(0)) && heldItem.getCount() + 1 <= heldItem.getMaxStackSize()) {
                        reconstructor.inv.setStackInSlot(0, ItemStack.EMPTY);
                        heldItem.grow(1);
                    }
                } else {
                    ItemStack slot = reconstructor.inv.getStackInSlot(0);
                    if (!slot.isEmpty() && hand == InteractionHand.MAIN_HAND) {
                        player.setItemInHand(hand, slot.copy());
                        reconstructor.inv.setStackInSlot(0, ItemStack.EMPTY);
                        return InteractionResult.CONSUME;
                    }
                }
            }
            return InteractionResult.FAIL;
        }
        return InteractionResult.CONSUME;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileEntityAtomicReconstructor(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> entityType) {
        return level.isClientSide()? TileEntityAtomicReconstructor::clientTick : TileEntityAtomicReconstructor::serverTick;
    }

    @Override
    public IBlockHud getHud() {
        return HUD;
    }

    public static class TheItemBlock extends AABlockItem {

        private long lastSysTime;
        private int toPick1;
        private int toPick2;
        private final Block block;

        public TheItemBlock(Block blockIn) {
            super(blockIn, ActuallyBlocks.defaultBlockItemProperties());
            block = blockIn;
        }

        
        @Override
        public void appendHoverText(@Nonnull ItemStack pStack, @Nonnull Item.TooltipContext context, TooltipDisplay display, @Nonnull Consumer<Component> pTooltip, @Nonnull TooltipFlag pFlag) {
            super.appendHoverText(pStack, context, display, pTooltip, pFlag);

            long sysTime = System.currentTimeMillis();

            if (this.lastSysTime + 3000 < sysTime) {
                this.lastSysTime = sysTime;
                if (context.level() != null) {
                    RandomSource random = context.level().getRandom();
                    this.toPick1 = random.nextInt(NAME_FLAVOR_AMOUNTS_1) + 1;
                    this.toPick2 = random.nextInt(NAME_FLAVOR_AMOUNTS_2) + 1;
                }
            }

            String base = block.getDescriptionId() + ".info.";
            pTooltip.accept(Component.translatable(base + "1." + this.toPick1).append(" ").append(Component.translatable(base + "2." + this.toPick2)).withStyle(s -> s.withColor(ChatFormatting.GRAY)));

            if (pStack.has(ActuallyComponents.ENERGY_STORAGE) ) {
                int energy = pStack.getOrDefault(ActuallyComponents.ENERGY_STORAGE, 0);
                NumberFormat format = NumberFormat.getInstance();
                pTooltip.accept(Component.translatable("misc.actuallyadditions.power_single", format.format(energy)).withStyle(ChatFormatting.GRAY));
            }
            if (pStack.has(ActuallyComponents.PULSE_MODE)) {
                pTooltip.accept(Component.translatable("info.actuallyadditions.redstoneMode").append(": ")
                        .append(Component.translatable(pStack.getOrDefault(ActuallyComponents.PULSE_MODE, false)?"info.actuallyadditions.redstoneMode.pulse":"info.actuallyadditions.redstoneMode.deactivation").withStyle($ -> $.withColor(ChatFormatting.RED))));
            }
        }

        @Override
        protected boolean updateCustomBlockEntityTag(BlockPos pPos, Level pLevel, @Nullable Player pPlayer, ItemStack pStack, BlockState pState) {
            return super.updateCustomBlockEntityTag(pPos, pLevel, pPlayer, pStack, pState);
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level world, BlockPos pos, Direction direction) {
        BlockEntity t = world.getBlockEntity(pos);
        int i = 0;
        if (t instanceof TileEntityAtomicReconstructor) {
            i = ((TileEntityAtomicReconstructor) t).getEnergy();
        }
        return Mth.clamp(i / 20000, 0, 15);
    }
}
