package com.github.alexmodguy.alexscaves.client.render.entity;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.model.LuxtructosaurusModel;
import com.github.alexmodguy.alexscaves.client.render.ACRenderTypes;
import com.github.alexmodguy.alexscaves.client.render.ColorUtil;
import com.github.alexmodguy.alexscaves.server.entity.living.LuxtructosaurusEntity;
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
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;

import javax.annotation.Nullable;
import java.util.HashMap;

public class LuxtructosaurusRenderer extends LegacyMobRenderer<LuxtructosaurusEntity, LuxtructosaurusModel> implements CustomBookEntityRenderer {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/luxtructosaurus.png");
    private static final Identifier TEXTURE_ENRAGED = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/luxtructosaurus_enraged.png");
    private static final Identifier TEXTURE_ENRAGED_GLOW = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/luxtructosaurus_enraged_glow.png");

    private static final HashMap<Integer, Vec3> mouthParticlePositions = new HashMap<>();


    private boolean sepia = false;

    public LuxtructosaurusRenderer(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn, new LuxtructosaurusModel(), 4.0F);
        this.addLayer(new LayerGlow());
    }

    protected void scale(LuxtructosaurusEntity mob, PoseStack matrixStackIn, float partialTicks) {
    }

    public Identifier getTextureLocation(LuxtructosaurusEntity entity) {
        return entity.isEnraged() ? TEXTURE_ENRAGED : TEXTURE;
    }

    @Nullable
    protected RenderType getRenderType(LuxtructosaurusEntity mob, boolean normal, boolean translucent, boolean outline) {
        Identifier resourcelocation = this.getTextureLocation(mob);
        if (translucent) {
            return RenderTypes.entityTranslucent(resourcelocation);
        } else if (normal) {
            return sepia ? ACRenderTypes.getBookWidget(resourcelocation, true) : RenderTypes.entityCutout(resourcelocation);
        } else {
            return outline ? RenderTypes.outline(resourcelocation) : null;
        }
    }

    public void render(LuxtructosaurusEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource source, int packedLight) {
        if(sepia){
            this.model.straighten = true;
        }
        super.render(entity, entityYaw, partialTicks, poseStack, source, packedLight);
        if(sepia){
            this.model.straighten = false;
        }
        mouthParticlePositions.put(entity.getId(), this.model.getMouthPosition(Vec3.ZERO));
    }

    public static Vec3 getMouthPositionFor(int entityId) {
        return mouthParticlePositions.get(entityId);
    }

    public boolean shouldRender(LuxtructosaurusEntity entity, Frustum camera, double x, double y, double z) {
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

    @Override
    public void setSepiaFlag(boolean sepiaFlag) {
        this.sepia = sepiaFlag;
    }

    protected float getFlipDegrees(LuxtructosaurusEntity luxtructosaurus) {
        return 0.0F;
    }

    class LayerGlow extends LegacyLayer<LuxtructosaurusEntity, LuxtructosaurusModel> {

        public LayerGlow() {
            super(LuxtructosaurusRenderer.this);
        }

        public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, LuxtructosaurusEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            float enragedAlpha = ((float) (Math.sin(ageInTicks * 0.2F)) * 0.15F + 0.85F) * entitylivingbaseIn.getEnragedProgress(partialTicks);
            VertexConsumer ivertexbuilder1 = bufferIn.getBuffer(ACRenderTypes.getEyesAlphaEnabled(TEXTURE_ENRAGED_GLOW));
            this.getParentModel().renderToBuffer(matrixStackIn, ivertexbuilder1, packedLightIn, com.github.alexthe666.citadel.client.render.legacy.LegacyMobRenderer.getOverlayCoords(entitylivingbaseIn, 0.0F), ColorUtil.packColor(1.0F, 1.0F, 1.0F, enragedAlpha));
        }
    }
}

