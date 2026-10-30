package net.diaithir.storagenet;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/** A thin cable. It joins itself to neighbouring network blocks and to anything with a block entity (chests, barrels). The plain cable has no block entity and no tick. */
public class CableBlock extends Block implements NetBlock {
    public static final MapCodec<CableBlock> CODEC = simpleCodec(p -> new CableBlock(p, CableKind.CABLE));
    public static final Map<Direction, BooleanProperty> CONN = PipeBlock.PROPERTY_BY_DIRECTION;

    private static final VoxelShape CORE = Block.box(6, 6, 6, 10, 10, 10);
    private static final VoxelShape[] ARMS = new VoxelShape[6];
    private static final VoxelShape[] SHAPES = new VoxelShape[64];

    static {
        ARMS[Direction.NORTH.ordinal()] = Block.box(6, 6, 0, 10, 10, 6);
        ARMS[Direction.SOUTH.ordinal()] = Block.box(6, 6, 10, 10, 10, 16);
        ARMS[Direction.WEST.ordinal()] = Block.box(0, 6, 6, 6, 10, 10);
        ARMS[Direction.EAST.ordinal()] = Block.box(10, 6, 6, 16, 10, 10);
        ARMS[Direction.DOWN.ordinal()] = Block.box(6, 0, 6, 10, 6, 10);
        ARMS[Direction.UP.ordinal()] = Block.box(6, 10, 6, 10, 16, 10);
        for (int m = 0; m < 64; m++) {
            VoxelShape s = CORE;
            for (Direction d : Direction.values()) if ((m & (1 << d.ordinal())) != 0) s = Shapes.or(s, ARMS[d.ordinal()]);
            SHAPES[m] = s;
        }
    }

    private final CableKind kind;

    public CableBlock(Properties properties, CableKind kind) {
        super(properties);
        this.kind = kind;
        BlockState s = stateDefinition.any();
        for (Direction d : Direction.values()) s = s.setValue(CONN.get(d), false);
        registerDefaultState(s);
    }

    public CableKind kind() {
        return kind;
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        for (Direction d : Direction.values()) builder.add(CONN.get(d));
    }

    static boolean connects(BlockState neighbour) {
        return neighbour.getBlock() instanceof NetBlock || neighbour.hasBlockEntity();
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState s = defaultBlockState();
        for (Direction d : Direction.values()) s = s.setValue(CONN.get(d), connects(level.getBlockState(pos.relative(d))));
        return s;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction dir, BlockPos neighbourPos,
            BlockState neighbourState, RandomSource random) {
        return state.setValue(CONN.get(dir), connects(neighbourState));
    }

    private int mask(BlockState state) {
        int m = 0;
        for (Direction d : Direction.values()) if (state.getValue(CONN.get(d))) m |= 1 << d.ordinal();
        return m;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[mask(state)];
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        Networks.markDirty(level);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, orientation, movedByPiston);
        Networks.markDirty(level);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        Networks.markDirty(level);
    }
}
