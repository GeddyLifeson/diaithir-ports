package net.diaithir.animation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.IntBinaryOperator;

/**
 * Pure geometry for the 3D skin layers: turns one overlay box of the 64x64 skin (hat, jacket, sleeves, pants) into extruded pixel voxels.
 * Every opaque texel of every face becomes a flat outer quad lifted by {@code thick} pixels plus a side wall on each edge whose neighbour texel is
 * transparent or lies outside that face. Positions are model pixels (y down, like ModelPart), uv are 0..1 on a 64x64 texture. No Minecraft classes, so the
 * server selftest can check it.
 */
public final class SkinVoxels {
    public static final int TEX = 64, ALPHA_MIN = 128;

    /** One quad: 4 corners x,y,z (12 floats), their u,v (8 floats) and the outward unit normal. */
    public record Quad(float[] pos, float[] uv, float nx, float ny, float nz) { }

    /** A box of the skin: origin x,y,z and size w,h,d in model pixels, texture origin u,v; thick = extrusion in pixels. */
    public record Box(float x, float y, float z, float w, float h, float d, int u, int v, float thick) { }

    static final float HEAD_THICK = 1.0F, BODY_THICK = 0.7F;

    /** The overlay boxes, as in PlayerModel.createMesh (relative to their bone). */
    public static Map<String, Box> boxes(boolean slim) {
        Map<String, Box> m = new java.util.LinkedHashMap<>();
        m.put("hat", new Box(-4, -8, -4, 8, 8, 8, 32, 0, HEAD_THICK));
        m.put("jacket", new Box(-4, 0, -2, 8, 12, 4, 16, 32, BODY_THICK));
        m.put("left_sleeve", new Box(-1, -2, -2, slim ? 3 : 4, 12, 4, 48, 48, BODY_THICK));
        m.put("right_sleeve", new Box(slim ? -2 : -3, -2, -2, slim ? 3 : 4, 12, 4, 40, 32, BODY_THICK));
        m.put("left_pants", new Box(-2, 0, -2, 4, 12, 4, 0, 48, BODY_THICK));
        m.put("right_pants", new Box(-2, 0, -2, 4, 12, 4, 0, 32, BODY_THICK));
        return m;
    }

    /** Face directions in vanilla ModelPart.Cube order (DOWN UP WEST NORTH EAST SOUTH) and their normals, as Direction.getUnitVec3f gives them (model space is y-down, so DOWN sits at minY). */
    private static final float[][] N = {{0, -1, 0}, {0, 1, 0}, {-1, 0, 0}, {0, 0, -1}, {1, 0, 0}, {0, 0, 1}};

