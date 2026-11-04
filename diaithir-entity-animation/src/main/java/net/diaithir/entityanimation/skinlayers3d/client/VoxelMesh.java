package net.diaithir.entityanimation.skinlayers3d.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.platform.NativeImage;
import java.util.ArrayList;
import java.util.List;

/** Per-pixel extruded cuboids for one outer-layer box (UV origin u,v; size w,h,d in pixels). */
public final class VoxelMesh {
    private static final int MAX_QUADS = 12000;

    private final float[] data; // x,y,z,nx,ny,nz,u,v per vertex
    private final int vertexCount;

    private VoxelMesh(float[] data, int vertexCount) {
        this.data = data;
        this.vertexCount = vertexCount;
    }

    public boolean isEmpty() {
        return vertexCount == 0;
    }

    public static VoxelMesh build(NativeImage img, int u, int v, int w, int h, int d) {
        int tw = img.getWidth(), th = img.getHeight();
        List<float[]> quads = new ArrayList<>();
        // faces: order matches vanilla cube UV layout
        // top
        face(img, quads, tw, th, u + d, v, w, d, Face.UP, w, h, d);
        face(img, quads, tw, th, u + d + w, v, w, d, Face.DOWN, w, h, d);
        face(img, quads, tw, th, u, v + d, d, h, Face.EAST, w, h, d);
        face(img, quads, tw, th, u + d, v + d, w, h, Face.NORTH, w, h, d);
        face(img, quads, tw, th, u + d + w, v + d, d, h, Face.WEST, w, h, d);
        face(img, quads, tw, th, u + 2 * d + w, v + d, w, h, Face.SOUTH, w, h, d);
        int n = Math.min(quads.size(), MAX_QUADS);
        float[] out = new float[n * 4 * 8];
        int o = 0;
        for (int i = 0; i < n; i++) {
            float[] q = quads.get(i);
            System.arraycopy(q, 0, out, o, 32);
            o += 32;
        }
        return new VoxelMesh(out, n * 4);
    }

    private enum Face { UP, DOWN, NORTH, SOUTH, EAST, WEST }

    private static boolean opaque(NativeImage img, int x, int y, int tw, int th) {
        if (x < 0 || y < 0 || x >= tw || y >= th) return false;
        return (img.getPixel(x, y) >>> 24) > 10;
    }

