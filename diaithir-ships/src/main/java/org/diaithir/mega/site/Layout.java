package org.diaithir.mega.site;

import org.diaithir.mega.site.Blueprints.Kind;
import org.diaithir.mega.site.Blueprints.Stage;
import org.diaithir.mega.site.Blueprints.Structure;

/**
 * Maps the i-th block of the whole structure to an offset from the site origin. Dig stages carve a pit below the origin;
 * place stages fill a square layer after layer upward, each stage on top of the last. Sky-scale parts stay data (remote stages have 0 blocks here
 * only when their block share is 0; otherwise they continue the column).
 */
public final class Layout {
    public record Off(int x, int y, int z, boolean carve) {}

    private Layout() {}

    public static Off at(Structure st, int index) {
        int base = 0, top = 0, depth = 0;
        for (Stage s : st.stages()) {
            int side = side(s);
            int layers = (s.blocks() + side * side - 1) / (side * side);
            if (index < base + s.blocks()) {
                int i = index - base;
                int x = i % side - side / 2, z = (i / side) % side - side / 2, layer = i / (side * side);
                if (s.kind() == Kind.DIG) return new Off(x, -1 - depth - layer, z, true);
                return new Off(x, top + layer, z, false);
            }
            base += s.blocks();
            if (s.kind() == Kind.DIG) depth += layers; else top += layers;
        }
        throw new IllegalArgumentException("block index " + index);
    }

    private static int side(Stage s) { return Math.max(1, Math.min(24, (int) Math.ceil(Math.sqrt(s.blocks())))); }
}
