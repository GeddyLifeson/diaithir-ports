package net.diaithir.elevators;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** One cell of a light bridge: a thin floor you can walk on. It checks every second that an emitter still feeds it and fades if not. */
final class LightBridge extends Block {
    static final BooleanProperty AXIS_X = BooleanProperty.create("axis_x");
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 2, 16);

    LightBridge(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(AXIS_X, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(AXIS_X);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        if (!level.isClientSide()) level.scheduleTick(pos, this, 20);
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    /** True if walking back along the strip reaches a powered emitter facing it within range. */
    static boolean fed(Level level, BlockPos pos, boolean axisX) {
        for (Direction d : axisX ? new Direction[]{Direction.EAST, Direction.WEST} : new Direction[]{Direction.NORTH, Direction.SOUTH}) {
            for (int i = 1; i <= BridgeEmitter.RANGE + 1; i++) {
                BlockState s = level.getBlockState(pos.relative(d, i));
                if (s.is(ElevatorsMod.LIGHT_BRIDGE.get())) continue;
                if (s.is(ElevatorsMod.EMITTER.get()) && s.getValue(BridgeEmitter.POWERED) && s.getValue(BridgeEmitter.FACING) == d.getOpposite()) return true;
                break;
            }
        }
        return false;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (fed(level, pos, state.getValue(AXIS_X))) level.scheduleTick(pos, this, 20);
        else level.removeBlock(pos, false);
    }
}
