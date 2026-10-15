package me.desht.pneumaticcraft.client.render.entity.drone;

import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.pneumaticcraft.client.model.ModelMinigun;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.client.model.entity.drone.ModelDrone;
import me.desht.pneumaticcraft.client.render.legacy.RecordingBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;

public class DroneMinigunLayer extends RenderLayer<DroneRenderState, ModelDrone> {
    private final ModelMinigun modelDroneMinigun;

    DroneMinigunLayer(RenderLayerParent<DroneRenderState, ModelDrone> renderer, EntityRendererProvider.Context ctx) {
        super(renderer);

        modelDroneMinigun = new ModelMinigun(ctx.bakeLayer(PNCModelLayers.MINIGUN));
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, DroneRenderState state, float yRot, float xRot) {
        if (state.minigun != null) {
            RecordingBufferSource buffer = new RecordingBufferSource(collector);
            modelDroneMinigun.renderMinigun(poseStack, buffer, lightCoords, LivingEntityRenderer.getOverlayCoords(state, 0.0F), state.minigun, state.partialTick, true);
            buffer.submit();
        }
    }
}
