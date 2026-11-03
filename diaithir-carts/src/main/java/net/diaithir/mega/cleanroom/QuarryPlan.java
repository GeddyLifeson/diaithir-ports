package net.diaithir.mega.cleanroom;

/**
 * Clean-room quarry cursor: digs a rectangular footprint one horizontal layer at a time from the top down to a floor, column by
 * column in a serpentine order. Written from the behaviour of a quarry block only (no third-party code or assets).
 * Energy per block comes from {@code Dig} (MegaSpec specific energy); this class only decides WHICH cell is dug next.
 */
public final class QuarryPlan {
    private final int x0, z0, w, d, topY, floorY;
    private int layer, index;

    /** Footprint w x d starting at (x0,z0); digs from topY down to floorY inclusive. */
    public QuarryPlan(int x0, int z0, int w, int d, int topY, int floorY) {
        if (w <= 0 || d <= 0 || floorY > topY) throw new IllegalArgumentException("empty quarry");
        this.x0 = x0; this.z0 = z0; this.w = w; this.d = d; this.topY = topY; this.floorY = floorY;
    }

    public long totalCells() { return (long) w * d * (topY - floorY + 1); }

    public boolean done() { return topY - layer < floorY; }

    /** Next cell {x,y,z}, advancing the cursor; null when finished. Rows alternate direction so travel stays short. */
    public int[] next() {
        if (done()) return null;
        int row = index / w, col = index % w;
        int x = x0 + ((row % 2 == 0) ? col : w - 1 - col);
        int[] cell = {x, topY - layer, z0 + row};
        if (++index >= w * d) { index = 0; layer++; }
        return cell;
    }

    public long dugCells() { return (long) layer * w * d + index; }
}
