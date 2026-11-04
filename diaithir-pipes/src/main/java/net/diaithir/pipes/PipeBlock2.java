package net.diaithir.pipes;

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
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/** One pipe block. A 4x4 core with an arm toward every face that holds a pipe or a capability block and is not disconnected. */
public final class PipeBlock2 extends Block implements EntityBlock {
    public static final MapCodec<PipeBlock2> CODEC = simpleCodec(p -> new PipeBlock2(p, PipeKind.ITEM, PipeTier.BASIC));
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

    private final PipeKind kind;
    private final PipeTier tier;

    public PipeBlock2(Properties properties, PipeKind kind, PipeTier tier) {
        super(properties);
        this.kind = kind;
        this.tier = tier;
        BlockState s = stateDefinition.any();
        for (Direction d : Direction.values()) s = s.setValue(CONN.get(d), false);
        registerDefaultState(s);
    }

    public PipeKind kind() {
        return kind;
    }

    public PipeTier tier() {
        return tier;
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        for (Direction d : Direction.values()) builder.add(CONN.get(d));
    }

    /** True if the neighbour is a pipe of the same kind or exposes the matching capability on the facing side. */
    boolean joins(LevelReader level, BlockPos pos, Direction d) {
        BlockState n = level.getBlockState(pos.relative(d));
        if (n.getBlock() instanceof PipeBlock2 p) return p.kind == kind;
        if (!(level instanceof Level l)) return n.hasBlockEntity();
        return PipeIo.hasCapability(l, kind, pos.relative(d), d.getOpposite());
    }

    private BlockState recompute(BlockState state, LevelReader level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        for (Direction d : Direction.values()) {
            boolean off = be instanceof PipeBlockEntity p && p.mode(d) == FaceMode.DISCONNECTED;
            state = state.setValue(CONN.get(d), !off && joins(level, pos, d));
        }
        return state;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return recompute(defaultBlockState(), context.getLevel(), context.getClickedPos());
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction dir, BlockPos neighbourPos,
            BlockState neighbourState, RandomSource random) {
        return recompute(state, level, pos);
    }

    private static int mask(BlockState state) {
        int m = 0;
        for (Direction d : Direction.values()) if (state.getValue(CONN.get(d))) m |= 1 << d.ordinal();
        return m;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[mask(state)];
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PipeBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != DiaithirPipes.PIPE_BE.get()) return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof PipeBlockEntity p && lvl instanceof ServerLevel sl) p.serverTick(sl);
        };
    }
}
