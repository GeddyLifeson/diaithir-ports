package net.diaithir.freight;

import java.util.ArrayList;
import java.util.List;

/**
 * A space that holds unit loads at cell positions. This is the one model every freight system shares: a container interior, a trailer bed, a flatcar,
 * a ship hold, a depot floor. Capacity is the space (cells) and the payload limit, never a slot count. Loads stay where they were put; the bay is plain
 * data, so it travels with whatever carries it. Plain Java, no Minecraft types.
 */
public final class CargoBay {
    public enum Result { OK, UNKNOWN_TYPE, AGE_LOCKED, OUT_OF_BAY, OVERLAP, UNSUPPORTED, OVER_PAYLOAD, OVER_STACK }

    /** A load standing in the bay. {@code turned} swaps its width and length. {@code payloadKg} is what it carries; {@code inside} is the bay of a container. */
    public static final class Placed {
        public final LoadType type;
        public final int x, y, z;
        public final boolean turned;
        public double payloadKg;
        public final CargoBay inside;
        /** Opaque to the bay: the glue keeps a loose load's item slots here. */
        public Object data;

        Placed(LoadType type, int x, int y, int z, boolean turned, double payloadKg) {
            this.type = type;
            this.x = x; this.y = y; this.z = z;
            this.turned = turned;
            this.payloadKg = payloadKg;
            this.inside = type.container() ? new CargoBay(type.inner()[0], type.inner()[1], type.inner()[2], type.payloadKg()) : null;
        }

        public int w() { return turned ? type.l() : type.w(); }
        public int l() { return turned ? type.w() : type.l(); }
        public int h() { return type.h(); }

        /** Tare plus own payload plus everything in a container's bay. */
        public double grossKg() { return type.tareKg() + (inside != null ? inside.payloadKg() : payloadKg); }

        boolean covers(int cx, int cy, int cz) {
            return cx >= x && cx < x + w() && cy >= y && cy < y + h() && cz >= z && cz < z + l();
        }
    }

    public final int w, h, l;
    /** Most net mass the bay may hold (a deck's rating, a container's payload). */
    public final double maxPayloadKg;
    private final List<Placed> loads = new ArrayList<>();

    public CargoBay(int w, int h, int l, double maxPayloadKg) {
        this.w = w; this.h = h; this.l = l;
        this.maxPayloadKg = maxPayloadKg;
    }

    public List<Placed> loads() { return List.copyOf(loads); }

    public int cells() { return w * h * l; }

    public int usedCells() {
        int n = 0;
        for (Placed p : loads) n += p.type.cells();
        return n;
    }

    public int freeCells() { return cells() - usedCells(); }

    /** Gross mass of every load in the bay (what the carrier must bear). */
    public double payloadKg() {
        double m = 0;
        for (Placed p : loads) m += p.grossKg();
        return m;
    }

    public Placed at(int x, int y, int z) {
        for (Placed p : loads) if (p.covers(x, y, z)) return p;
        return null;
    }

    /** Checks every rule; {@code age} is the server's current Age and {@code payloadKg} what the new load carries. */
    public Result check(LoadType t, boolean turned, int x, int y, int z, double payloadKg, int age) {
        if (t == null) return Result.UNKNOWN_TYPE;
        if (age < t.minAge()) return Result.AGE_LOCKED;
        int lw = turned ? t.l() : t.w(), ll = turned ? t.w() : t.l();
        if (x < 0 || y < 0 || z < 0 || x + lw > w || y + t.h() > h || z + ll > l) return Result.OUT_OF_BAY;
        for (int cx = x; cx < x + lw; cx++)
            for (int cy = y; cy < y + t.h(); cy++)
                for (int cz = z; cz < z + ll; cz++)
                    if (at(cx, cy, cz) != null) return Result.OVERLAP;
        double gross = t.tareKg() + payloadKg;
        if (payloadKg > t.payloadKg() || payloadKg < 0 || payloadKg() + gross > maxPayloadKg) return Result.OVER_PAYLOAD;
        if (y > 0) {
            List<Placed> under = new ArrayList<>();
            for (int cx = x; cx < x + lw; cx++)
                for (int cz = z; cz < z + ll; cz++) {
                    Placed s = at(cx, y - 1, cz);
                    if (s == null) return Result.UNSUPPORTED;
                    if (!under.contains(s)) under.add(s);
                }
            for (Placed s : under) if (!bears(s, gross / under.size())) return Result.OVER_STACK;
        }
        return Result.OK;
    }

    public Result place(LoadType t, boolean turned, int x, int y, int z, double payloadKg, int age) {
        Result r = check(t, turned, x, y, z, payloadKg, age);
        if (r == Result.OK) loads.add(new Placed(t, x, y, z, turned, payloadKg));
        return r;
    }

    /** True when a load, and everything it stands on, can take {@code extra} kg more on top. */
    private boolean bears(Placed s, double extra) {
        if (loadOn(s) + extra > s.type.stackKg()) return false;
        if (s.y == 0) return true;
        List<Placed> under = new ArrayList<>();
        for (int cx = s.x; cx < s.x + s.w(); cx++)
            for (int cz = s.z; cz < s.z + s.l(); cz++) {
                Placed u = at(cx, s.y - 1, cz);
                if (u != null && !under.contains(u)) under.add(u);
            }
        for (Placed u : under) if (!bears(u, extra / under.size())) return false;
        return true;
    }

    /** Puts a load back where it already stood (a world scan, a saved item): bounds and overlap only, no support or weight rule. */
    public Placed restore(LoadType t, boolean turned, int x, int y, int z, double payloadKg) {
        int lw = turned ? t.l() : t.w(), ll = turned ? t.w() : t.l();
        if (x < 0 || y < 0 || z < 0 || x + lw > w || y + t.h() > h || z + ll > l) return null;
        for (int cx = x; cx < x + lw; cx++)
            for (int cy = y; cy < y + t.h(); cy++)
                for (int cz = z; cz < z + ll; cz++) if (at(cx, cy, cz) != null) return null;
        Placed p = new Placed(t, x, y, z, turned, payloadKg);
        loads.add(p);
        return p;
    }

    /** Gross mass resting on top of a load, counting loads stacked on those too. */
    public double loadOn(Placed p) {
        double m = 0;
        for (Placed o : loads) if (o != p && o.y == p.y + p.h() && overlaps(o, p)) m += o.grossKg() + loadOn(o);
        return m;
    }

    private static boolean overlaps(Placed a, Placed b) {
        return a.x < b.x + b.w() && b.x < a.x + a.w() && a.z < b.z + b.l() && b.z < a.z + a.l();
    }

    /** A load can leave when nothing stands on it. */
    public boolean canTake(Placed p) {
        for (Placed o : loads) if (o != p && o.y == p.y + p.h() && overlaps(o, p)) return false;
        return loads.contains(p);
    }

    public boolean take(Placed p) { return canTake(p) && loads.remove(p); }
}
