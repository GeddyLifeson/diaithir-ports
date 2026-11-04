package net.diaithir.carts;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.RailShape;

import java.util.List;

/**
 * Guards the stretch of track it faces (the rail in front of it, or just below that, and up to LIMIT rails each way along the connected track).
 * It shows red and gives a redstone signal of 15 while any minecart is on that stretch, green and 0 otherwise. Wire the signal to a station rail,
 * a powered rail or a lever-driven gate upstream to keep trains apart.
 */
public final class BlockSignal extends Block {
    public static final int LIMIT = 32, INTERVAL = 10;
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty RED = BooleanProperty.create("red");

    public BlockSignal(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(RED, false));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(FACING, RED); }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) { return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite()); }

    @Override protected boolean isSignalSource(BlockState state) { return true; }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction dir) { return state.getValue(RED) ? 15 : 0; }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        if (!level.isClientSide() && !old.is(this)) level.scheduleTick(pos, this, 1);
    }

    /** The rails this signal guards. */
    public static List<BlockPos> stretch(Level level, BlockPos pos, Direction facing) {
        BlockPos front = pos.relative(facing);
        for (BlockPos p : new BlockPos[]{front, front.below(), front.above()}) if (RailTrace.isRail(level, p)) return RailTrace.trace(level, p, LIMIT);
        return List.of();
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        boolean red = RailTrace.occupied(level, stretch(level, pos, state.getValue(FACING)));
        if (red != state.getValue(RED)) level.setBlock(pos, state.setValue(RED, red), 3);
        level.scheduleTick(pos, this, INTERVAL);
    }
}