    /** Builds the quads for a box. alpha(x,y) returns the 0..255 alpha of skin pixel x,y. */
    public static List<Quad> build(Box b, IntBinaryOperator alpha) {
        float x0 = b.x, y0 = b.y, z0 = b.z, x1 = b.x + b.w, y1 = b.y + b.h, z1 = b.z + b.d;
        // the eight corners as in ModelPart.Cube: t = z0 side, l = z1 side; 0:(x0,y0) 1:(x1,y0) 2:(x1,y1) 3:(x0,y1)
        float[][] t = {{x0, y0, z0}, {x1, y0, z0}, {x1, y1, z0}, {x0, y1, z0}};
        float[][] l = {{x0, y0, z1}, {x1, y0, z1}, {x1, y1, z1}, {x0, y1, z1}};
        float u0 = b.u, u1 = b.u + b.d, u2 = u1 + b.w, u22 = u2 + b.w, u3 = u2 + b.d, u4 = u3 + b.w;
        float v0 = b.v, v1 = b.v + b.d, v2 = v1 + b.h;
        // per face: 4 vertices in vanilla order and the texture rectangle (a0,b0)-(a1,b1) vanilla passes to Polygon (vertex0=(a1,b0) 1=(a0,b0) 2=(a0,b1) 3=(a1,b1))
        float[][][] verts = {{l[1], l[0], t[0], t[1]}, {t[2], t[3], l[3], l[2]}, {t[0], l[0], l[3], t[3]}, {t[1], t[0], t[3], t[2]}, {l[1], t[1], t[2], l[2]}, {l[0], l[1], l[2], l[3]}};
        float[][] rect = {{u1, v0, u2, v1}, {u2, v1, u22, v0}, {u0, v1, u1, v2}, {u1, v1, u2, v2}, {u2, v1, u3, v2}, {u3, v1, u4, v2}};
        List<Quad> out = new ArrayList<>();
        for (int f = 0; f < 6; f++) {
            float[][] V = verts[f];
            float a0 = rect[f][0], b0 = rect[f][1], a1 = rect[f][2], b1 = rect[f][3];
            int ta = (int) Math.min(a0, a1), tb = (int) Math.min(b0, b1), na = (int) Math.abs(a1 - a0), nb = (int) Math.abs(b1 - b0);
            float[] n = N[f];
            for (int i = 0; i < na; i++) {
                for (int j = 0; j < nb; j++) {
                    int tx = ta + i, ty = tb + j;
                    if (!opaque(alpha, tx, ty)) continue;
                    // outer quad: vertices at the texel's corners, lifted along the normal
                    float[] pos = new float[12], uv = new float[8];
                    for (int k = 0; k < 4; k++) {
                        float a = vertA(k, a0, a1, tx), bb = vertB(k, b0, b1, ty);
                        float[] p = at(V, a0, b0, a1, b1, a, bb);
                        pos[k * 3] = p[0] + n[0] * b.thick; pos[k * 3 + 1] = p[1] + n[1] * b.thick; pos[k * 3 + 2] = p[2] + n[2] * b.thick;
                        uv[k * 2] = a / TEX; uv[k * 2 + 1] = bb / TEX;
                    }
                    out.add(new Quad(pos, uv, n[0], n[1], n[2]));
                    // walls: neighbour in -a, +a, -b, +b (texture directions)
                    int[][] nb4 = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
                    for (int[] d : nb4) {
                        int ni = i + d[0], nj = j + d[1];
                        boolean inside = ni >= 0 && ni < na && nj >= 0 && nj < nb;
                        if (inside && opaque(alpha, ta + ni, tb + nj)) continue;
                        out.add(wall(V, a0, b0, a1, b1, tx, ty, d, n, b.thick));
                    }
                }
            }
        }
        return out;
    }

    private static boolean opaque(IntBinaryOperator alpha, int x, int y) {
        return x >= 0 && y >= 0 && x < TEX && y < TEX && alpha.applyAsInt(x, y) >= ALPHA_MIN;
    }

    /** Texture coordinate of vertex k of a texel [tx,tx+1]x[ty,ty+1], following vanilla's vertex-to-uv assignment (0:(a1,b0) 1:(a0,b0) 2:(a0,b1) 3:(a1,b1)). */
    private static float vertA(int k, float a0, float a1, int tx) {
        boolean high = k == 0 || k == 3;                       // vertices at the a1 side
        boolean upward = a1 > a0;
        return tx + (high == upward ? 1 : 0);
    }

    private static float vertB(int k, float b0, float b1, int ty) {
        boolean high = k == 2 || k == 3;                       // vertices at the b1 side
        boolean upward = b1 > b0;
        return ty + (high == upward ? 1 : 0);
    }

    /** Position on the face for texture coordinate (a,b): bilinear from vertex1 (a0,b0) along 0 (a1,b0) and 2 (a0,b1). */
    private static float[] at(float[][] V, float a0, float b0, float a1, float b1, float a, float b) {
        float sa = (a - a0) / (a1 - a0), sb = (b - b0) / (b1 - b0);
        return new float[]{V[1][0] + sa * (V[0][0] - V[1][0]) + sb * (V[2][0] - V[1][0]),
                V[1][1] + sa * (V[0][1] - V[1][1]) + sb * (V[2][1] - V[1][1]),
                V[1][2] + sa * (V[0][2] - V[1][2]) + sb * (V[2][2] - V[1][2])};
    }

