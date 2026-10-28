package me.desht.pneumaticcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter that lets the original PneumaticCraft block entity renderers (written against the pre-26.1
 * {@code render(te, partialTicks, poseStack, bufferSource, light, overlay)} API) run on the 26.1 submit API.
 * The legacy render method writes vertices into a recording buffer source; the recorded geometry is then
 * replayed through {@link SubmitNodeCollector#submitCustomGeometry}.
 */
public abstract class LegacyBER<T extends BlockEntity> implements BlockEntityRenderer<T, LegacyBER.State<T>> {

    public static class State<T extends BlockEntity> extends BlockEntityRenderState {
        public T te;
        public float partialTicks;
    }

    @Override
    public State<T> createRenderState() {
        return new State<>();
    }

    @Override
    public void extractRenderState(T te, State<T> state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderState.extractBase(te, state, breakProgress);
        state.te = te;
        state.partialTicks = partialTicks;
    }

    @Override
    public void submit(State<T> state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.te == null || state.te.isRemoved()) return;
        Recorder rec = new Recorder(state.lightCoords);
        render(state.te, state.partialTicks, poseStack, rec, state.lightCoords, OverlayTexture.NO_OVERLAY);
        rec.replay(collector);
    }

    /** The legacy render method. */
    protected abstract void render(T te, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay);

    // ------------------------------------------------------------------------------------------------------------

    /** Queue an item to be drawn at the current pose once the recorded geometry is replayed. */
    public static void renderItem(ItemStack stack, ItemDisplayContext ctx, PoseStack ps, MultiBufferSource buffer, int light) {
        if (stack.isEmpty() || !(buffer instanceof Recorder rec)) return;
        PoseStack copy = new PoseStack();
        copy.last().pose().set(ps.last().pose());
        copy.last().normal().set(ps.last().normal());
        rec.items.add(new QueuedItem(stack.copy(), ctx, copy, light));
    }

    /** Queue a text string, drawn at the current pose once geometry is replayed. */
    public static void drawText(Font font, String text, float x, float y, int color, PoseStack ps, MultiBufferSource buffer, int light) {
        drawText(font, Component.literal(text), x, y, color, ps, buffer, light);
    }

    public static void drawText(Font font, Component text, float x, float y, int color, PoseStack ps, MultiBufferSource buffer, int light) {
        if (!(buffer instanceof Recorder rec)) return;
        PoseStack copy = new PoseStack();
        copy.last().pose().set(ps.last().pose());
        copy.last().normal().set(ps.last().normal());
        rec.texts.add(new QueuedText(text, x, y, color, copy, light));
    }

    private record QueuedText(Component text, float x, float y, int color, PoseStack pose, int light) {}

    private record QueuedItem(ItemStack stack, ItemDisplayContext ctx, PoseStack pose, int light) {}

    public static final class Recorder implements MultiBufferSource {
        private final List<QueuedItem> items = new ArrayList<>();
        private final List<QueuedText> texts = new ArrayList<>();
        private final Map<RenderType, Buffer> buffers = new LinkedHashMap<>();
        private final int defaultLight;

        public Recorder(int defaultLight) {
            this.defaultLight = defaultLight;
        }

        @Override
        public VertexConsumer getBuffer(RenderType type) {
            return buffers.computeIfAbsent(type, t -> new Buffer(defaultLight));
        }

        public void replay(SubmitNodeCollector collector) {
            Minecraft mc = Minecraft.getInstance();
            for (QueuedText qt : texts) {
                collector.submitText(qt.pose(), qt.x(), qt.y(), qt.text().getVisualOrderText(), false, Font.DisplayMode.NORMAL, qt.light(), qt.color(), 0, 0);
            }
            for (QueuedItem qi : items) {
                ItemStackRenderState st = new ItemStackRenderState();
                mc.getItemModelResolver().updateForTopItem(st, qi.stack(), qi.ctx(), mc.level, null, 0);
                st.submit(qi.pose(), collector, qi.light(), OverlayTexture.NO_OVERLAY, 0);
            }
            for (Map.Entry<RenderType, Buffer> e : buffers.entrySet()) {
                Buffer b = e.getValue();
                b.finish();
                if (b.verts.isEmpty()) continue;
                collector.submitCustomGeometry(new PoseStack(), e.getKey(), (pose, out) -> {
                    for (Vtx v : b.verts) {
                        out.addVertex(v.x, v.y, v.z).setColor(v.r, v.g, v.b, v.a).setUv(v.u, v.v)
                                .setUv1(v.ov & 0xFFFF, v.ov >> 16 & 0xFFFF).setUv2(v.light & 0xFFFF, v.light >> 16 & 0xFFFF)
                                .setNormal(v.nx, v.ny, v.nz);
                    }
                });
            }
        }
    }

    private static final class Vtx {
        float x, y, z, u, v, nx, ny = 1, nz;
        int r = 255, g = 255, b = 255, a = 255;
        int ov = OverlayTexture.NO_OVERLAY, light;
    }

    private static final class Buffer implements VertexConsumer {
        final List<Vtx> verts = new ArrayList<>();
        private Vtx cur;
        private final int defaultLight;

        Buffer(int defaultLight) {
            this.defaultLight = defaultLight;
        }

        void finish() {
            cur = null;
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            cur = new Vtx();
            cur.light = defaultLight;
            cur.x = x; cur.y = y; cur.z = z;
            verts.add(cur);
            return this;
        }

        @Override
        public VertexConsumer setColor(int r, int g, int b, int a) {
            cur.r = r; cur.g = g; cur.b = b; cur.a = a;
            return this;
        }

        @Override
        public VertexConsumer setColor(int argb) {
            return setColor(argb >> 16 & 0xFF, argb >> 8 & 0xFF, argb & 0xFF, argb >>> 24);
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            cur.u = u; cur.v = v;
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            cur.ov = (v & 0xFFFF) << 16 | (u & 0xFFFF);
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            cur.light = (v & 0xFFFF) << 16 | (u & 0xFFFF);
            return this;
        }

        @Override
        public VertexConsumer setLight(int packed) {
            cur.light = packed;
            return this;
        }

        @Override
        public VertexConsumer setOverlay(int packed) {
            cur.ov = packed;
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            cur.nx = x; cur.ny = y; cur.nz = z;
            return this;
        }

        @Override
        public VertexConsumer setLineWidth(float w) {
            return this;
        }
    }
}
