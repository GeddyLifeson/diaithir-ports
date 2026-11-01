package org.diaithir.connected.content.crossconnector;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.decoration.encasing.EncasableBlock;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.diaithir.connected.content.IConnectionForwardingBlock;
import org.diaithir.connected.content.KineticHelper;
import org.jspecify.annotations.Nullable;

/** A static junction that lets two perpendicular shaft lines cross without connecting, by forwarding network links through it. */
public class CrossConnectorBlock extends Block implements IWrenchable, IConnectionForwardingBlock, IRotate, EncasableBlock {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
    private static final VoxelShape CORE = Block.box(5, 5, 5, 11, 11, 11);

    public CrossConnectorBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(AXIS, Direction.Axis.Y));
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rot) {
        return switch (rot) {
            case COUNTERCLOCKWISE_90, CLOCKWISE_90 -> switch (state.getValue(AXIS)) {
                case X -> state.setValue(AXIS, Direction.Axis.Z);
                case Z -> state.setValue(AXIS, Direction.Axis.X);
                default -> state;
            };
            default -> state;
        };
    }

    public static Direction.@Nullable Axis getPreferredAxis(BlockPlaceContext context) {
        Direction.Axis preferred = null;
        for (Direction side : Iterate.directions) {
            BlockState blockState = context.getLevel().getBlockState(context.getClickedPos().relative(side));
            if (blockState.getBlock() instanceof IRotate rot
                && rot.hasShaftTowards(context.getLevel(), context.getClickedPos().relative(side), blockState, side.getOpposite())) {
                if (preferred != null && preferred != side.getAxis()) {
                    return null;
                }
                preferred = side.getAxis();
            }
        }
        return preferred;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction.Axis preferred = getPreferredAxis(context);
        boolean sneaking = context.getPlayer() != null && context.getPlayer().isShiftKeyDown();
        if (preferred != null && !sneaking) {
            Direction.Axis looking = context.getNearestLookingDirection().getAxis();
            if (looking == preferred) return defaultBlockState().setValue(AXIS, preferred.isVertical() ? Direction.Axis.X : Direction.Axis.Y);
            return defaultBlockState().setValue(AXIS, looking);
        }
        return defaultBlockState().setValue(AXIS, preferred != null && sneaking ? context.getClickedFace().getAxis() : context.getNearestLookingDirection().getAxis());
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.isShiftKeyDown() || !player.mayBuild()) return InteractionResult.TRY_WITH_EMPTY_HAND;
        return tryEncase(state, level, pos, stack, player, hand, hit);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction.Axis axis = state.getValue(AXIS);
        VoxelShape shape = CORE;
        for (Direction.Axis a : Direction.Axis.values()) {
            if (a == axis) continue;
            shape = Shapes.or(shape, a == Direction.Axis.X ? Block.box(0, 6, 6, 16, 10, 10) : a == Direction.Axis.Y ? Block.box(6, 0, 6, 10, 16, 10) : Block.box(6, 6, 0, 10, 10, 16));
        }
        return shape;
    }

    public void updateConnections(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) return;
        Direction.Axis axis = state.getValue(AXIS);
        for (Direction direction : Iterate.directions) {
            if (direction.getAxis() == axis) continue;
            BlockPos sourcePos = pos;
            BlockPos neighborPos = pos.relative(direction);
            while (!sourcePos.equals(neighborPos) && level.getBlockState(neighborPos).getBlock() instanceof IConnectionForwardingBlock fwd) {
                BlockPos temp = sourcePos;
                sourcePos = neighborPos;
                neighborPos = fwd.forwardConnection(level, temp, temp.equals(pos) ? state : level.getBlockState(temp), neighborPos);
            }
            BlockEntity be = level.getBlockEntity(neighborPos);
            if (be instanceof KineticBlockEntity kinetic) KineticHelper.updateKineticBlock(kinetic);
        }
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        updateConnections(level, pos, state);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos, boolean movedByPiston) {
        updateConnections(level, pos, state);
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }

    @Override
    public BlockPos forwardConnection(Level level, BlockPos sourcePos, BlockState sourceState, BlockPos neighbourPos) {
        BlockState state = level.getBlockState(neighbourPos);
        if (state.getBlock() != this) return neighbourPos;
        Vec3i offset = neighbourPos.subtract(sourcePos);
        if (!(sourceState.getBlock() instanceof IRotate rotating)) return neighbourPos;
        Direction offsetDirection = null;
        for (Direction d : Direction.values()) if (d.getUnitVec3i().equals(offset)) offsetDirection = d;
        if (offsetDirection == null) return neighbourPos;
        if (!rotating.hasShaftTowards(level, sourcePos, sourceState, offsetDirection)) return neighbourPos;
        if (offsetDirection.getAxis() == state.getValue(AXIS)) return neighbourPos;
        return neighbourPos.offset(offset);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() != state.getValue(AXIS);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(AXIS);
    }
}
