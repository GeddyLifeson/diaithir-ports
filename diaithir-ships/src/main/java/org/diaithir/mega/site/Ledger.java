package org.diaithir.mega.site;

import java.util.HashMap;
import java.util.Map;

/** Delivered bulk material in tonnes per c: tag. The depot owns one; the stage engine draws from it. No slots, no GUI. */
public final class Ledger {
    private final Map<String, Double> tonnes = new HashMap<>();

    public double get(String tag) { return tonnes.getOrDefault(tag, 0.0); }
    public void add(String tag, double t) { tonnes.merge(tag, t, Double::sum); }
    public void take(String tag, double t) { tonnes.merge(tag, -t, Double::sum); }
    public Map<String, Double> view() { return tonnes; }
    public void set(Map<String, Double> m) { tonnes.clear(); tonnes.putAll(m); }
}
