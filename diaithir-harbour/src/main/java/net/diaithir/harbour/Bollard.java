package net.diaithir.harbour;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Mooring bollard. Every few ticks it takes hold of boats within {@link #reach} blocks; a held boat cannot drift or be paddled away until the bollard is
 * let go (use it bare-handed, or give it a redstone signal). Clean-room: the behaviour of a quay post, nothing copied.
 */
public class Bollard extends Block {
    public static final MapCodec<Bollard> CODEC = simpleCodec(Bollard::new);
    public static final BooleanProperty LOOSE = BooleanProperty.create("loose");
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final int PERIOD = 4;
    private static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 12, 12);

    /** Boats being held: boat id to the bollard position and level key. */
    static final Map<UUID, Held> HELD = new HashMap<>();

    record Held(BlockPos bollard, String dimension) {}

    public Bollard(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(LOOSE, false).setValue(POWERED, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(LOOSE, POWERED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    /** How far a bollard reaches; the harbour office raises it with the Age (see {@link Office}). */
    public static int reach(Level level, BlockPos pos) {
        return Office.reachNear(level, pos);
    }

    public static boolean holding(BlockState s) {
        return !s.getValue(LOOSE) && !s.getValue(POWERED);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        if (!level.isClientSide() && !old.is(state.getBlock())) level.scheduleTick(pos, this, PERIOD);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, net.minecraft.world.level.redstone.Orientation o, boolean moved) {
        if (level.isClientSide()) return;
        boolean p = level.hasNeighborSignal(pos);
        if (p != state.getValue(POWERED)) level.setBlock(pos, state.setValue(POWERED, p), 2);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            BlockState next = state.cycle(LOOSE);
            level.setBlock(pos, next, 3);
            player.sendOverlayMessage(Component.literal(next.getValue(LOOSE) ? "Let go: boats may leave." : "Made fast: boats within " + reach(level, pos) + " blocks are held."));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        String dim = level.dimension().identifier().toString();
        int r = reach(level, pos);
        AABB box = new AABB(pos).inflate(r, 2, r);
        if (holding(state)) {
            for (AbstractBoat boat : level.getEntitiesOfClass(AbstractBoat.class, box)) HELD.putIfAbsent(boat.getUUID(), new Held(pos, dim));
        }
        // let go of boats this bollard holds when it is loose, powered, or the boat has moved off
        HELD.entrySet().removeIf(e -> e.getValue().bollard().equals(pos) && e.getValue().dimension().equals(dim)
                && (!holding(state) || !(level.getEntity(e.getKey()) instanceof AbstractBoat b) || b.isRemoved() || !b.getBoundingBox().intersects(box.inflate(1))));
        level.scheduleTick(pos, this, PERIOD);
    }

    /** Called every tick for a boat: a held boat does not move sideways. */
    static void hold(AbstractBoat boat) {
        if (HELD.isEmpty() || !HELD.containsKey(boat.getUUID())) return;
        var v = boat.getDeltaMovement();
        boat.setDeltaMovement(0, v.y, 0);
    }
}
