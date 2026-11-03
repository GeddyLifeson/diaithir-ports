package net.diaithir.wiretech.circuit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A whole circuit without a world: nodes with logic, wires between their ports, stepped one update at a time. The block entities use the same
 * steps with real positions as node ids, so what passes here is what runs in game.
 */
public final class Sim {
    public static final class Node {
        public final long id;
        public Logic logic;
        public Tier tier;
        public Signals out = new Signals();

        Node(long id, Logic logic, Tier tier) {
            this.id = id;
            this.logic = logic;
            this.tier = tier;
        }
    }

    public final Map<Long, Node> nodes = new HashMap<>();
    public final List<Net.Link> links = new ArrayList<>();
    public long tick;

    public Node node(long id) {
        return nodes.get(id);
    }

    public Node add(long id, Tier tier, String program) {
        Node n = new Node(id, Logic.parse(program), tier);
        nodes.put(id, n);
        return n;
    }

    /** Joins output or input ports of two nodes with a wire of the given colour. */
    public void wire(long a, boolean aOut, long b, boolean bOut, Color c) {
        links.add(new Net.Link(new Net.Terminal(a, aOut, c), new Net.Terminal(b, bOut, c)));
    }

    public In input(Node n, Map<Net.Terminal, Signals> solved) {
        return new In(solved.getOrDefault(new Net.Terminal(n.id, false, Color.RED), Signals.EMPTY),
                solved.getOrDefault(new Net.Terminal(n.id, false, Color.GREEN), Signals.EMPTY));
    }

    /** Reads what the network shows at one node's input terminal right now. */
    public In seen(long id) {
        return input(nodes.get(id), solve());
    }

    private Map<Net.Terminal, Signals> solve() {
        Map<Net.Terminal, Signals> outs = new HashMap<>();
        for (Node n : nodes.values()) {
            outs.put(new Net.Terminal(n.id, true, Color.RED), n.out);
            outs.put(new Net.Terminal(n.id, true, Color.GREEN), n.out);
        }
        return Net.solve(links, outs);
    }

    /** One game tick. Every node whose tier period divides the tick computes from the networks as they were, then all outputs change together. */
    public void step() {
        Map<Net.Terminal, Signals> solved = solve();
        Map<Long, Signals> next = new HashMap<>();
        for (Node n : nodes.values()) {
            if (tick % n.tier.period != 0) continue;
            Signals o = n.logic.output(input(n, solved), new Logic.Ctx(tick, n.id, id -> 64)).limited(n.tier.maxValue, n.tier.maxTypes);
            next.put(n.id, o);
        }
        next.forEach((id, o) -> nodes.get(id).out = o);
        tick++;
    }

    public void run(int ticks) {
        for (int i = 0; i < ticks; i++) step();
    }
}
