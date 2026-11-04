package net.diaithir.miners.oredetector.mixinutil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * One delayed action per player (replaces upstream's ServerPlayer mixin, so the jar needs no mixin).
 * Ticked from the server tick event.
 */
public final class DelayedExecute {
    private record Task(long due, Runnable action) {}

    private static final Map<UUID, Task> TASKS = new HashMap<>();
    private static long now;

    private DelayedExecute() {}

    public static void init() {
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post e) -> {
            now = e.getServer().overworld().getGameTime();
            var it = TASKS.entrySet().iterator();
            while (it.hasNext()) {
                var en = it.next();
                if (en.getValue().due() <= now) {
                    it.remove();
                    en.getValue().action().run();
                }
            }
        });
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.server.ServerStoppedEvent e) -> TASKS.clear());
    }

    public static boolean isRunning(Player p) {
        return TASKS.containsKey(p.getUUID());
    }

    /** Runs {@code action} after {@code delay} ticks; false if this player already has one waiting. */
    public static boolean delayedExecute(Player p, int delay, Runnable action) {
        if (TASKS.containsKey(p.getUUID())) return false;
        TASKS.put(p.getUUID(), new Task(now + delay, action));
        return true;
    }
}
