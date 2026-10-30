package com.github.alexmodguy.alexscaves.client.render.entity;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.model.FloaterModel;
import com.github.alexmodguy.alexscaves.client.render.ACRenderTypes;
import com.github.alexmodguy.alexscaves.server.entity.item.FloaterEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import com.github.alexthe666.citadel.client.render.legacy.LegacyEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public class FloaterRenderer extends LegacyEntityRenderer<FloaterEntity> {

    private static final FloaterModel FLOATER_MODEL = new FloaterModel();
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/floater.png");

    public FloaterRenderer(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn);
    }


    public void render(FloaterEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int packedLightIn) {
        float ageInTicks = entity.tickCount + partialTicks;

        poseStack.pushPose();
        poseStack.translate(0.0D, (double) 1.5F, 0.0D);
        poseStack.mulPose(Axis.XP.rotationDegrees(180));
        FLOATER_MODEL.setupAnim(entity, 0.0F, 0.0F, ageInTicks, 0.0F, 0.0F);
        VertexConsumer textureVertexConsumer = bufferIn.getBuffer(ACRenderTypes.getGhostly(TEXTURE));
        FLOATER_MODEL.renderToBuffer(poseStack, textureVertexConsumer, packedLightIn, OverlayTexture.NO_OVERLAY, -1);
        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, bufferIn, packedLightIn);
    }

    public Identifier getTextureLocation(FloaterEntity entity) {
        return TEXTURE;
    }
}
