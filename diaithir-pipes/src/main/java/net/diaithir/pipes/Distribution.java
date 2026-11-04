package net.diaithir.pipes;

import java.util.ArrayList;
import java.util.List;

/** Pure ordering logic for the insert targets of one extract face. */
public enum Distribution {
    ROUND_ROBIN, NEAREST;

    public Distribution next() {
        return this == ROUND_ROBIN ? NEAREST : ROUND_ROBIN;
    }

    public static Distribution byOrdinal(int i) {
        return i == 1 ? NEAREST : ROUND_ROBIN;
    }

    /** Orders target distances: nearest sorts ascending; round robin rotates the list so the target after the last served goes first. Returns indices into the input. */
    public static List<Integer> order(Distribution mode, int[] distances, int cursor) {
        int n = distances.length;
        List<Integer> idx = new ArrayList<>(n);
        if (n == 0) return idx;
        if (mode == NEAREST) {
            for (int i = 0; i < n; i++) idx.add(i);
            idx.sort((a, b) -> distances[a] != distances[b] ? Integer.compare(distances[a], distances[b]) : Integer.compare(a, b));
        } else {
            int start = Math.floorMod(cursor, n);
            for (int i = 0; i < n; i++) idx.add((start + i) % n);
        }
        return idx;
    }
}
