package com.github.alexmodguy.alexscaves.server.level.map;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Replacement for Forge's WorldWorkerManager: runs cave map biome searches on the server thread with a per-tick time budget. */
@EventBusSubscriber(modid = AlexsCaves.MODID)
public class CaveMapWorkers {
    private static final List<CaveBiomeMapWorldWorker> WORKERS = new ArrayList<>();
    private static final long BUDGET_NANOS = 5_000_000L;

    public static void add(CaveBiomeMapWorldWorker worker) {
        WORKERS.add(worker);
    }

    public static boolean isActive() {
        return !WORKERS.isEmpty();
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (WORKERS.isEmpty()) return;
        long end = System.nanoTime() + BUDGET_NANOS;
        Iterator<CaveBiomeMapWorldWorker> it = WORKERS.iterator();
        while (it.hasNext()) {
            CaveBiomeMapWorldWorker worker = it.next();
            boolean more = true;
            while (more && System.nanoTime() < end) {
                more = worker.hasWork() && worker.doWork();
            }
            if (!more) it.remove();
            if (System.nanoTime() >= end) break;
        }
    }
}
