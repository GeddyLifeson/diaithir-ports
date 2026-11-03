package org.diaithir.mega.shape;

/**
 * Dig-region shapes ported from RFTools Builder (McJtyMods/RFToolsBuilder, shapes/Formulas.java).
 *
 * Original: MIT License, Copyright (c) 2019 McJtyMods. Full notice in LICENSES.md (RFTools Builder row).
 * Changes: Minecraft types (World, BlockPos, NBT) removed so the maths is pure Java and unit-testable; Scan and Composition
 * formulas (world/NBT backed) are left to the entity layer; per-shape code folded into one enum.
 */
public enum Shape {
    BOX, TOP_DOME, BOTTOM_DOME, SPHERE, CYLINDER, CAPPED_CYLINDER, PRISM, TORUS, HEART, CONE;

    /** A shape fitted to a bounding box of dx*dy*dz blocks centred on (cx,cy,cz) plus offset. */
    public Formula fit(int cx, int cy, int cz, int dx, int dy, int dz, int ox, int oy, int oz) {
        return new Formula(this, cx, cy, cz, dx, dy, dz, ox, oy, oz);
    }

    public static final class Formula {
        private final Shape shape;
        private final int dx, dy, dz;
        private final float centerx, centery, centerz;
        private final int x1, y1, z1, x2, y2, z2;
        private final float dx2, dy2, dz2;
        private final int davg2;       // davg for 2D shapes
        private final int davg3;       // davg for 3D shapes
        private final float smallRadius, bigRadius, coneDy, topy;

        Formula(Shape shape, int cx, int cy, int cz, int dx, int dy, int dz, int ox, int oy, int oz) {
            this.shape = shape;
            this.dx = dx; this.dy = dy; this.dz = dz;
            centerx = cx + ox + ((dx % 2 != 0) ? 0.0f : -.5f);
            centery = cy + oy + ((dy % 2 != 0) ? 0.0f : -.5f);
            centerz = cz + oz + ((dz % 2 != 0) ? 0.0f : -.5f);
            x1 = cx - dx / 2 + ox; y1 = cy - dy / 2 + oy; z1 = cz - dz / 2 + oz;
            x2 = x1 + dx; y2 = y1 + dy; z2 = z1 + dz;
            float f3 = 1.8f, f2 = 1.7f;
            boolean flat = shape == CYLINDER || shape == CAPPED_CYLINDER || shape == CONE;
            float factor = flat ? f2 : f3;
            dx2 = dx == 0 ? .5f : ((dx + factor) * (dx + factor)) / 4.0f;
            dy2 = dy == 0 ? .5f : ((dy + factor) * (dy + factor)) / 4.0f;
            dz2 = dz == 0 ? .5f : ((dz + factor) * (dz + factor)) / 4.0f;
            davg3 = (int) ((dx + dy + dz + f3 * 3) / 3);
            davg2 = (int) ((dx + dz + f2 * 2) / 2);
            smallRadius = (dy - 2) / 2.0f;
            bigRadius = (dx - 2) / 2.0f - smallRadius;
            coneDy = dy + .5f;                       // avoid division by zero
            topy = cy + oy + dy / 2;
        }

        public boolean isInside(int x, int y, int z) {
            switch (shape) {
                case BOX: return x >= x1 && x < x2 && y >= y1 && y < y2 && z >= z1 && z < z2;
                case PRISM: {
                    if (y < y1 || y >= y2) return false;
                    int d = y - y1;
                    return x >= x1 + d && x < x2 - d && z >= z1 + d && z < z2 - d;
                }
                case SPHERE: return round3(x, y, z);
                case TOP_DOME: return y >= centery && round3(x, y, z);
                case BOTTOM_DOME: return y <= centery && round3(x, y, z);
                case CYLINDER: return round2(x, z);
                case CAPPED_CYLINDER: return y >= y1 && y < y2 && round2(x, z);
                case CONE: {
                    double dist = Math.sqrt(sq2(x, z));
                    return ((int) (dist * (davg2 / 2 + 1))) <= (davg2 / 2 - 1) * (topy - y) / coneDy;
                }
                case TORUS: {
                    double rr = bigRadius - Math.sqrt((x - centerx) * (x - centerx) + (z - centerz) * (z - centerz));
                    return rr * rr + (y - centery) * (y - centery) - smallRadius * smallRadius < 0;
                }
                case HEART: {
                    double xx = (x - centerx) * 2.6 / dx + .1;
                    double zz = (y - centery) * 2.4 / dy + .2;
                    double yy = (z - centerz) * 1.6 / dz + .1;
                    double f1 = Math.pow(xx * xx + (9.0 / 4.0) * yy * yy + zz * zz - 1, 3.0);
                    return f1 - xx * xx * zz * zz * zz - (9.0 / 80.0) * yy * yy * zz * zz * zz < 0;
                }
            }
            return false;
        }

        /** Surface voxel: inside with at least one face neighbour outside (hollow shells, tunnel linings). */
        public boolean isBorder(int x, int y, int z) {
            if (shape == BOX)
                return isInside(x, y, z) && (x == x1 || x == x2 - 1 || y == y1 || y == y2 - 1 || z == z1 || z == z2 - 1);
            if (!isInside(x - 1, y, z) || !isInside(x + 1, y, z) || !isInside(x, y, z - 1) || !isInside(x, y, z + 1)
                    || !isInside(x, y - 1, z) || !isInside(x, y + 1, z))
                return isInside(x, y, z);
            return false;
        }

        /** Number of 1 m3 blocks in the region (solid) or shell only. Feeds {@code Dig.Deposit}. */
        public int volume(boolean solid) {
            int n = 0;
            int pad = 2;
            for (int x = x1 - pad; x < x2 + pad; x++)
                for (int y = y1 - pad; y < y2 + pad; y++)
                    for (int z = z1 - pad; z < z2 + pad; z++)
                        if (solid ? isInside(x, y, z) : isBorder(x, y, z)) n++;
            return n;
        }

        private boolean round3(int x, int y, int z) {
            double dist = Math.sqrt((x - centerx) * (x - centerx) / dx2 + (y - centery) * (y - centery) / dy2 + (z - centerz) * (z - centerz) / dz2);
            return ((int) (dist * (davg3 / 2 + 1))) <= (davg3 / 2 - 1);
        }

        private boolean round2(int x, int z) {
            double dist = Math.sqrt(sq2(x, z));
            return ((int) (dist * (davg2 / 2 + 1))) <= (davg2 / 2 - 1);
        }

        private float sq2(int x, int z) {
            return (x - centerx) * (x - centerx) / dx2 + (z - centerz) * (z - centerz) / dz2;
        }
    }
}
