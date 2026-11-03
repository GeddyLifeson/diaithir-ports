package net.diaithir.computing;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Wireless messages between computers in one dimension: a message on a channel reaches every other listening computer within RANGE blocks. Server thread only. */
final class Net {
    static final int RANGE = 64, INBOX = 32, MAX_CHARS = 256;
    private static final Map<ResourceKey<Level>, Set<ComputerEntity>> NODES = new HashMap<>();

    static void add(ComputerEntity e) { NODES.computeIfAbsent(e.getLevel().dimension(), k -> new HashSet<>()).add(e); }

    static void remove(ComputerEntity e) {
        if (e.getLevel() == null) return;
        Set<ComputerEntity> s = NODES.get(e.getLevel().dimension());
        if (s != null) s.remove(e);
    }

    /** Returns how many computers got it. */
    static int send(ComputerEntity from, int channel, String message) {
        Set<ComputerEntity> s = NODES.get(from.getLevel().dimension());
        if (s == null) return 0;
        String msg = message.length() > MAX_CHARS ? message.substring(0, MAX_CHARS) : message;
        int n = 0;
        for (ComputerEntity to : s) {
            if (to == from || !to.listening.contains(channel)) continue;
            double d = Math.sqrt(to.getBlockPos().distSqr(from.getBlockPos()));
            if (d > RANGE) continue;
            if (to.inbox.size() < INBOX) { to.inbox.add(new Msg(channel, msg, (int) Math.round(d))); n++; }
        }
        return n;
    }

    record Msg(int channel, String text, int distance) {}

    static void clear() { NODES.clear(); }

    private Net() {}
}
