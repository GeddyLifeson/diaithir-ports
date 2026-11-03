package net.diaithir.stations;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/** Steam power for stations: jobs draw joules from hand-fed boilers within range. All or nothing. */
public final class Boilers {
    private Boilers() {}

    public static final int RANGE = 8;

    private static List<BoilerBlockEntity> near(Level level, BlockPos origin) {
        List<BoilerBlockEntity> out = new ArrayList<>();
        for (BlockPos p : BlockPos.betweenClosed(origin.offset(-RANGE, -RANGE, -RANGE), origin.offset(RANGE, RANGE, RANGE))) {
            if (level.getBlockEntity(p) instanceof BoilerBlockEntity be) out.add(be);
        }
        return out;
    }

    /** Joules the boilers in range could supply now: stored plus what their fuel would burn to. */
    public static double available(Level level, BlockPos origin) {
        double j = 0;
        for (BoilerBlockEntity be : near(level, origin)) j += be.potentialJoules();
        return j;
    }

    /** Draws joules from the boilers in range, burning fuel as needed. False pays nothing. */
    public static boolean draw(Level level, BlockPos origin, double joules) {
        if (level.isClientSide() || joules <= 0) return joules <= 0;
        List<BoilerBlockEntity> bs = near(level, origin);
        double have = 0;
        for (BoilerBlockEntity be : bs) have += be.potentialJoules();
        if (have + 1.0E-6 < joules) return false;
        double need = joules;
        for (BoilerBlockEntity be : bs) {
            if (need <= 1.0E-6) break;
            need = be.spend(need);
        }
        return need <= 1.0E-6;
    }
}
