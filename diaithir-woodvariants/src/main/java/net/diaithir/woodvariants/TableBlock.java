package net.diaithir.woodvariants;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A table that joins to any table beside it: the sides that meet lose their edge leg, so a row of tables
 * is one long top with legs only at the outer corners.
 */
public final class TableBlock extends Block {
    public static final MapCodec<TableBlock> CODEC = simpleCodec(TableBlock::new);
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    private static final VoxelShape TOP = Block.box(0, 13, 0, 16, 16, 16);
    private static final VoxelShape[] SHAPES = new VoxelShape[16];

    static {
        for (int i = 0; i < 16; i++) {
            SHAPES[i] = shapeFor((i & 1) != 0, (i & 2) != 0, (i & 4) != 0, (i & 8) != 0);
        }
    }

    public TableBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false).setValue(WEST, false));
    }

    static VoxelShape shapeFor(boolean n, boolean e, boolean s, boolean w) {
        boolean[] l = Box.legs(n, e, s, w);
        VoxelShape shape = TOP;
        if (l[0]) shape = Shapes.or(shape, Block.box(1, 0, 1, 4, 13, 4));
        if (l[1]) shape = Shapes.or(shape, Block.box(12, 0, 1, 15, 13, 4));
        if (l[2]) shape = Shapes.or(shape, Block.box(12, 0, 12, 15, 13, 15));
        if (l[3]) shape = Shapes.or(shape, Block.box(1, 0, 12, 4, 13, 15));
        return shape;
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int i = (state.getValue(NORTH) ? 1 : 0) | (state.getValue(EAST) ? 2 : 0) | (state.getValue(SOUTH) ? 4 : 0) | (state.getValue(WEST) ? 8 : 0);
        return SHAPES[i];
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST);
    }

    private static BooleanProperty prop(Direction d) {
        return switch (d) {
            case NORTH -> NORTH;
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            default -> WEST;
        };
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState();
        for (Direction d : Direction.Plane.HORIZONTAL) {
            state = state.setValue(prop(d), context.getLevel().getBlockState(context.getClickedPos().relative(d)).getBlock() instanceof TableBlock);
        }
        return state;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
                                     BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        if (direction.getAxis().isHorizontal()) {
            return state.setValue(prop(direction), neighbourState.getBlock() instanceof TableBlock);
        }
        return super.updateShape(state, level, ticks, pos, direction, neighbourPos, neighbourState, random);
    }
}
