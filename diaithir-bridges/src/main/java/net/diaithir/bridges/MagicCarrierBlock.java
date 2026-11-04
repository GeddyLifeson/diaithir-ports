package net.diaithir.bridges;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/** Magic carrier block: a reagent in hand goes in, sneaking with an empty hand picks the carrier, an empty hand reads it. */
final class MagicCarrierBlock extends Block implements EntityBlock {
    MagicCarrierBlock(Properties props) { super(props); }

    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new MagicCarrierEntity(pos, state); }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != DiaithirBridges.MAGIC_CARRIER_ENTITY.get()) return null;
        return (lvl, pos, st, be) -> ((MagicCarrierEntity) be).serverTick();
    }

    @Override protected boolean hasAnalogOutputSignal(BlockState state) { return true; }

    @Override protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction dir) {
        return level.getBlockEntity(pos) instanceof MagicCarrierEntity be ? be.signal() : 0;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof MagicCarrierEntity be) || !be.canPlaceItem(0, held)) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        ItemStack slot = be.getItem(0);
        if (!slot.isEmpty() && !ItemStack.isSameItemSameComponents(slot, held)) return InteractionResult.PASS;
        int n = Math.min(held.getMaxStackSize() - slot.getCount(), held.getCount());
        if (n <= 0) return InteractionResult.PASS;
        if (slot.isEmpty()) be.setItem(0, held.copyWithCount(n)); else slot.grow(n);
        held.shrink(n);
        be.setChanged();
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof MagicCarrierEntity be)) return InteractionResult.PASS;
        boolean changed = player.isShiftKeyDown() && be.cycle();
        player.sendSystemMessage(Component.literal(be.describe() + (changed ? " (carrier changed)" : player.isShiftKeyDown() ? " (empty it to change carrier)" : " (sneak to change carrier)")));
        return InteractionResult.SUCCESS;
    }
}
