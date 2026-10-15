package me.desht.pneumaticcraft.client.render.entity.drone;

import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.client.model.entity.drone.ModelDrone;
import me.desht.pneumaticcraft.client.model.entity.drone.ModelDroneCore;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.item.DyeColor;

public class DroneColourLayer extends RenderLayer<DroneRenderState, ModelDrone> {
    private final ModelDroneCore model;

    DroneColourLayer(RenderLayerParent<DroneRenderState, ModelDrone> rendererIn, EntityRendererProvider.Context ctx) {
        super(rendererIn);

        model = new ModelDroneCore(ctx.bakeLayer(PNCModelLayers.DRONE_CORE));
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, DroneRenderState state, float yRot, float xRot) {
        int col = 0xFF000000 | DyeColor.byId(state.droneColor).getTextureDiffuseColor();
        collector.submitModel(model, state, poseStack, RenderTypes.entityCutout(Textures.DRONE_ENTITY), lightCoords,
                LivingEntityRenderer.getOverlayCoords(state, 0.0F), col, null, state.outlineColor, null);
    }
}
