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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

/**
 * A bridge segment: a deck at the top of its block with a railing on every side that does not touch another bridge
 * segment, so a row of them makes one bridge with rails along both edges. Rails are 24 sixteenths tall for collision.
 */
public final class BridgeBlock extends Block {
    public static final MapCodec<BridgeBlock> CODEC = simpleCodec(BridgeBlock::new);
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    private static final Map<Direction, BooleanProperty> SIDES = Map.of(
        Direction.NORTH, NORTH, Direction.EAST, EAST, Direction.SOUTH, SOUTH, Direction.WEST, WEST);
    private static final VoxelShape DECK = Block.box(0, 12, 0, 16, 16, 16);
    private static final Map<Direction, VoxelShape> RAILS = Map.of(
        Direction.NORTH, Block.box(0, 16, 0, 16, 24, 2), Direction.SOUTH, Block.box(0, 16, 14, 16, 24, 16),
        Direction.WEST, Block.box(0, 16, 0, 2, 24, 16), Direction.EAST, Block.box(14, 16, 0, 16, 24, 16));

    public BridgeBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(NORTH, true).setValue(EAST, true).setValue(SOUTH, true).setValue(WEST, true));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    private boolean open(BlockState neighbour) {
        return !(neighbour.getBlock() instanceof BridgeBlock);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState s = defaultBlockState();
        for (Map.Entry<Direction, BooleanProperty> e : SIDES.entrySet()) {
            s = s.setValue(e.getValue(), open(context.getLevel().getBlockState(context.getClickedPos().relative(e.getKey()))));
        }
        return s;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction dir,
                                     BlockPos neighbourPos, BlockState neighbour, RandomSource random) {
        BooleanProperty p = SIDES.get(dir);
        return p == null ? state : state.setValue(p, open(neighbour));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape s = DECK;
        for (Map.Entry<Direction, BooleanProperty> e : SIDES.entrySet()) {
            if (state.getValue(e.getValue())) {
                s = Shapes.or(s, RAILS.get(e.getKey()));
            }
        }
        return s;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST);
    }
}
