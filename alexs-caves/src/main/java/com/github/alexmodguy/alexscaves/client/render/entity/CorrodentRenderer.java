package com.github.alexmodguy.alexscaves.client.render.entity;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.model.CorrodentModel;
import com.github.alexmodguy.alexscaves.client.render.ColorUtil;
import com.github.alexmodguy.alexscaves.server.entity.living.CorrodentEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import com.github.alexthe666.citadel.client.render.legacy.LegacyMobRenderer;
import com.github.alexthe666.citadel.client.render.legacy.LegacyLayer;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class CorrodentRenderer extends LegacyMobRenderer<CorrodentEntity, CorrodentModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/corrodent.png");
    private static final Identifier TEXTURE_EYES = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/corrodent_eyes.png");
    private static final Map<BlockPos, Integer> allDugBlocksOnScreen = new HashMap<>();

    public CorrodentRenderer(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn, new CorrodentModel(), 0.5F);
        this.addLayer(new LayerGlow());
    }

    public Identifier getTextureLocation(CorrodentEntity entity) {
        return TEXTURE;
    }

    public void render(CorrodentEntity entityIn, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int packedLightIn) {
        super.render(entityIn, entityYaw, partialTicks, poseStack, bufferIn, packedLightIn);
        double x = Mth.lerp(partialTicks, entityIn.xOld, entityIn.getX());
        double y = Mth.lerp(partialTicks, entityIn.yOld, entityIn.getY());
        double z = Mth.lerp(partialTicks, entityIn.zOld, entityIn.getZ());
        float digAmount = entityIn.getDigAmount(partialTicks);
        if (digAmount > 0) {
            double digEffectDistance = 3;
            for (BlockPos mutableBlockPos : BlockPos.betweenClosed((int) Math.floor(x - digEffectDistance), (int) Math.floor(y - digEffectDistance), (int) Math.floor(z - digEffectDistance), (int) Math.floor(x + digEffectDistance), (int) Math.floor(y + digEffectDistance), (int) Math.floor(z + digEffectDistance))) {
                int amount = (int) (entityIn.getCorrosionAmount(mutableBlockPos) * digAmount);
                if (amount >= 0) {
                    allDugBlocksOnScreen.put(mutableBlockPos.immutable(), Math.max(allDugBlocksOnScreen.getOrDefault(mutableBlockPos, -1), amount));
                }
            }
        }
    }

    

    class LayerGlow extends LegacyLayer<CorrodentEntity, CorrodentModel> {

        public LayerGlow() {
            super(CorrodentRenderer.this);
        }

        public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, CorrodentEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            VertexConsumer ivertexbuilder = bufferIn.getBuffer(RenderTypes.eyes(TEXTURE_EYES));
            float alpha = 1.0F;
            this.getParentModel().renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, com.github.alexthe666.citadel.client.render.legacy.LegacyMobRenderer.getOverlayCoords(entitylivingbaseIn, 0.0F), ColorUtil.packColor(1.0F, 1.0F, 1.0F, alpha));
        }
    }
}


