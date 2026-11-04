package net.diaithir.wiretech.circuit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Solves the wire networks of one level. A terminal is one colour of one port (input or output) of one node. A link joins two terminals
 * into one network. The signals on a network are the sum of every output terminal in it. An input terminal reads its network, so a node never
 * sees its own output unless a wire closes the loop, and then it sees it one update later.
 */
public final class Net {
    private Net() {}

    public record Terminal(long node, boolean output, Color color) {}

    public record Link(Terminal a, Terminal b) {}

    /** The signals on each linked terminal. Terminals with no link are not in the result (their network is just themselves). */
    public static Map<Terminal, Signals> solve(List<Link> links, Map<Terminal, Signals> outputs) {
        Map<Terminal, Terminal> parent = new HashMap<>();
        for (Link l : links) union(parent, l.a(), l.b());
        Map<Terminal, Signals> sums = new HashMap<>();
        for (Terminal t : parent.keySet()) {
            Signals out = t.output() ? outputs.get(t) : null;
            if (out != null) sums.computeIfAbsent(find(parent, t), k -> new Signals()).addAll(out);
        }
        Map<Terminal, Signals> r = new HashMap<>();
        for (Terminal t : parent.keySet()) r.put(t, sums.getOrDefault(find(parent, t), Signals.EMPTY));
        return r;
    }

    private static Terminal find(Map<Terminal, Terminal> p, Terminal t) {
        p.putIfAbsent(t, t);
        Terminal r = t;
        while (!p.get(r).equals(r)) r = p.get(r);
        while (!p.get(t).equals(r)) {
            Terminal n = p.get(t);
            p.put(t, r);
            t = n;
        }
        return r;
    }

    private static void union(Map<Terminal, Terminal> p, Terminal a, Terminal b) {
        Terminal ra = find(p, a), rb = find(p, b);
        if (!ra.equals(rb)) p.put(ra, rb);
    }
}
