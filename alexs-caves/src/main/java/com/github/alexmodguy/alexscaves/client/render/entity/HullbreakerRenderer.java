package com.github.alexmodguy.alexscaves.client.render.entity;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.model.HullbreakerModel;
import com.github.alexmodguy.alexscaves.client.render.ACRenderTypes;
import com.github.alexmodguy.alexscaves.client.render.ColorUtil;
import com.github.alexmodguy.alexscaves.server.entity.living.HullbreakerEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import com.github.alexthe666.citadel.client.render.legacy.LegacyMobRenderer;
import com.github.alexthe666.citadel.client.render.legacy.LegacyLayer;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.entity.PartEntity;

import javax.annotation.Nullable;

public class HullbreakerRenderer extends LegacyMobRenderer<HullbreakerEntity, HullbreakerModel> implements CustomBookEntityRenderer {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/hullbreaker.png");
    private static final Identifier TEXTURE_GLOW = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/hullbreaker_glow.png");
    private boolean sepia;

    public HullbreakerRenderer(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn, new HullbreakerModel(), 2.25F);
        this.addLayer(new LayerGlow());
    }

    public void render(HullbreakerEntity entity, float f1, float partialTicks, PoseStack poseStack, MultiBufferSource source, int light) {
        this.model.straighten = sepia;
        super.render(entity, f1, partialTicks, poseStack, source, light);
    }

        @Nullable
    protected RenderType getRenderType(HullbreakerEntity mob, boolean normal, boolean translucent, boolean outline) {
        Identifier resourcelocation = this.getTextureLocation(mob);
        if (translucent) {
            return RenderTypes.entityTranslucent(resourcelocation);
        } else if (normal) {
            return sepia ? ACRenderTypes.getBookWidget(resourcelocation, true) : RenderTypes.entityTranslucent(resourcelocation);
        } else {
            return outline ? RenderTypes.outline(resourcelocation) : null;
        }
    }

    protected void scale(HullbreakerEntity mob, PoseStack matrixStackIn, float partialTicks) {
    }

    public Identifier getTextureLocation(HullbreakerEntity entity) {
        return TEXTURE;
    }

    public boolean shouldRender(HullbreakerEntity entity, Frustum camera, double x, double y, double z) {
        if (super.shouldRender(entity, camera, x, y, z)) {
            return true;
        } else {
            for (PartEntity part : entity.getParts()) {
                if (camera.isVisible(part.getBoundingBox())) {
                    return true;
                }
            }
            return false;
        }
    }

    protected float getFlipDegrees(HullbreakerEntity hullbreakerEntity) {
        return 0.0F;
    }

    @Override
    public void setSepiaFlag(boolean sepiaFlag) {
        this.sepia = sepiaFlag;
    }

    class LayerGlow extends LegacyLayer<HullbreakerEntity, HullbreakerModel> {

        public LayerGlow() {
            super(HullbreakerRenderer.this);
        }

        public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, HullbreakerEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            VertexConsumer ivertexbuilder = bufferIn.getBuffer(ACRenderTypes.getEyesAlphaEnabled(TEXTURE_GLOW));
            float alpha = (float) ((Math.sin(entitylivingbaseIn.getPulseAmount(partialTicks)) + 1.0F) * 0.5F);
            this.getParentModel().renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, com.github.alexthe666.citadel.client.render.legacy.LegacyMobRenderer.getOverlayCoords(entitylivingbaseIn, 0.0F), ColorUtil.packColor(1.0F, 1.0F, 1.0F, alpha));
        }
    }
}


