package net.diaithir.forcefield;

/**
 * Pure integer geometry for the three shell shapes. Offsets are relative to the projector block. No game classes, so it is unit tested.
 * A cube and a sphere are hollow shells around the projector. A wall is a flat slab in front of the projector (facing is a horizontal
 * unit normal nx, nz) that spans 2r+1 blocks sideways and up and down.
 */
public final class Geometry {
    private Geometry() {}

    /** Wall distance in blocks between the projector and the first layer. */
    public static final int WALL_GAP = 2;

    public enum Shape {
        CUBE, SPHERE, WALL;

        public Shape next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    /** True when the offset is a block of the shell (r = outer radius, t = layers). */
    public static boolean inShell(Shape shape, int dx, int dy, int dz, int r, int t, int nx, int nz) {
        switch (shape) {
            case CUBE: {
                int d = Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));
                return d <= r && d > r - t;
            }
            case SPHERE: {
                double d = Math.sqrt((double) dx * dx + (double) dy * dy + (double) dz * dz);
                return d <= r + 0.5 && d > r + 0.5 - t;
            }
            default: {
                int along = dx * nx + dz * nz;
                int side = Math.abs(dx * nz - dz * nx);
                return along >= WALL_GAP && along < WALL_GAP + t && side <= r && Math.abs(dy) <= r;
            }
        }
    }

    /** Extent of the shell around the projector, in blocks, on each axis (min then max): {x0, y0, z0, x1, y1, z1}. */
    public static int[] bounds(Shape shape, int r, int t, int nx, int nz) {
        if (shape != Shape.WALL) return new int[] {-r - 1, -r - 1, -r - 1, r + 1, r + 1, r + 1};
        int far = WALL_GAP + t;
        int x0 = nx != 0 ? Math.min(0, nx * far) : -r;
        int x1 = nx != 0 ? Math.max(0, nx * far) : r;
        int z0 = nz != 0 ? Math.min(0, nz * far) : -r;
        int z1 = nz != 0 ? Math.max(0, nz * far) : r;
        return new int[] {x0 - 1, -r - 1, z0 - 1, x1 + 1, r + 1, z1 + 1};
    }

    /** Offsets of every shell block, packed as three ints each. */
    public static int[] shell(Shape shape, int r, int t, int nx, int nz) {
        int[] b = bounds(shape, r, t, nx, nz);
        int n = 0;
        int[] out = new int[1536];
        for (int x = b[0]; x <= b[3]; x++) {
            for (int y = b[1]; y <= b[4]; y++) {
                for (int z = b[2]; z <= b[5]; z++) {
                    if (!inShell(shape, x, y, z, r, t, nx, nz)) continue;
                    if (n + 3 > out.length) out = java.util.Arrays.copyOf(out, out.length * 2);
                    out[n++] = x;
                    out[n++] = y;
                    out[n++] = z;
                }
            }
        }
        return java.util.Arrays.copyOf(out, n);
    }

    /** Number of shell blocks. Counts without allocating. */
    public static int count(Shape shape, int r, int t, int nx, int nz) {
        int[] b = bounds(shape, r, t, nx, nz);
        int n = 0;
        for (int x = b[0]; x <= b[3]; x++) {
            for (int y = b[1]; y <= b[4]; y++) {
                for (int z = b[2]; z <= b[5]; z++) {
                    if (inShell(shape, x, y, z, r, t, nx, nz)) n++;
                }
            }
        }
        return n;
    }

    /** Largest radius not above the wanted one whose shell has at most cap blocks (at least 1). */
    public static int fit(Shape shape, int wanted, int t, int nx, int nz, int cap) {
        int r = Math.max(1, wanted);
        while (r > 1 && count(shape, r, t, nx, nz) > cap) r--;
        return r;
    }

    /**
     * True when a point (in block units relative to the projector centre) is somewhere a blocked entity must not be: anywhere inside
     * the outer cube or sphere, or inside the slab of a wall (with half a block to spare on each side).
     */
    public static boolean inVolume(Shape shape, double dx, double dy, double dz, int r, int t, int nx, int nz) {
        switch (shape) {
            case CUBE:
                return Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz))) <= r + 0.5;
            case SPHERE:
                return dx * dx + dy * dy + dz * dz <= (r + 1.0) * (r + 1.0);
            default: {
                double along = dx * nx + dz * nz;
                double side = Math.abs(dx * nz - dz * nx);
                return along >= WALL_GAP - 0.5 && along <= WALL_GAP + t + 0.5 && side <= r + 1 && Math.abs(dy) <= r + 1;
            }
        }
    }

    /**
     * Where to put a blocked entity that stands at the offset: the same point moved out through the nearest outer face (cube), along
     * the radius (sphere) or to the front of the slab (wall), clear of the shell by about a block and a half.
     */
    public static double[] eject(Shape shape, double dx, double dy, double dz, int r, int t, int nx, int nz) {
        double clear = 1.6;
        switch (shape) {
            case CUBE: {
                double ax = Math.abs(dx), ay = Math.abs(dy), az = Math.abs(dz);
                double edge = r + 0.5 + clear;
                // leave through the face the point is closest to, measured as the shortest distance to the outer surface
                double ex = r + 0.5 - ax, ey = r + 0.5 - ay, ez = r + 0.5 - az;
                if (ex <= ey && ex <= ez) return new double[] {Math.signum(dx == 0 ? 1 : dx) * edge, dy, dz};
                if (ez <= ey) return new double[] {dx, dy, Math.signum(dz == 0 ? 1 : dz) * edge};
                // never push up or down: pick the nearer horizontal face when the vertical one wins
                if (ex <= ez) return new double[] {Math.signum(dx == 0 ? 1 : dx) * edge, dy, dz};
                return new double[] {dx, dy, Math.signum(dz == 0 ? 1 : dz) * edge};
            }
            case SPHERE: {
                double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (len < 1e-6) return new double[] {r + 0.5 + clear, 0, 0};
                double k = (r + 0.5 + clear) / len;
                return new double[] {dx * k, dy * k, dz * k};
            }
            default: {
                double along = dx * nx + dz * nz;
                double target = WALL_GAP + t + clear;
                return new double[] {dx + nx * (target - along), dy, dz + nz * (target - along)};
            }
        }
    }
}
