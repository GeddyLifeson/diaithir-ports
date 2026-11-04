package net.diaithir.carts;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;

/**
 * Reads the cart on the rail directly above it and gives a redstone signal, so a circuit gate or reader can see vehicles:
 * 0 no cart, 1 a cart with no schedule, 2 to 13 a scheduled cart heading for stop number (signal - 2; 13 means stop 11 or any later one, since a signal has 16 levels), 14 a scheduled cart with no crew waiting for one,
 * 15 an autonomous cart (capable type with a module fitted).
 */
public final class CartReader extends Block {
    public static final int INTERVAL = 5;
    public static final IntegerProperty POWER = IntegerProperty.create("power", 0, 15);

    public CartReader(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(POWER, 0));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(POWER); }

    @Override protected boolean isSignalSource(BlockState state) { return true; }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction dir) {
        return dir == Direction.DOWN ? 0 : state.getValue(POWER);   // never power the rail above (a station rail would be released early)
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        if (!level.isClientSide() && !old.is(this)) level.scheduleTick(pos, this, 1);
    }

    /** The signal for the carts on the rail above pos (the strongest if several). */
    public static int read(Level level, BlockPos pos) {
        int best = 0;
        for (AbstractMinecart c : level.getEntitiesOfClass(AbstractMinecart.class, new AABB(pos.above()))) best = Math.max(best, signalOf(c));
        return best;
    }

    public static int signalOf(AbstractMinecart c) {
        if (Autonomy.autonomous(c)) return 15;
        Schedule s = Schedule.get(c);
        if (s == null) return 1;
        return Autonomy.mayRun(c) ? 2 + Math.min(s.next(), 11) : 14;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int p = read(level, pos);
        if (p != state.getValue(POWER)) level.setBlock(pos, state.setValue(POWER, p), 3);
        level.scheduleTick(pos, this, INTERVAL);
    }
}
