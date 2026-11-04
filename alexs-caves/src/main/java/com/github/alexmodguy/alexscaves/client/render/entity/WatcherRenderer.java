package com.github.alexmodguy.alexscaves.client.render.entity;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.model.WatcherModel;
import com.github.alexmodguy.alexscaves.client.render.ACRenderTypes;
import com.github.alexmodguy.alexscaves.server.entity.living.WatcherEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import com.github.alexthe666.citadel.client.render.legacy.LegacyMobRenderer;
import com.github.alexthe666.citadel.client.render.legacy.LegacyLayer;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;

import javax.annotation.Nullable;
import net.neoforged.neoforge.common.NeoForge;
import com.github.alexmodguy.alexscaves.client.render.ColorUtil;

public class WatcherRenderer extends LegacyMobRenderer<WatcherEntity, WatcherModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/watcher.png");
    private static final Identifier TEXTURE_MOTH = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/watcher_moth.png");
    private static final Identifier TEXTURE_EYESPOTS = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/watcher_eyespots.png");

    public WatcherRenderer(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn, new WatcherModel(), 0.5F);
        this.addLayer(new LayerGlow());
    }

    protected void scale(WatcherEntity mob, PoseStack matrixStackIn, float partialTicks) {
    }

    protected int getModelColor(WatcherEntity entity, float partialTicks) {
        return ColorUtil.packColor(1.0F, 1.0F, 1.0F, getWatcherTransparency(entity, partialTicks));
    }

    private float getWatcherTransparency(WatcherEntity entity, float partialTicks) {
        return (1F - entity.getShadeAmount(partialTicks)) * 0.8F + 0.2F;
    }

    @Nullable
    protected RenderType getRenderType(WatcherEntity entity, boolean visible, boolean invisible, boolean glowing) {
        Identifier resourcelocation = this.getTextureLocation(entity);
        return RenderTypes.entityTranslucent(resourcelocation);
    }

    public Identifier getTextureLocation(WatcherEntity entity) {
        return TEXTURE;
    }

    class LayerGlow extends LegacyLayer<WatcherEntity, WatcherModel> {

        public LayerGlow() {
            super(WatcherRenderer.this);
        }

        public void render(PoseStack poseStack, MultiBufferSource bufferIn, int packedLightIn, WatcherEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            VertexConsumer ivertexbuilder1 = bufferIn.getBuffer(RenderTypes.entityCutout(TEXTURE_MOTH));
            this.getParentModel().renderToBuffer(poseStack, ivertexbuilder1, packedLightIn, com.github.alexthe666.citadel.client.render.legacy.LegacyMobRenderer.getOverlayCoords(entitylivingbaseIn, 0.0F), -1);
            VertexConsumer ivertexbuilder2 = bufferIn.getBuffer(ACRenderTypes.getEyesAlphaEnabled(TEXTURE_EYESPOTS));
            this.getParentModel().renderToBuffer(poseStack, ivertexbuilder2, packedLightIn, com.github.alexthe666.citadel.client.render.legacy.LegacyMobRenderer.getOverlayCoords(entitylivingbaseIn, 0.0F), ColorUtil.packColor(1.0F, 1.0F, 1.0F, 0.66F));
        }
    }
}

