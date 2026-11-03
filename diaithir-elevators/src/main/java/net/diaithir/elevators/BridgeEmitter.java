package net.diaithir.elevators;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * Light bridge emitter. While it has a redstone signal it projects a walkable strip of light blocks in front of it, up to {@link #RANGE} blocks or until
 * something solid is in the way; with no signal the strip fades. Clean-room: written from the behaviour only (an emitter that lays a floor of light).
 */
public class BridgeEmitter extends HorizontalDirectionalBlock {
    public static final MapCodec<BridgeEmitter> CODEC = simpleCodec(BridgeEmitter::new);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final int RANGE = 24, PERIOD = 10;

    public BridgeEmitter(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, false));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(FACING, POWERED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection()).setValue(POWERED, ctx.getLevel().hasNeighborSignal(ctx.getClickedPos()));
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        if (!level.isClientSide()) level.scheduleTick(pos, this, 1);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, net.minecraft.world.level.redstone.Orientation o, boolean moved) {
        if (level.isClientSide()) return;
        boolean p = level.hasNeighborSignal(pos);
        if (p != state.getValue(POWERED)) {
            level.setBlock(pos, state.setValue(POWERED, p), 2);
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        Direction f = state.getValue(FACING);
        boolean on = state.getValue(POWERED);
        boolean blocked = false;
        for (int i = 1; i <= RANGE; i++) {
            BlockPos p = pos.relative(f, i);
            BlockState s = level.getBlockState(p);
            boolean ours = s.is(ElevatorsMod.LIGHT_BRIDGE.get());
            if (on && !blocked) {
                if (ours) continue;
                if (s.isAir() || s.canBeReplaced()) level.setBlock(p, ElevatorsMod.LIGHT_BRIDGE.get().defaultBlockState().setValue(LightBridge.AXIS_X, f.getAxis() == Direction.Axis.X), 3);
                else blocked = true;
            } else if (ours) {
                level.removeBlock(p, false);
            } else {
                break;
            }
        }
        if (on) level.scheduleTick(pos, this, PERIOD);
    }
}
