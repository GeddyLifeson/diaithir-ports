package net.diaithir.wiretech.circuit;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/** A bag of signal id to 32-bit value. Zero entries are dropped, sums saturate like a signed 32-bit counter would not, so overflow never wraps. */
public final class Signals {
    public static final Signals EMPTY = new Signals();

    private final Map<String, Integer> m = new TreeMap<>();

    public int get(String id) {
        return m.getOrDefault(id, 0);
    }

    public Signals add(String id, long v) {
        long r = (long) get(id) + v;
        return set(id, r);
    }

    public Signals set(String id, long v) {
        int c = (int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, v));
        if (c == 0) m.remove(id);
        else m.put(id, c);
        return this;
    }

    public Signals addAll(Signals o) {
        for (Map.Entry<String, Integer> e : o.m.entrySet()) add(e.getKey(), e.getValue());
        return this;
    }

    public Map<String, Integer> entries() {
        return Collections.unmodifiableMap(m);
    }

    public boolean isEmpty() {
        return m.isEmpty();
    }

    public int size() {
        return m.size();
    }

    public Signals copy() {
        return new Signals().addAll(this);
    }

    /** Clamps every value to +-max and keeps at most maxTypes signals (largest magnitudes first). */
    public Signals limited(int max, int maxTypes) {
        Signals r = new Signals();
        m.entrySet().stream().sorted((a, b) -> Integer.compare(Math.abs(b.getValue()), Math.abs(a.getValue())))
                .limit(maxTypes).forEach(e -> r.set(e.getKey(), Math.max(-max, Math.min(max, e.getValue()))));
        return r;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Signals s && s.m.equals(m);
    }

    @Override
    public int hashCode() {
        return m.hashCode();
    }

    @Override
    public String toString() {
        return m.toString();
    }
}
