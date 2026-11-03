package net.diaithir.automationcrafting.packages;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** What is still missing for a package or a research. Items are taken only up to what is missing. */
public final class Tally {
    private final Map<String, Integer> left = new LinkedHashMap<>();

    public Tally() {}

    public Tally(List<PackageDef.Req> reqs) {
        for (PackageDef.Req r : reqs) left.merge(r.item(), r.count(), Integer::sum);
    }

    public Tally(Map<String, Integer> saved) {
        left.putAll(saved);
    }

    /** Takes up to n of the item; returns how many were taken. */
    public int accept(String item, int n) {
        Integer need = left.get(item);
        if (need == null || n <= 0) return 0;
        int take = Math.min(need, n);
        if (take == need) left.remove(item);
        else left.put(item, need - take);
        return take;
    }

    public boolean wants(String item) {
        return left.containsKey(item);
    }

    public boolean done() {
        return left.isEmpty();
    }

    public Map<String, Integer> view() {
        return left;
    }

    public Tally copy() {
        return new Tally(left);
    }
}
