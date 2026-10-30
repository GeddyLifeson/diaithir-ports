package com.github.alexmodguy.alexscaves.client.render.entity;

import net.minecraft.client.renderer.rendertype.RenderType;
import com.github.alexmodguy.alexscaves.server.entity.item.DesolateDaggerEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import com.github.alexthe666.citadel.client.render.legacy.LegacyEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

public class DesolateDaggerRenderer extends LegacyEntityRenderer<DesolateDaggerEntity> {

    public DesolateDaggerRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    public void render(DesolateDaggerEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource source, int lightIn) {
        super.render(entity, entityYaw, partialTicks, poseStack, source, lightIn);
        float ageInTicks = partialTicks + entity.tickCount;
        double stab = Math.max(entity.getStab(partialTicks), Math.sin(ageInTicks * 0.1F) * 0.2F);
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.5D, 0.0D);
        poseStack.mulPose(Axis.YN.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) + 90.0F));
        poseStack.mulPose(Axis.ZN.rotationDegrees((float) (Mth.lerp(partialTicks, entity.xRotO, entity.getXRot()) + 5F * Math.sin(ageInTicks * 0.2F))));
        poseStack.mulPose(Axis.ZN.rotationDegrees(45));
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        float f = 1.0F;
        float f1 = 0;
        float f2 = 0;
        float startAlpha = ageInTicks < 3 ? 0 : (ageInTicks - 3) / 6F;
        float alpha = (float) Math.min(0.6F + stab, Math.min(1F, startAlpha));
        int redOverlay = OverlayTexture.pack(OverlayTexture.u(0), OverlayTexture.v(true));
        poseStack.translate(stab, stab + Math.cos(ageInTicks * 0.1F) * 0.2F, 0);
        com.github.alexthe666.citadel.client.render.legacy.LegacyRenderContext.renderItem(entity, entity.daggerRenderStack, net.minecraft.world.item.ItemDisplayContext.NONE, poseStack, 240);
        poseStack.popPose();
    }

    

    

    public Identifier getTextureLocation(DesolateDaggerEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}

