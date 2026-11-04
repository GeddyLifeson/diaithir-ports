package net.diaithir.crafting.foundry;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * The hook machines call to ask for a worker. A machine that needs an operator calls {@link #work} with the seconds of work a job takes
 * (the `s` of its progression node) and runs the job only when it returns true.
 */
public final class Operators {
    private Operators() {}

    /** Draws {@code seconds} of work from the Operator Seats within range of {@code origin}. All or nothing: false pays nothing. */
    public static boolean work(Level level, BlockPos origin, double seconds) {
        if (level.isClientSide() || seconds <= 0) return seconds <= 0;
        int r = Config.RANGE.get();
        double have = 0;
        java.util.List<OperatorSeatBlockEntity> seats = new java.util.ArrayList<>();
        for (BlockPos p : BlockPos.betweenClosed(origin.offset(-r, -r, -r), origin.offset(r, r, r))) {
            if (level.getBlockEntity(p) instanceof OperatorSeatBlockEntity be) {
                seats.add(be);
                have += be.reserveSeconds() + (double) be.food.count() * FoodStore.nutrition(be.food.resource()) * Config.SECONDS_PER_NUTRITION.get();
            }
        }
        if (have + 1.0E-9 < seconds) return false;
        double need = seconds;
        for (OperatorSeatBlockEntity be : seats) {
            if (need <= 1.0E-9) break;
            need = be.spend(need);
        }
        return need <= 1.0E-9;
    }

    /** True when at least one seat in range has food or work in hand. */
    public static boolean staffed(Level level, BlockPos origin) {
        int r = Config.RANGE.get();
        for (BlockPos p : BlockPos.betweenClosed(origin.offset(-r, -r, -r), origin.offset(r, r, r))) {
            if (level.getBlockEntity(p) instanceof OperatorSeatBlockEntity be && (be.reserveSeconds() > 0 || be.food.count() > 0)) return true;
        }
        return false;
    }
}
