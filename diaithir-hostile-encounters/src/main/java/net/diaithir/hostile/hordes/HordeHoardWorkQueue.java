package net.diaithir.hostile.hordes;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Server-thread delayed-runnable queue. Equivalent of HordeHoardMod#queueServerWork
 * from the 1.20.1 source. The per-loader entry points pump this on every server tick.
 *
 * If you don't actually need delayed work in any of the ported procedures, this can be
 * deleted along with the per-loader server-tick subscriber that drains it. Source had it
 * because MCreator generates it for every mod regardless of usage.
 */
public class HordeHoardWorkQueue {

    private static final Collection<AbstractMap.SimpleEntry<Runnable, Integer>> WORK_QUEUE =
            new ConcurrentLinkedQueue<>();

    public static void queueServerWork(int tick, Runnable action) {
        WORK_QUEUE.add(new AbstractMap.SimpleEntry<>(action, tick));
    }

    public static void onServerTickEnd() {
        List<AbstractMap.SimpleEntry<Runnable, Integer>> ready = new ArrayList<>();
        WORK_QUEUE.forEach(work -> {
            work.setValue(work.getValue() - 1);
            if (work.getValue() == 0) {
                ready.add(work);
            }
        });
        ready.forEach(e -> e.getKey().run());
        WORK_QUEUE.removeAll(ready);
    }
}
