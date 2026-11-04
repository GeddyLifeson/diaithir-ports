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

    /** Computers joined to this one through connected cables, at any distance. */
    static Set<ComputerEntity> wired(ComputerEntity from) {
        Set<ComputerEntity> out = new HashSet<>();
        net.minecraft.world.level.Level level = from.getLevel();
        java.util.ArrayDeque<net.minecraft.core.BlockPos> todo = new java.util.ArrayDeque<>();
        Set<net.minecraft.core.BlockPos> seen = new HashSet<>();
        net.minecraft.core.BlockPos origin = from.getBlockPos();
        for (net.minecraft.core.Direction d : net.minecraft.core.Direction.values()) {
            net.minecraft.core.BlockPos n = origin.relative(d);
            if (level.getBlockState(n).is(Registry.CABLE.get()) && seen.add(n)) todo.add(n);
        }
        while (!todo.isEmpty() && seen.size() <= MAX_CABLES) {
            net.minecraft.core.BlockPos c = todo.poll();
            for (net.minecraft.core.Direction d : net.minecraft.core.Direction.values()) {
                net.minecraft.core.BlockPos n = c.relative(d);
                if (level.getBlockState(n).is(Registry.CABLE.get())) { if (seen.add(n)) todo.add(n); }
                else if (level.getBlockEntity(n) instanceof ComputerEntity ce && ce != from) out.add(ce);
            }
        }
        return out;
    }

    static final int MAX_CABLES = 512;

    /** Returns how many computers got it: those within RANGE blocks, and those on the same cable network at any distance. */
    static int send(ComputerEntity from, int channel, String message) {
        Set<ComputerEntity> s = NODES.get(from.getLevel().dimension());
        Set<ComputerEntity> targets = new HashSet<>(wired(from));
        if (s != null) for (ComputerEntity to : s) {
            if (to != from && Math.sqrt(to.getBlockPos().distSqr(from.getBlockPos())) <= RANGE) targets.add(to);
        }
        String msg = message.length() > MAX_CHARS ? message.substring(0, MAX_CHARS) : message;
        int n = 0;
        for (ComputerEntity to : targets) {
            if (!to.listening.contains(channel) || to.getLevel() != from.getLevel()) continue;
            double d = Math.sqrt(to.getBlockPos().distSqr(from.getBlockPos()));
            if (to.inbox.size() < INBOX) { to.inbox.add(new Msg(channel, msg, (int) Math.round(d))); n++; }
        }
        return n;
    }

    record Msg(int channel, String text, int distance) {}

    static void clear() { NODES.clear(); }

    private Net() {}
}
