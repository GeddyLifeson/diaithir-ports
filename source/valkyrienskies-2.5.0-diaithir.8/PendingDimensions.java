package org.valkyrienskies.mod.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Levels created at runtime (dynamic dimensions) while the server is running must not be registered with
 * vs-core's ship world at an arbitrary point of the tick: vs-core enforces a stage order per tick
 * (PRE_TICK, then dimension/chunk updates, then POST_TICK...) and addDimension at the wrong time makes
 * postTickGame throw ("Constraints failed" / NPE) and crashes the server. Once the first tick has started,
 * new levels are queued and registered right after preTickGame of the next tick.
 */
public final class PendingDimensions {
    private static final Object LOCK = new Object();
    private static final Map<String, Runnable> PENDING = new LinkedHashMap<>();
    private static boolean started = false;

    private PendingDimensions() {}

    /** Run now (during startup), or queue until after the next preTickGame. */
    public static void registerOrDefer(final String dimensionId, final Runnable register) {
        synchronized (LOCK) {
            if (started) {
                PENDING.put(dimensionId, register);
                return;
            }
        }
        register.run();
    }

    public static boolean isPending(final String dimensionId) {
        synchronized (LOCK) {
            return PENDING.containsKey(dimensionId);
        }
    }

    /** Called right after preTickGame. */
    public static void drainAfterPreTick() {
        final Runnable[] toRun;
        synchronized (LOCK) {
            started = true;
            toRun = PENDING.values().toArray(new Runnable[0]);
            PENDING.clear();
        }
        for (final Runnable r : toRun) r.run();
    }

    public static void reset() {
        synchronized (LOCK) { PENDING.clear(); started = false; }
    }
}
