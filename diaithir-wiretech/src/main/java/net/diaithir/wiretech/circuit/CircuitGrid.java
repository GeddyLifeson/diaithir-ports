package net.diaithir.wiretech.circuit;

import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/** Steps every loaded circuit node of a level together, with the same rules as {@link Sim}: nodes read the networks as they were, then all outputs change at once. */
public final class CircuitGrid {
    private CircuitGrid() {}

    private static final Map<ServerLevel, Set<CircuitNodeBE>> NODES = new WeakHashMap<>();

    static void add(ServerLevel level, CircuitNodeBE be) {
        NODES.computeIfAbsent(level, l -> new HashSet<>()).add(be);
    }

    static void remove(ServerLevel level, CircuitNodeBE be) {
        Set<CircuitNodeBE> s = NODES.get(level);
        if (s != null) s.remove(be);
    }

    public static int size(ServerLevel level) {
        return NODES.getOrDefault(level, Set.of()).size();
    }

    public static void tick(ServerLevel level) {
        Set<CircuitNodeBE> set = NODES.get(level);
        if (set == null || set.isEmpty()) return;
        long gt = level.getGameTime();
        List<CircuitNodeBE> nodes = new ArrayList<>(set);
        List<Net.Link> links = new ArrayList<>();
        Map<Net.Terminal, Signals> outs = new HashMap<>();
        for (CircuitNodeBE n : nodes) {
            if (n.kind.hasOutput) {
                outs.put(new Net.Terminal(n.id(), true, Color.RED), n.out);
                outs.put(new Net.Terminal(n.id(), true, Color.GREEN), n.out);
            }
            for (CircuitNodeBE.Link l : n.links()) {
                links.add(new Net.Link(new Net.Terminal(n.id(), l.selfOut(), l.color()), new Net.Terminal(l.other().asLong(), l.otherOut(), l.color())));
            }
        }
        Map<Net.Terminal, Signals> solved = Net.solve(links, outs);
        Map<CircuitNodeBE, Signals> next = new HashMap<>();
        for (CircuitNodeBE n : nodes) {
            if (gt % n.tier.period != 0) continue;
            In in = new In(solved.getOrDefault(new Net.Terminal(n.id(), false, Color.RED), Signals.EMPTY),
                    solved.getOrDefault(new Net.Terminal(n.id(), false, Color.GREEN), Signals.EMPTY));
            next.put(n, n.compute(level, in, gt).limited(n.tier.maxValue, n.tier.maxTypes));
        }
        next.forEach((n, o) -> n.out = o);
    }
}
