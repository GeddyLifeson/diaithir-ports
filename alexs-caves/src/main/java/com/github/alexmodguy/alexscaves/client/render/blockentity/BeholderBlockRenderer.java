package com.github.alexmodguy.alexscaves.client.render.blockentity;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.model.BeholderModel;
import com.github.alexmodguy.alexscaves.client.render.ColorUtil;
import com.github.alexmodguy.alexscaves.server.block.blockentity.BeholderBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.github.alexthe666.citadel.client.render.legacy.LegacyBlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;

public class BeholderBlockRenderer<T extends BeholderBlockEntity> extends LegacyBlockEntityRenderer<T> {

    private static final BeholderModel MODEL = new BeholderModel();
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/beholder.png");
    private static final Identifier TEXTURE_EYE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/beholder_eye.png");

    protected final RandomSource random = RandomSource.create();

    public BeholderBlockRenderer(BlockEntityRendererProvider.Context rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(T beholder, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 1.5F, 0.5F);
        poseStack.mulPose(Axis.XP.rotationDegrees(-180));
        MODEL.hideEye(beholder.isFirstPersonView(Minecraft.getInstance().getCameraEntity()));
        MODEL.setupAnim(null, beholder.getEyeXRot(partialTicks), beholder.getEyeYRot(partialTicks), beholder.age + partialTicks, 0, 0);
        MODEL.renderToBuffer(poseStack, bufferIn.getBuffer(RenderTypes.entityCutout(TEXTURE)), combinedLightIn, combinedOverlayIn, ColorUtil.packColor(1.0F, 1.0F, 1.0F, 1.0F));
        MODEL.renderToBuffer(poseStack, bufferIn.getBuffer(RenderTypes.eyes(TEXTURE_EYE)), combinedLightIn, combinedOverlayIn, ColorUtil.packColor(1.0F, 1.0F, 1.0F, 1.0F));
        poseStack.popPose();

    }

    public int getViewDistance() {
        return 128;
    }
}
