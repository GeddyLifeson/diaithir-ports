package net.diaithir.bridges;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/** Put a rail on top; a fitted locomotive standing there is charged from the Forge Energy this block receives. */
final class LocoChargerBlock extends Block implements EntityBlock {
    LocoChargerBlock(Properties props) { super(props); }

    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new LocoChargerEntity(pos, state); }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != DiaithirBridges.LOCO_CHARGER_ENTITY.get()) return null;
        return (lvl, pos, st, be) -> ((LocoChargerEntity) be).serverTick();
    }

    @Override protected boolean hasAnalogOutputSignal(BlockState state) { return true; }

    @Override protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction dir) {
        return level.getBlockEntity(pos) instanceof LocoChargerEntity be ? be.signal() : 0;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof LocoChargerEntity be) player.sendSystemMessage(be.status());
        return InteractionResult.SUCCESS;
    }
}