    // Box spans x:[0,w] y:[0,h] z:[0,d], model-space pixels, y down (vanilla). Origin at box min corner.
    private static void face(NativeImage img, List<float[]> out, int tw, int th, int fu, int fv, int fw, int fh,
                             Face f, int w, int h, int d) {
        for (int i = 0; i < fw; i++) {
            for (int j = 0; j < fh; j++) {
                int px = fu + i, py = fv + j;
                if (!opaque(img, px, py, tw, th)) continue;
                float u0 = px / (float) tw, u1 = (px + 1) / (float) tw;
                float v0 = py / (float) th, v1 = (py + 1) / (float) th;
                float x0, x1, y0, y1, z0, z1;
                float nx = 0, ny = 0, nz = 0;
                float[] c = new float[32];
                switch (f) {
                    case UP -> { // image x -> +x, image y -> +z(front is -z so v grows toward front: flipped)
                        x0 = i; x1 = i + 1; z0 = d - j - 1; z1 = d - j; y0 = y1 = 0; ny = -1;
                        put(c, 0, x1, y0, z0, nx, ny, nz, u0, v1);
                        put(c, 1, x0, y0, z0, nx, ny, nz, u1, v1);
                        put(c, 2, x0, y0, z1, nx, ny, nz, u1, v0);
                        put(c, 3, x1, y0, z1, nx, ny, nz, u0, v0);
                        // fix winding/uv: top face
                        put(c, 0, x0, y0, z1, nx, ny, nz, u0, v0);
                        put(c, 1, x1, y0, z1, nx, ny, nz, u1, v0);
                        put(c, 2, x1, y0, z0, nx, ny, nz, u1, v1);
                        put(c, 3, x0, y0, z0, nx, ny, nz, u0, v1);
                        // mirror x so image x maps to world x
                    }
                    case DOWN -> {
                        x0 = i; x1 = i + 1; z0 = j; z1 = j + 1; y0 = h; ny = 1;
                        put(c, 0, x0, y0, z0, nx, ny, nz, u0, v0);
                        put(c, 1, x1, y0, z0, nx, ny, nz, u1, v0);
                        put(c, 2, x1, y0, z1, nx, ny, nz, u1, v1);
                        put(c, 3, x0, y0, z1, nx, ny, nz, u0, v1);
                        // flip x to match vanilla (down face mirrored on x)
                        float dx = w;
                        for (int k = 0; k < 4; k++) c[k * 8] = dx - c[k * 8] - 0f;
                        // reorder winding after mirror
                        swap(c, 0, 1);
                        swap(c, 2, 3);
                        // restore intended z ordering (z: back..front vs image y)
                    }
                    case NORTH -> { // front, faces -z
                        x0 = i; x1 = i + 1; y0 = j; y1 = j + 1; nz = -1;
                        put(c, 0, x1, y1, 0, nx, ny, nz, u0, v1);
                        put(c, 1, x0, y1, 0, nx, ny, nz, u1, v1);
                        put(c, 2, x0, y0, 0, nx, ny, nz, u1, v0);
                        put(c, 3, x1, y0, 0, nx, ny, nz, u0, v0);
                        // front: image x increases toward +x of viewer-right => model -x; mirror
                        for (int k = 0; k < 4; k++) c[k * 8] = w - c[k * 8];
                        // after mirror x ordering reversed; restore by swapping uv-consistent pairs
                        swap(c, 0, 1);
                        swap(c, 2, 3);
                        // re-assign UVs to correct vertices
                        fixUv(c, u0, u1, v0, v1);
                    }
                    case SOUTH -> { // back, faces +z
                        y0 = j; y1 = j + 1; nz = 1;
                        float xa = w - i, xb = w - i - 1;
                        put(c, 0, xb, y1, d, nx, ny, nz, u1, v1);
                        put(c, 1, xa, y1, d, nx, ny, nz, u0, v1);
                        put(c, 2, xa, y0, d, nx, ny, nz, u0, v0);
                        put(c, 3, xb, y0, d, nx, ny, nz, u1, v0);
                        // x mapped so image-x increases toward -x from viewer behind
                        for (int k = 0; k < 4; k++) c[k * 8] = c[k * 8];
                    }
                    case EAST -> { // +x side in model space (image left part of side strip), faces +x
                        y0 = j; y1 = j + 1; nx = 1;
                        float za = i, zb = i + 1;
                        put(c, 0, w, y1, za, nx, ny, nz, u0, v1);
                        put(c, 1, w, y1, zb, nx, ny, nz, u1, v1);
                        put(c, 2, w, y0, zb, nx, ny, nz, u1, v0);
                        put(c, 3, w, y0, za, nx, ny, nz, u0, v0);
                    }
                    default -> { // WEST faces -x
                        y0 = j; y1 = j + 1; nx = -1;
                        float za = d - i, zb = d - i - 1;
                        put(c, 0, 0, y1, za, nx, ny, nz, u0, v1);
                        put(c, 1, 0, y1, zb, nx, ny, nz, u1, v1);
                        put(c, 2, 0, y0, zb, nx, ny, nz, u1, v0);
                        put(c, 3, 0, y0, za, nx, ny, nz, u0, v0);
                    }
                }
                // Extrude: each face cell becomes a full cube side only when the pixel is opaque; the cell's depth
                // is represented by offsetting the face outward is not needed -- side walls are produced below.
                out.add(c);
                addWalls(img, out, tw, th, px, py, fu, fv, fw, fh, i, j, f, w, h, d);
            }
        }
    }

