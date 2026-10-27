package net.id.paradise_lost.client.rendering.entity.passive;

import com.mojang.blaze3d.vertex.PoseStack;
import net.id.paradise_lost.client.model.ParadiseLostModelLayers;
import net.id.paradise_lost.client.model.entity.MoaModel;
import net.id.paradise_lost.client.rendering.entity.state.MoaRenderState;
import net.id.paradise_lost.entity.passive.moa.MoaEntity;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class MoaEntityRenderer extends MobRenderer<MoaEntity, MoaRenderState, MoaModel> {

    public MoaEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new MoaModel(context.bakeLayer(ParadiseLostModelLayers.MOA)), 0.7f);
    }

    @Override
    public MoaRenderState createRenderState() {
        return new MoaRenderState();
    }

    @Override
    protected void scale(MoaRenderState state, PoseStack poseStack) {
        float moaScale = state.isBaby ? Math.min(0.43F + (state.ageInTicks * 0.00001f), 0.72f) : 1.0F;
        poseStack.scale(moaScale, moaScale, moaScale);
    }

    @Override
    public void extractRenderState(MoaEntity moa, MoaRenderState state, float partialTicks) {
        super.extractRenderState(moa, state, partialTicks);
        state.texture = moa.getGenes().getTexture();
        state.glowing = moa.getGenes().getRace().glowing();
        if (state.glowing) {
            state.lightCoords = LightCoordsUtil.FULL_BRIGHT;
        }
        state.saddled = moa.isSaddled();
        state.hasChest = moa.hasChest();
        state.inAir = moa.isInAir;
        state.wingRoll = moa.getWingRoll();
        state.wingYaw = moa.getWingYaw();
        state.legPitch = moa.getLegPitch();
        var motion = moa.getDeltaMovement();
        state.horizontalSpeed = (float) Math.sqrt(motion.x * motion.x + motion.z * motion.z);
    }

    @Override
    public Identifier getTextureLocation(MoaRenderState state) {
        return state.texture;
    }
}
