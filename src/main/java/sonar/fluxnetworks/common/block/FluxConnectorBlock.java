package sonar.fluxnetworks.common.block;

import net.minecraft.world.level.redstone.Orientation;

import net.minecraft.world.level.ScheduledTickAccess;

import net.minecraft.util.RandomSource;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import sonar.fluxnetworks.common.device.TileFluxConnector;
import sonar.fluxnetworks.common.util.FluxUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.*;

/**
 * Defines the block base class for any flux device that can connect to adjacent blocks.
 */
@ParametersAreNonnullByDefault
public abstract class FluxConnectorBlock extends FluxDeviceBlock implements SimpleWaterloggedBlock {

    // the indices are equal to that of Direction.get3DDataValue()
    public static final BooleanProperty[] SIDES_CONNECTED = new BooleanProperty[]{DOWN, UP, NORTH, SOUTH, WEST, EAST};

    protected FluxConnectorBlock(Properties props) {
        super(props);
        registerDefaultState(getStateDefinition().any()
                .setValue(DOWN, Boolean.FALSE)
                .setValue(UP, Boolean.FALSE)
                .setValue(NORTH, Boolean.FALSE)
                .setValue(SOUTH, Boolean.FALSE)
                .setValue(WEST, Boolean.FALSE)
                .setValue(EAST, Boolean.FALSE)
                .setValue(WATERLOGGED, Boolean.FALSE)
        );
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
                                @Nullable Orientation orientation, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, orientation, isMoving);
        // block changed; 26.1 no longer passes the neighbour's position, so every side is rechecked
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TileFluxConnector c) {
            for (Direction dir : Direction.values()) {
                c.updateSideTransfer(dir, level.getBlockEntity(pos.relative(dir)));
            }
        }
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
        super.onNeighborChange(state, level, pos, neighbor);
        // block entity changed, not always being called, because some mods do not call related methods
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TileFluxConnector c) {
            Direction dir = FluxUtils.getBlockDirection(pos, neighbor);
            if (dir != null) {
                c.updateSideTransfer(dir, level.getBlockEntity(neighbor));
            }
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(WATERLOGGED,
                context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
    }

    /**
     * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
     * For example, fences make their connections to the passed in state if possible, and wet concrete powder
     * immediately returns its solidified counterpart.
     * Note that this method should ideally consider only the specific direction passed in.
     */
    @Nonnull
    @Override
    public BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos currentPos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (state.getValue(WATERLOGGED)) {
            ticks.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, level, ticks, currentPos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SIDES_CONNECTED);
        builder.add(WATERLOGGED);
    }

    @Nonnull
    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }
}
