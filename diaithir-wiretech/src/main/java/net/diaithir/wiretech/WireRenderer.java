package net.diaithir.wiretech;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Draws the wires of a connector as sagging ropes. The end with the lower block position draws each wire, so none is drawn twice. */
public final class WireRenderer implements BlockEntityRenderer<ConnectorBlockEntity, WireRenderer.State> {
    private static final int STEPS = 20;

    public static final class State extends BlockEntityRenderState {
        final List<float[]> wires = new ArrayList<>(); // dx, dy, dz of the far end from the start, then colour as r g b
    }

    public WireRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ConnectorBlockEntity be, State state, float partialTicks, Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
        state.wires.clear();
        Level level = be.getLevel();
        if (level == null) return;
        BlockPos pos = be.getBlockPos();
        Vec3 from = be.attach();
        int rgb = be.wire.color;
        for (BlockPos other : be.links()) {
            if (pos.asLong() > other.asLong()) continue;
            Vec3 to = ConnectorBlockEntity.attachOf(other, level.getBlockState(other));
            state.wires.add(new float[] {(float) (from.x - pos.getX()), (float) (from.y - pos.getY()), (float) (from.z - pos.getZ()),
                    (float) (to.x - from.x), (float) (to.y - from.y), (float) (to.z - from.z),
                    ((rgb >> 16) & 255) / 255f, ((rgb >> 8) & 255) / 255f, (rgb & 255) / 255f});
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        for (float[] w : state.wires) {
            collector.submitCustomGeometry(poseStack, RenderTypes.leash(), (pose, buffer) -> rope(pose, buffer, w, state.lightCoords));
        }
    }

    private static void rope(PoseStack.Pose pose, VertexConsumer b, float[] w, int light) {
        float sx = w[0], sy = w[1], sz = w[2], dx = w[3], dy = w[4], dz = w[5];
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        float sag = 0.03f * len;
        float horiz = dx * dx + dz * dz;
        float offX = 0.02f, offZ = 0f;
        if (horiz > 1.0E-4f) {
            float inv = 0.02f / (float) Math.sqrt(horiz);
            offX = dz * inv;
            offZ = -dx * inv;
        }
        for (int pass = 0; pass < 2; pass++) {
            boolean back = pass == 1;
            for (int i = 0; i <= STEPS; i++) {
                int k = back ? STEPS - i : i;
                float t = k / (float) STEPS;
                float shade = (k % 2 == (back ? 1 : 0)) ? 0.75f : 1.0f;
                float x = sx + dx * t, y = sy + dy * t - sag * 4f * t * (1f - t), z = sz + dz * t;
                float lo = back ? 0.0f : 0.04f, hi = back ? 0.04f : 0.0f;
                b.addVertex(pose, x - offX, y + lo, z - offZ).setColor(w[6] * shade, w[7] * shade, w[8] * shade, 1.0f).setLight(light);
                b.addVertex(pose, x + offX, y + hi, z + offZ).setColor(w[6] * shade, w[7] * shade, w[8] * shade, 1.0f).setLight(light);
            }
        }
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 96;
    }
}
