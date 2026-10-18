package me.desht.pneumaticcraft.client.render.legacy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lets McJtyLib-style renderers, which write vertices into a MultiBufferSource, run under 26.1's submit pipeline:
 * vertices are recorded per render type, then {@link #submit()} hands them to the collector as custom geometry.
 * Elements a renderer never set get defaults, so a render type that needs more elements than the old code wrote still draws.
 */
public final class RecordingBufferSource implements MultiBufferSource {

    private final SubmitNodeCollector collector;
    private final Map<RenderType, Recorder> buffers = new LinkedHashMap<>();

    public RecordingBufferSource(SubmitNodeCollector collector) {
        this.collector = collector;
    }

    public SubmitNodeCollector collector() {
        return collector;
    }

    @Override
    public VertexConsumer getBuffer(RenderType type) {
        return buffers.computeIfAbsent(type, t -> new Recorder());
    }

    public void submit() {
        PoseStack identity = new PoseStack();
        buffers.forEach((type, recorder) -> collector.submitCustomGeometry(identity, type, (pose, consumer) -> recorder.replay(consumer)));
        buffers.clear();
    }

    private static final class Recorder implements VertexConsumer {
        // per vertex: x y z u v nx ny nz lineWidth, and color overlay light
        private static final int F = 9;
        private static final int I = 3;
        private final FloatArrayList floats = new FloatArrayList();
        private final IntArrayList ints = new IntArrayList();

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            floats.addElements(floats.size(), new float[]{x, y, z, 0, 0, 0, 1, 0, 1});
            ints.addElements(ints.size(), new int[]{-1, OverlayTexture.NO_OVERLAY, LightCoordsUtil.FULL_BRIGHT});
            return this;
        }

        private void setF(int index, float value) {
            floats.set(floats.size() - F + index, value);
        }

        private void setI(int index, int value) {
            ints.set(ints.size() - I + index, value);
        }

        @Override
        public VertexConsumer setColor(int r, int g, int b, int a) {
            setI(0, ARGB.color(a, r, g, b));
            return this;
        }

        @Override
        public VertexConsumer setColor(int color) {
            setI(0, color);
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            setF(3, u);
            setF(4, v);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            setI(1, u & 0xFFFF | v << 16);
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            setI(2, u & 0xFFFF | v << 16);
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            setF(5, x);
            setF(6, y);
            setF(7, z);
            return this;
        }

        @Override
        public VertexConsumer setLineWidth(float width) {
            setF(8, width);
            return this;
        }

        void replay(VertexConsumer out) {
            for (int f = 0, i = 0; i < ints.size(); f += F, i += I) {
                int overlay = ints.getInt(i + 1);
                int light = ints.getInt(i + 2);
                out.addVertex(floats.getFloat(f), floats.getFloat(f + 1), floats.getFloat(f + 2))
                        .setColor(ints.getInt(i))
                        .setUv(floats.getFloat(f + 3), floats.getFloat(f + 4))
                        .setUv1(overlay & 0xFFFF, overlay >>> 16)
                        .setUv2(light & 0xFFFF, light >>> 16)
                        .setNormal(floats.getFloat(f + 5), floats.getFloat(f + 6), floats.getFloat(f + 7))
                        .setLineWidth(floats.getFloat(f + 8));
            }
        }
    }
}
