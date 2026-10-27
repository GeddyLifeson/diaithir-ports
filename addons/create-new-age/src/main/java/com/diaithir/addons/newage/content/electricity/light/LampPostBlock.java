package com.diaithir.addons.newage.content.electricity.light;

import net.minecraft.world.level.redstone.Orientation;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public class LampPostBlock extends Block implements IWrenchable {

    public static final BooleanProperty TOP = BooleanProperty.create("top");
    public static final BooleanProperty BOTTOM = BooleanProperty.create("bottom");
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");

    public LampPostBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(TOP,  true)
                .setValue(BOTTOM,  true)
                .setValue(NORTH,  false)
                .setValue(EAST,  false)
                .setValue(SOUTH,  false)
                .setValue(WEST,  false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(TOP, BOTTOM, NORTH, EAST, SOUTH, WEST));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = Block.box(6, 0, 6, 10, 16, 10);

        if (state.getValue(TOP)) {
            shape = Shapes.or(shape, Block.box(5, 14, 5, 11, 16, 11));
        }
        if (state.getValue(NORTH) || state.getValue(EAST) || state.getValue(SOUTH) || state.getValue(WEST)) {
            shape = Shapes.or(shape, Block.box(5, 5, 5, 11, 11, 11));
        }else if (state.getValue(BOTTOM)) {
            shape = Shapes.or(shape, Block.box(5, 0, 5, 11, 11, 11));
        }

        if (state.getValue(NORTH)) {
            shape = Shapes.or(shape, Block.box(6, 6, 0, 10, 10, 6));
        }
        if (state.getValue(EAST)) {
            shape = Shapes.or(shape, Block.box(10, 6, 6, 16, 10, 10));
        }
        if (state.getValue(SOUTH)) {
            shape = Shapes.or(shape, Block.box(6, 6, 10, 10, 10, 16));
        }
        if (state.getValue(WEST)) {
            shape = Shapes.or(shape, Block.box(0, 6, 6, 6, 10, 10));
        }

        return shape;
    }

    public static BooleanProperty getDirectionProperty(Direction direction) {
        return switch (direction) {
            case DOWN -> BOTTOM;
            case UP -> TOP;
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case EAST -> EAST;
        };
    }

    public BlockState updateState(BlockState state, LevelAccessor world, BlockPos pos, BiFunction<Direction, BlockState, Boolean> horizontalCheck) {
        BlockState top = world.getBlockState(pos.relative(Direction.UP));
        BlockState bottom = world.getBlockState(pos.relative(Direction.DOWN));
        BlockState north = world.getBlockState(pos.relative(Direction.NORTH));
        BlockState east = world.getBlockState(pos.relative(Direction.EAST));
        BlockState south = world.getBlockState(pos.relative(Direction.SOUTH));
        BlockState west = world.getBlockState(pos.relative(Direction.WEST));

        return state
                .setValue(TOP, !(top.getBlock() instanceof LampPostBlock))
                .setValue(BOTTOM, !(bottom.getBlock() instanceof LampPostBlock))
                .setValue(NORTH, north.getBlock() instanceof LampPostBlock && horizontalCheck.apply(Direction.NORTH, north))
                .setValue(EAST, east.getBlock() instanceof LampPostBlock && horizontalCheck.apply(Direction.EAST, east))
                .setValue(SOUTH, south.getBlock() instanceof LampPostBlock && horizontalCheck.apply(Direction.SOUTH, south))
                .setValue(WEST, west.getBlock() instanceof LampPostBlock && horizontalCheck.apply(Direction.WEST, west));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = this.defaultBlockState();
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();

        return updateState(state, world, pos, (dir, st) -> context.getClickedFace() == dir.getOpposite());
    }

    @Override
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block neighborBlock, Orientation neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, world, pos, neighborBlock, neighborPos, movedByPiston);

        state = updateState(state, world, pos, (dir, st) -> st.getValue(getDirectionProperty(dir.getOpposite())));

        world.setBlockAndUpdate(pos, state);
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        Direction face = context.getClickedFace();
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();

        Block connectedBlock = world.getBlockState(pos.relative(face)).getBlock();
        BooleanProperty property = getDirectionProperty(face);
        if (face.getAxis().isHorizontal() && connectedBlock instanceof LampPostBlock) {
            world.setBlockAndUpdate(pos, state.setValue(property, !state.getValue(property)));
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
