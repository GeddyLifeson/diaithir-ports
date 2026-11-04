package net.diaithir.woodvariants;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

/** Decorative furniture that faces the person who placed it: coffee table, end table, cupboard, counter, bookcase. */
public final class DecorBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<DecorBlock> CODEC = simpleCodec(DecorBlock::new);
    public static final int[][] COFFEE = {Box.b(1, 7, 1, 15, 9, 15), Box.b(2, 0, 2, 4, 7, 4), Box.b(12, 0, 2, 14, 7, 4), Box.b(2, 0, 12, 4, 7, 14), Box.b(12, 0, 12, 14, 7, 14)};
    public static final int[][] END = {Box.b(2, 13, 2, 14, 16, 14), Box.b(3, 2, 3, 13, 13, 13), Box.b(3, 0, 3, 5, 2, 5), Box.b(11, 0, 3, 13, 2, 5), Box.b(3, 0, 11, 5, 2, 13), Box.b(11, 0, 11, 13, 2, 13)};
    public static final int[][] CUPBOARD = {Box.b(1, 0, 1, 15, 16, 15)};
    public static final int[][] COUNTER = {Box.b(0, 0, 0, 16, 16, 16)};

    private final Map<Direction, VoxelShape> shapes;

    public DecorBlock(Properties properties) {
        this(properties, COUNTER);
    }

    public DecorBlock(Properties properties, int[][] boxes) {
        super(properties);
        this.shapes = Box.shapes(boxes);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes.get(state.getValue(FACING));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