    private static void addWalls(NativeImage img, List<float[]> out, int tw, int th, int px, int py,
                                 int fu, int fv, int fw, int fh, int i, int j, Face f, int w, int h, int d) {
        // Depth walls are generated for neighbours within the same face that are transparent/out of face.
        // Simplified: thin outward extrusion of 1 pixel is done via the cube extent in render(); walls omitted
        // where adjacent cell is opaque (culled) and added otherwise.
        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] dd : dirs) {
            int ni = i + dd[0], nj = j + dd[1];
            boolean inFace = ni >= 0 && nj >= 0 && ni < fw && nj < fh;
            if (inFace && opaque(img, fu + ni, fv + nj, tw, th)) continue;
            float[] base = out.get(out.size() - 1);
            float[] c = new float[32];
            // build wall from the face quad edge, extruded inward by 1px toward box centre then rendered scaled out
            wall(base, c, dd, f);
            if (c != null) out.add(c);
        }
    }

    private static void wall(float[] face, float[] c, int[] dd, Face f) {
        // Edge selection: vertices 0..3 form a loop; pick edge by direction using face-local extents.
        float nx = face[3], ny = face[4], nz = face[5];
        // outward offset along face normal by 1 pixel (extrusion outward)
        float ox = nx, oy = ny, oz = nz;
        int a, b;
        // choose edge via uv-space direction
        float[] uvc = {0, 0};
        a = 0; b = 1;
        float minU = Math.min(Math.min(face[6], face[14]), Math.min(face[22], face[30]));
        float maxU = Math.max(Math.max(face[6], face[14]), Math.max(face[22], face[30]));
        float minV = Math.min(Math.min(face[7], face[15]), Math.min(face[23], face[31]));
        float maxV = Math.max(Math.max(face[7], face[15]), Math.max(face[23], face[31]));
        boolean found = false;
        for (int e = 0; e < 4 && !found; e++) {
            int p = e, q = (e + 1) % 4;
            float pu = face[p * 8 + 6], pv = face[p * 8 + 7], qu = face[q * 8 + 6], qv = face[q * 8 + 7];
            boolean match;
            if (dd[0] == -1) match = pu == minU && qu == minU;
            else if (dd[0] == 1) match = pu == maxU && qu == maxU;
            else if (dd[1] == -1) match = pv == minV && qv == minV;
            else match = pv == maxV && qv == maxV;
            if (match) { a = p; b = q; found = true; }
        }
        if (!found) return;
        float cu = (face[6] + face[14] + face[22] + face[30]) / 4f;
        float cv = (face[7] + face[15] + face[23] + face[31]) / 4f;
        put(c, 0, face[a * 8], face[a * 8 + 1], face[a * 8 + 2], 0, 0, 0, cu, cv);
        put(c, 1, face[b * 8], face[b * 8 + 1], face[b * 8 + 2], 0, 0, 0, cu, cv);
        put(c, 2, face[b * 8] + ox, face[b * 8 + 1] + oy, face[b * 8 + 2] + oz, 0, 0, 0, cu, cv);
        put(c, 3, face[a * 8] + ox, face[a * 8 + 1] + oy, face[a * 8 + 2] + oz, 0, 0, 0, cu, cv);
        // wall normal: perpendicular to edge and face normal
        float ex = face[b * 8] - face[a * 8], ey = face[b * 8 + 1] - face[a * 8 + 1], ez = face[b * 8 + 2] - face[a * 8 + 2];
        float wx = ey * nz - ez * ny, wy = ez * nx - ex * nz, wz = ex * ny - ey * nx;
        float len = (float) Math.sqrt(wx * wx + wy * wy + wz * wz);
        if (len > 0) { wx /= len; wy /= len; wz /= len; }
        // face centre
        float fx = (face[0] + face[8] + face[16] + face[24]) / 4f;
        float fy = (face[1] + face[9] + face[17] + face[25]) / 4f;
        float fz = (face[2] + face[10] + face[18] + face[26]) / 4f;
        float mx = (face[a * 8] + face[b * 8]) / 2f - fx, my = (face[a * 8 + 1] + face[b * 8 + 1]) / 2f - fy, mz = (face[a * 8 + 2] + face[b * 8 + 2]) / 2f - fz;
        if (wx * mx + wy * my + wz * mz < 0) { wx = -wx; wy = -wy; wz = -wz; }
        for (int k = 0; k < 4; k++) { c[k * 8 + 3] = wx; c[k * 8 + 4] = wy; c[k * 8 + 5] = wz; }
    }

    private static void put(float[] c, int idx, float x, float y, float z, float nx, float ny, float nz, float u, float v) {
        int o = idx * 8;
        c[o] = x; c[o + 1] = y; c[o + 2] = z; c[o + 3] = nx; c[o + 4] = ny; c[o + 5] = nz; c[o + 6] = u; c[o + 7] = v;
    }

    private static void swap(float[] c, int a, int b) {
        for (int k = 0; k < 8; k++) {
            float t = c[a * 8 + k];
            c[a * 8 + k] = c[b * 8 + k];
            c[b * 8 + k] = t;
        }
    }

    private static void fixUv(float[] c, float u0, float u1, float v0, float v1) {
        // vertices after mirror+swap: order x ascending; assign u so image x increases toward model -x
        put(c, 0, c[0], c[1], c[2], c[3], c[4], c[5], u1, v1);
        put(c, 1, c[8], c[9], c[10], c[11], c[12], c[13], u0, v1);
        put(c, 2, c[16], c[17], c[18], c[19], c[20], c[21], u0, v0);
        put(c, 3, c[24], c[25], c[26], c[27], c[28], c[29], u1, v0);
    }

    /**
     * Render the mesh. Coordinates are in pixels relative to the box min corner; (ox,oy,oz) is the box min corner
     * in part-local pixels; scale inflates about the box centre.
     */
    public void render(PoseStack pose, VertexConsumer vc, int light, int overlay, int color,
                       float ox, float oy, float oz, float w, float h, float d, float scale) {
        PoseStack.Pose p = pose.last();
        float cx = w / 2f, cy = h / 2f, cz = d / 2f;
        for (int i = 0; i < vertexCount; i++) {
            int o = i * 8;
            float x = (data[o] - cx) * scale + cx + ox;
            float y = (data[o + 1] - cy) * scale + cy + oy;
            float z = (data[o + 2] - cz) * scale + cz + oz;
            vc.addVertex(p, x / 16f, y / 16f, z / 16f)
                    .setColor(color)
                    .setUv(data[o + 6], data[o + 7])
                    .setOverlay(overlay)
                    .setLight(light)
                    .setNormal(p, data[o + 3], data[o + 4], data[o + 5]);
        }
    }
}
