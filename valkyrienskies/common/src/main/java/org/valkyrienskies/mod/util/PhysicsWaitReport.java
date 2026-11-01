package org.valkyrienskies.mod.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * With synchronizePhysics the game thread blocks in postTickGame until the Physics thread has run physicsTicksPerGameTick
 * steps. A daemon watcher notices a wait that lasts too long and logs where the Physics thread is while the wait is
 * still going on, so a slow tick can be told from a stuck one (at most once per 30 seconds).
 */
public final class PhysicsWaitReport {
    private static final Logger LOGGER = LogManager.getLogger("VS2");
    private static final long THRESHOLD_NANOS = 1_500_000_000L;
    private static final long MIN_GAP_NANOS = 30_000_000_000L;
    private static volatile long waitStart = 0L;
    private static volatile boolean reported = false;
    private static long lastReport = System.nanoTime() - MIN_GAP_NANOS;
    private static boolean started = false;

    private PhysicsWaitReport() {
    }

    public static void begin() {
        if (!started) {
            started = true;
            final Thread t = new Thread(PhysicsWaitReport::watch, "VS physics wait watcher");
            t.setDaemon(true);
            t.start();
        }
        reported = false;
        waitStart = System.nanoTime();
    }

    public static void end() {
        final long start = waitStart;
        waitStart = 0L;
        if (reported && start != 0L) {
            LOGGER.warn("VS2: the game thread waited {} ms for the physics thread in postTickGame",
                (System.nanoTime() - start) / 1_000_000L);
        }
    }

    private static void watch() {
        while (true) {
            try {
                Thread.sleep(500L);
            } catch (final InterruptedException e) {
                return;
            }
            final long start = waitStart;
            final long now = System.nanoTime();
            if (start == 0L || reported || now - start < THRESHOLD_NANOS || now - lastReport < MIN_GAP_NANOS) {
                continue;
            }
            reported = true;
            lastReport = now;
            final StringBuilder sb = new StringBuilder("VS2: the game thread has waited ").append((now - start) / 1_000_000L)
                .append(" ms for the physics thread in postTickGame. Physics thread: ");
            boolean found = false;
            for (final var entry : Thread.getAllStackTraces().entrySet()) {
                if ("Physics thread".equals(entry.getKey().getName())) {
                    found = true;
                    sb.append(entry.getKey().getState());
                    int n = 0;
                    for (final StackTraceElement e : entry.getValue()) {
                        sb.append("\n    at ").append(e);
                        if (++n >= 16) {
                            break;
                        }
                    }
                }
            }
            if (!found) {
                sb.append("not running");
            }
            LOGGER.warn(sb.toString());
        }
    }
}
