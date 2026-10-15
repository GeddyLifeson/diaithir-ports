package me.desht.pneumaticcraft.client.render.entity.drone;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.client.model.entity.drone.ModelDrone;
import me.desht.pneumaticcraft.client.render.legacy.RecordingBufferSource;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public class DroneTargetLaserLayer extends RenderLayer<DroneRenderState, ModelDrone> {
    DroneTargetLaserLayer(RenderLayerParent<DroneRenderState, ModelDrone> rendererer) {
        super(rendererer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, DroneRenderState state, float yRot, float xRot) {
        if (state.targetLine != null && state.oldTargetLine != null) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.XP.rotationDegrees(180));
            poseStack.translate(0, -1.5, 0);
            poseStack.scale(2f, 2f, 2f);
            RecordingBufferSource buffer = new RecordingBufferSource(collector);
            RenderUtils.renderProgressingLine3d(state.oldTargetLine, state.targetLine, state.partialTick, poseStack, buffer.getBuffer(RenderTypes.lines()), 0xFFFF0000);
            buffer.submit();
            poseStack.popPose();
        }
    }
}
