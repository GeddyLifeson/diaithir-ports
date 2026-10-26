package net.diaithir.bridges;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Containers;
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

final class BridgeBlock extends Block implements EntityBlock {
    private final BridgeEntity.Kind kind;

    BridgeEntity.Kind kind() { return kind; }

    BridgeBlock(Properties props, BridgeEntity.Kind kind) {
        super(props);
        this.kind = kind;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BridgeEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != DiaithirBridges.ENTITY.get()) return null;
        return (lvl, pos, st, be) -> ((BridgeEntity) be).serverTick();
    }

    // a reagent in hand goes into the machine
    @Override
    protected InteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof BridgeEntity be)) return InteractionResult.PASS;
        if (kind != BridgeEntity.Kind.SOURCE || !BridgeEntity.fuelValue(held).isPresent()) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        ItemStack slot = be.getItem(0);
        if (!slot.isEmpty() && !ItemStack.isSameItemSameComponents(slot, held)) return InteractionResult.PASS;
        int room = held.getMaxStackSize() - slot.getCount();
        int n = Math.min(room, held.getCount());
        if (n <= 0) return InteractionResult.PASS;
        if (slot.isEmpty()) be.setItem(0, held.copyWithCount(n)); else slot.grow(n);
        held.shrink(n);
        be.setChanged();
        return InteractionResult.SUCCESS;
    }

    // an empty hand takes the output (charger) or reads the machine
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof BridgeEntity be)) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (kind == BridgeEntity.Kind.CHARGER && !be.getItem(0).isEmpty()) {
            ItemStack out = be.removeItemNoUpdate(0);
            if (!player.getInventory().add(out)) Containers.dropItemStack(level, pos.getX(), pos.getY() + 1, pos.getZ(), out);
            be.setChanged();
        }
        player.sendSystemMessage(be.status());
        return InteractionResult.SUCCESS;
    }

}
