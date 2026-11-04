package com.github.alexthe666.citadel.client.render.legacy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Bridge between the 1.21 immediate-mode render style (getBuffer(renderType).addVertex(...)) and 26.1's deferred submit pipeline.
 * Vertices written through this source are recorded in their final (already transformed) space, then replayed with
 * {@link SubmitNodeCollector#submitCustomGeometry} per render type by {@link #flush}.
 */
public class RecordingBufferSource implements MultiBufferSource {
    private final Map<RenderType, Recorder> recorders = new LinkedHashMap<>();

    @Override
    public VertexConsumer getBuffer(RenderType type) {
        return recorders.computeIfAbsent(type, t -> new Recorder());
    }

    public boolean isEmpty() {
        return recorders.isEmpty();
    }

    public void flush(SubmitNodeCollector collector) {
        for (Map.Entry<RenderType, Recorder> e : recorders.entrySet()) {
            Recorder r = e.getValue();
            if (r.count() == 0) continue;
            collector.submitCustomGeometry(new PoseStack(), e.getKey(), (pose, vc) -> r.replay(vc));
        }
        recorders.clear();
    }

    private static final class Recorder implements VertexConsumer {
        // per vertex: x y z u v nx ny nz  (8 floats) and color overlay light (3 ints, overlay/light packed u|v<<16)
        private final FloatArrayList f = new FloatArrayList();
        private final IntArrayList i = new IntArrayList();
        private float x, y, z, u, v, nx, ny = 1, nz;
        private int color = -1, overlay = 0, light = 0;
        private boolean open;

        int count() {
            return i.size() / 3 + (open ? 1 : 0);
        }

        private void close() {
            if (open) {
                f.add(x); f.add(y); f.add(z); f.add(u); f.add(v); f.add(nx); f.add(ny); f.add(nz);
                i.add(color); i.add(overlay); i.add(light);
                open = false;
            }
        }

        @Override
        public VertexConsumer addVertex(float px, float py, float pz) {
            close();
            x = px; y = py; z = pz; u = 0; v = 0; nx = 0; ny = 1; nz = 0; color = -1; overlay = 0; light = 0;
            open = true;
            return this;
        }

        @Override
        public VertexConsumer setColor(int r, int g, int b, int a) {
            color = ((a & 255) << 24) | ((r & 255) << 16) | ((g & 255) << 8) | (b & 255);
            return this;
        }

        @Override
        public VertexConsumer setColor(int argb) {
            color = argb;
            return this;
        }

        @Override
        public VertexConsumer setUv(float pu, float pv) {
            u = pu; v = pv;
            return this;
        }

        @Override
        public VertexConsumer setUv1(int pu, int pv) {
            overlay = (pu & 0xFFFF) | (pv << 16);
            return this;
        }

        @Override
        public VertexConsumer setUv2(int pu, int pv) {
            light = (pu & 0xFFFF) | (pv << 16);
            return this;
        }

        @Override
        public VertexConsumer setNormal(float pnx, float pny, float pnz) {
            nx = pnx; ny = pny; nz = pnz;
            return this;
        }

        @Override
        public VertexConsumer setLineWidth(float w) {
            return this;
        }

        void replay(VertexConsumer out) {
            close();
            int n = i.size() / 3;
            for (int k = 0; k < n; k++) {
                int b = k * 8;
                int c = i.getInt(k * 3), o = i.getInt(k * 3 + 1), l = i.getInt(k * 3 + 2);
                out.addVertex(f.getFloat(b), f.getFloat(b + 1), f.getFloat(b + 2))
                        .setColor(c)
                        .setUv(f.getFloat(b + 3), f.getFloat(b + 4))
                        .setUv1(o & 0xFFFF, o >>> 16)
                        .setUv2(l & 0xFFFF, l >>> 16)
                        .setNormal(f.getFloat(b + 5), f.getFloat(b + 6), f.getFloat(b + 7));
            }
        }
    }
}