    /** Side wall of texel (tx,ty) toward texture direction d={da,db}; flat colour of that texel, wound to face outward like the vanilla faces. */
    private static Quad wall(float[][] V, float a0, float b0, float a1, float b1, int tx, int ty, int[] d, float[] fn, float thick) {
        // edge in texture space: the two corners of the texel on side d
        float ea0, eb0, ea1, eb1;
        if (d[0] != 0) { ea0 = ea1 = tx + (d[0] > 0 ? 1 : 0); eb0 = ty; eb1 = ty + 1; }
        else { eb0 = eb1 = ty + (d[1] > 0 ? 1 : 0); ea0 = tx; ea1 = tx + 1; }
        float[] p0 = at(V, a0, b0, a1, b1, ea0, eb0), p1 = at(V, a0, b0, a1, b1, ea1, eb1);
        // outward direction of the wall in 3D: from the texel centre to the edge midpoint, projected on the face plane
        float[] c = at(V, a0, b0, a1, b1, tx + 0.5f, ty + 0.5f);
        float[] wn = {(p0[0] + p1[0]) / 2 - c[0], (p0[1] + p1[1]) / 2 - c[1], (p0[2] + p1[2]) / 2 - c[2]};
        float len = (float) Math.sqrt(wn[0] * wn[0] + wn[1] * wn[1] + wn[2] * wn[2]);
        wn[0] /= len; wn[1] /= len; wn[2] /= len;
        float[] q0 = p0, q1 = {p0[0] + fn[0] * thick, p0[1] + fn[1] * thick, p0[2] + fn[2] * thick},
                q2 = {p1[0] + fn[0] * thick, p1[1] + fn[1] * thick, p1[2] + fn[2] * thick}, q3 = p1;
        float[] cr = cross(sub(q1, q0), sub(q2, q1));
        boolean flip = Math.signum(dot(cr, wn)) != FRONT;      // vanilla's winding sign relative to the outward normal
        float[][] ps = flip ? new float[][]{q3, q2, q1, q0} : new float[][]{q0, q1, q2, q3};
        float[] pos = new float[12], uv = new float[8];
        float cu = (tx + 0.5f) / TEX, cv = (ty + 0.5f) / TEX;
        for (int k = 0; k < 4; k++) {
            pos[k * 3] = ps[k][0]; pos[k * 3 + 1] = ps[k][1]; pos[k * 3 + 2] = ps[k][2];
            uv[k * 2] = cu; uv[k * 2 + 1] = cv;
        }
        return new Quad(pos, uv, wn[0], wn[1], wn[2]);
    }

    /** Sign of dot(cross(v1-v0, v2-v1), faceNormal) in vanilla's cubes (measured once from the DOWN face {l1,l0,t0,t1}, whose normal is (0,-1,0): the model is y-down). */
    static final float FRONT = frontSign();

    private static float frontSign() {
        float[] l1 = {1, 1, 1}, l0 = {0, 1, 1}, t0 = {0, 1, 0};
        return Math.signum(dot(cross(sub(l0, l1), sub(t0, l0)), new float[]{0, -1, 0}));
    }

    static float[] sub(float[] a, float[] b) { return new float[]{a[0] - b[0], a[1] - b[1], a[2] - b[2]}; }
    static float dot(float[] a, float[] b) { return a[0] * b[0] + a[1] * b[1] + a[2] * b[2]; }
    static float[] cross(float[] a, float[] b) { return new float[]{a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]}; }

    /** Geometric normal sign of a quad relative to its declared normal: +1 when wound like vanilla (selftest). */
    public static float winding(Quad q) {
        float[] p = q.pos;
        float[] v0 = {p[0], p[1], p[2]}, v1 = {p[3], p[4], p[5]}, v2 = {p[6], p[7], p[8]};
        return Math.signum(dot(cross(sub(v1, v0), sub(v2, v1)), new float[]{q.nx, q.ny, q.nz})) * FRONT;
    }

    private SkinVoxels() { }
}
