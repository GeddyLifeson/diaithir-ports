package me.desht.pneumaticcraft.client.render.entity.drone;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.client.model.entity.drone.ModelDrone;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;

public class DroneHeldItemLayer extends RenderLayer<DroneRenderState, ModelDrone> {
    DroneHeldItemLayer(RenderLayerParent<DroneRenderState, ModelDrone> renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, DroneRenderState state, float yRot, float xRot) {
        if (state.heldItem.isEmpty()) return;

        poseStack.pushPose();
        // note: transform is currently set up so items render upside down
        poseStack.translate(0.0D, 1.5D, 0.0D);
        if (state.flipHeldItem) {
            poseStack.mulPose(Axis.XP.rotationDegrees(180));
        }
        poseStack.scale(state.heldItemScale, state.heldItemScale, state.heldItemScale);
        state.heldItem.submit(poseStack, collector, lightCoords, LivingEntityRenderer.getOverlayCoords(state, 0.0F), state.outlineColor);
        poseStack.popPose();
    }
}
