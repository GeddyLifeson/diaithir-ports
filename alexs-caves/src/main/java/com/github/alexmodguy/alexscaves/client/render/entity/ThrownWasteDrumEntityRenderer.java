package com.github.alexmodguy.alexscaves.client.render.entity;

import net.minecraft.client.renderer.rendertype.RenderType;
import com.github.alexmodguy.alexscaves.server.block.ACBlockRegistry;
import com.github.alexmodguy.alexscaves.server.entity.item.ThrownWasteDrumEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import com.github.alexthe666.citadel.client.render.legacy.LegacyEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public class ThrownWasteDrumEntityRenderer extends LegacyEntityRenderer<ThrownWasteDrumEntity> {

    public ThrownWasteDrumEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    public void render(ThrownWasteDrumEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource source, int lightIn) {
        super.render(entity, entityYaw, partialTicks, poseStack, source, lightIn);
        float ageInTicks = entity.tickCount + partialTicks;
        float progress = (entity.getOnGroundFor() + partialTicks) / ThrownWasteDrumEntity.MAX_TIME;
        if (progress > 1.0F) {
            return;
        }
        float expandScale = 1F + (float) Math.sin(progress * progress * Math.PI) * 0.5F;
        poseStack.pushPose();
        poseStack.scale(1F, 1 - progress * 0.03F, 1F);
        poseStack.pushPose();
        poseStack.scale(expandScale, expandScale - progress * 0.3F, expandScale);
        poseStack.translate(0D, 0.5D, 0D);
        if (entity.onGround()) {
            poseStack.mulPose(Axis.XP.rotationDegrees(90));
        } else {
            poseStack.mulPose(Axis.YN.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) (ageInTicks * 25)));
        }
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        BlockState state = ACBlockRegistry.WASTE_DRUM.get().defaultBlockState();
        com.github.alexthe666.citadel.client.render.legacy.LegacyRenderContext.renderBlock(state, poseStack, lightIn);
        poseStack.popPose();
        poseStack.popPose();
    }

    public Identifier getTextureLocation(ThrownWasteDrumEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}

