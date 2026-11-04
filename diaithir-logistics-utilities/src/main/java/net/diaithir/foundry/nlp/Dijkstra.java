// SPDX-License-Identifier: LGPL-3.0-only
// Design derived from Not Logistics Pipes (c) Contributors to Not Logistics Pipes <https://nlp.rs485.network>, reimplemented in Java for NeoForge 26.1.2 (Diaithir).
package net.diaithir.foundry.nlp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.LongFunction;

/** Shortest paths over the pipe graph from one origin. Edge cost is one block. */
public final class Dijkstra {
    private final long origin;
    private final Map<Long, Integer> dist = new HashMap<>();
    private final Map<Long, Long> prev = new HashMap<>();

    private Dijkstra(long origin) {
        this.origin = origin;
    }

    /** Explores at most limit nodes; neighbours gives the pipes connected to a position. */
    public static Dijkstra discover(long origin, LongFunction<List<Long>> neighbours, int limit) {
        Dijkstra d = new Dijkstra(origin);
        d.dist.put(origin, 0);
        PriorityQueue<long[]> queue = new PriorityQueue<>((a, b) -> Long.compare(a[1], b[1]));
        queue.add(new long[] {origin, 0});
        int settled = 0;
        while (!queue.isEmpty() && settled < limit) {
            long[] cur = queue.poll();
            if (cur[1] > d.dist.getOrDefault(cur[0], Integer.MAX_VALUE)) continue;
            settled++;
            for (long n : neighbours.apply(cur[0])) {
                int nd = (int) cur[1] + 1;
                if (nd < d.dist.getOrDefault(n, Integer.MAX_VALUE)) {
                    d.dist.put(n, nd);
                    d.prev.put(n, cur[0]);
                    queue.add(new long[] {n, nd});
                }
            }
        }
        return d;
    }

    public boolean reaches(long node) {
        return dist.containsKey(node);
    }

    public int distance(long node) {
        return dist.getOrDefault(node, Integer.MAX_VALUE);
    }

    public Map<Long, Integer> distances() {
        return dist;
    }

    /** Positions from the origin to target inclusive, or empty if target was not reached. */
    public List<Long> route(long target) {
        if (!dist.containsKey(target)) return List.of();
        List<Long> path = new ArrayList<>();
        for (Long at = target; at != null; at = prev.get(at)) path.add(at);
        Collections.reverse(path);
        return path.get(0) == origin ? path : List.of();
    }
}
