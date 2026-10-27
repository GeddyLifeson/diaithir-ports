package org.dimdev.dimdoors.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.dimdev.dimdoors.DimensionalDoors;
import org.dimdev.dimdoors.api.util.RGBA;
import org.dimdev.dimdoors.block.entity.DetachedRiftBlockEntity;
import org.dimdev.dimdoors.block.entity.RiftBlockEntity;
import org.dimdev.dimdoors.client.tesseract.Tesseract;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class DetachedRiftBlockEntityRenderer extends RiftBlockEntityRenderer<DetachedRiftBlockEntity, DetachedRiftBlockEntityRenderer.State> {
    public static final Identifier TESSERACT_PATH = DimensionalDoors.id("textures/other/tesseract.png");
    private static final RGBA DEFAULT_COLOR = new RGBA(1, 0.5f, 1, 1);

    public static class State extends RiftRenderState {
        public float riftYaw;
        public int curveId;
        public RGBA color = RGBA.NONE;
        public float angle;
    }

    public DetachedRiftBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(DetachedRiftBlockEntity rift, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        super.extractRenderState(rift, state, partialTicks, cameraPosition, breakProgress);
        state.riftYaw = rift.riftYaw;
        state.curveId = rift.getCurveID();
        state.color = rift.getColor();
        rift.renderAngle = (rift.renderAngle + 5 * partialTicks) % 360;
        state.angle = (float) rift.renderAngle;
    }

    @Override
    public void submit(State state, PoseStack matrices, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, matrices, collector, camera);

        if (DimensionalDoors.getConfig().getGraphicsConfig().showRiftCore || RiftBlockEntity.showRiftCoreUntil - System.currentTimeMillis() >= 0) {
            collector.submitCustomGeometry(matrices, RenderTypes.entityCutout(TESSERACT_PATH), (pose, vc) -> this.renderTesseract(vc, state, pose, matrices));
        }

        collector.submitCustomGeometry(matrices, RenderTypes.entityCutout(TESSERACT_PATH), (pose, vc) -> this.renderCrack(vc, state, pose));
    }

    private void renderCrack(VertexConsumer vc, State state, PoseStack.Pose pose) {
        PoseStack matrices = new PoseStack();
        matrices.last().pose().set(pose.pose());
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.mulPose(Axis.YP.rotationDegrees(state.riftYaw));
        RiftCrackRenderer.drawCrack(matrices.last().pose(), vc, 0, RiftCurves.CURVES.get(state.curveId), DimensionalDoors.getConfig().getGraphicsConfig().riftSize * state.size / 150, 0);
    }

    private void renderTesseract(VertexConsumer vc, State state, PoseStack.Pose pose, PoseStack unused) {
        float radian = (float) (state.angle * Mth.DEG_TO_RAD);
        RGBA color = state.color;
        if (Objects.equals(color, RGBA.NONE)) {
            color = DEFAULT_COLOR;
        }

        PoseStack matrices = new PoseStack();
        matrices.last().pose().set(pose.pose());
        matrices.translate(0.5, 0.5, 0.5);
        matrices.scale(0.25f, 0.25f, 0.25f);

        Tesseract.draw(matrices.last().pose(), vc, color, radian);
    }
}
