package net.diaithir.unrest;

import java.util.HashMap;
import java.util.Map;

/** The Unrest score of every chunk in one level. Pure logic (no Minecraft types) so it can be tested alone. Keys are packed chunk coordinates. */
public final class Field {
    private final Map<Long, Double> scores = new HashMap<>();

    public static long key(int cx, int cz) {
        return ((long) cx << 32) | (cz & 0xFFFFFFFFL);
    }

    public static int cx(long key) {
        return (int) (key >> 32);
    }

    public static int cz(long key) {
        return (int) key;
    }

    public double get(int cx, int cz) {
        return scores.getOrDefault(key(cx, cz), 0.0);
    }

    public void add(int cx, int cz, double amount) {
        if (amount == 0) return;
        double v = Math.max(0.0, get(cx, cz) + amount);
        if (v <= 0) scores.remove(key(cx, cz));
        else scores.put(key(cx, cz), v);
    }

    /** Takes up to amount out of a chunk and returns what was really taken. */
    public double take(int cx, int cz, double amount) {
        double have = get(cx, cz);
        double got = Math.min(have, Math.max(0, amount));
        add(cx, cz, -got);
        return got;
    }

    public int size() {
        return scores.size();
    }

    public double total() {
        double t = 0;
        for (double v : scores.values()) t += v;
        return t;
    }

    public Map<Long, Double> raw() {
        return scores;
    }

    /**
     * One step: every chunk gives spread (a fraction) of its score to its four neighbours in equal parts, then everything
     * decays by decay (a fraction) and scores under floor are dropped. Spreading alone keeps the total; decay shrinks it.
     */
    public void step(double spread, double decay, double floor) {
        Map<Long, Double> next = new HashMap<>();
        for (Map.Entry<Long, Double> e : scores.entrySet()) {
            int x = cx(e.getKey()), z = cz(e.getKey());
            double v = e.getValue();
            double out = v * spread;
            next.merge(e.getKey(), v - out, Double::sum);
            double q = out / 4.0;
            next.merge(key(x + 1, z), q, Double::sum);
            next.merge(key(x - 1, z), q, Double::sum);
            next.merge(key(x, z + 1), q, Double::sum);
            next.merge(key(x, z - 1), q, Double::sum);
        }
        scores.clear();
        for (Map.Entry<Long, Double> e : next.entrySet()) {
            double v = e.getValue() * (1.0 - decay);
            if (v >= floor) scores.put(e.getKey(), v);
        }
    }
}
