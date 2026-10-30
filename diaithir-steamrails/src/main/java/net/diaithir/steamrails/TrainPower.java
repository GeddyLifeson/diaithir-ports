package net.diaithir.steamrails;

import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * How steam turns into train speed. Every boiler on a steam carriage reports its thrust each tick; a mixin on Create's {@code Train} reads the
 * sum. A train that has reported recently is a steam train: its top speed, turning speed and acceleration come from pressure and cylinders, not
 * from Create's fuel timer. It cannot move without pressure.
 */
public final class TrainPower {
    private TrainPower() {}

    /** Ticks after the last report in which a train still counts as a steam train. */
    public static final long EXPIRE = 1200;
    /** Bar below which cylinders give nothing, and the pressure of one full unit of thrust (1 + this). */
    public static final float MIN_BAR = 1F;
    public static final float FULL_BAR = 6F;

    private static final class Entry {
        long tick = -1;
        double cur;
        double prev;
        int count;
    }

    private static final Map<UUID, Entry> ENTRIES = new HashMap<>();
    private static long now;

    public static void tick() {
        now++;
    }

    /** Thrust of one boiler: its useful cylinders times how far pressure is above the minimum, in units of the full-speed pressure. */
    public static double thrust(float pressure, int cylinders, int size) {
        int eff = Math.min(cylinders, (size + 1) / 2);
        double f = Math.max(0, (pressure - MIN_BAR) / (FULL_BAR - MIN_BAR));
        return eff * Math.min(3.0, f);
    }

    public static void report(UUID train, double thrust) {
        Entry e = ENTRIES.computeIfAbsent(train, k -> new Entry());
        if (e.tick != now) {
            e.prev = e.tick == now - 1 ? e.cur : 0;
            e.cur = 0;
            e.count = 0;
            e.tick = now;
        }
        e.cur += thrust;
        e.count++;
    }

    public static boolean isSteamTrain(UUID train) {
        Entry e = ENTRIES.get(train);
        return e != null && now - e.tick <= EXPIRE;
    }

    /** Thrust of the whole train as last settled. */
    public static double thrustOf(UUID train) {
        Entry e = ENTRIES.get(train);
        if (e == null) return 0;
        return e.tick == now ? e.prev : (now - e.tick == 1 ? e.cur : 0);
    }

    /** Pulling power against the weight of the train, 0 for nothing, 1 for full speed, up to 1.5 for spare power. */
    public static double ratio(double thrust, int carriages) {
        double drag = 0.4 + 0.3 * Math.max(1, carriages);
        return Math.max(0, Math.min(1.5, thrust / drag));
    }

    public static float maxSpeed(Train t, float orig) {
        if (!isSteamTrain(t.id)) return orig;
        double r = ratio(thrustOf(t.id), t.carriages.size());
        float top = AllConfigs.server().trains.poweredTrainTopSpeed.getF() / 20F;
        return (float) (top * Math.sqrt(Math.min(1, r)));
    }

    public static float maxTurnSpeed(Train t, float orig) {
        if (!isSteamTrain(t.id)) return orig;
        double r = ratio(thrustOf(t.id), t.carriages.size());
        float top = AllConfigs.server().trains.poweredTrainTurningTopSpeed.getF() / 20F;
        return (float) (top * Math.sqrt(Math.min(1, r)));
    }

    public static float acceleration(Train t, float orig) {
        if (!isSteamTrain(t.id)) return orig;
        double r = ratio(thrustOf(t.id), t.carriages.size());
        float base = AllConfigs.server().trains.poweredTrainAcceleration.getF() / 400F;
        return (float) (base * Math.max(0.25, r));
    }
}
