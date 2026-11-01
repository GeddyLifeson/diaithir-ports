package net.diaithir.sharedspace;

/** The four quarter cells of a shared space, seen from above: bit 0 is east (+x), bit 1 is south (+z). */
public final class Cells {
    public static final int COUNT = 4;

    private Cells() {
    }

    public static int at(double localX, double localZ) {
        return (localX >= 0.5 ? 1 : 0) | (localZ >= 0.5 ? 2 : 0);
    }

    /** The cell diagonally across, where the block that was already standing in the middle moves to. */
    public static int opposite(int cell) {
        return COUNT - 1 - cell;
    }

    public static double offsetX(int cell) {
        return (cell & 1) == 0 ? -0.25 : 0.25;
    }

    public static double offsetZ(int cell) {
        return (cell & 2) == 0 ? -0.25 : 0.25;
    }

    /** The free cell closest to the given local point, or -1 when all four are taken. */
    public static int nearestFree(boolean[] taken, double localX, double localZ) {
        int best = -1;
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i < COUNT; i++) {
            if (taken[i]) {
                continue;
            }
            double dx = localX - (0.5 + offsetX(i));
            double dz = localZ - (0.5 + offsetZ(i));
            double d = dx * dx + dz * dz;
            if (d < bestDist) {
                bestDist = d;
                best = i;
            }
        }
        return best;
    }
}
