package com.github.alexmodguy.alexscaves.client.render.blockentity;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.model.GobthumperModel;
import com.github.alexmodguy.alexscaves.client.render.ColorUtil;
import com.github.alexmodguy.alexscaves.server.block.blockentity.GobthumperBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.github.alexthe666.citadel.client.render.legacy.LegacyBlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.Identifier;

public class GobthumperBlockRenderer<T extends GobthumperBlockEntity> extends LegacyBlockEntityRenderer<T> {

    private static final GobthumperModel MODEL = new GobthumperModel();
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/gobthumper.png");
    private static final Identifier TEXTURE_JELLY = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/gobthumper_jelly.png");

    public GobthumperBlockRenderer(BlockEntityRendererProvider.Context rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(T gobthumper, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 1.5F, 0.5F);
        poseStack.mulPose(Axis.XP.rotationDegrees(-180));
        MODEL.setupAnim(null, 0.0F, 0.0F, gobthumper.getThumpTime(partialTicks), 0, 0);
        MODEL.renderToBuffer(poseStack, bufferIn.getBuffer(RenderTypes.entityCutout(TEXTURE)), combinedLightIn, combinedOverlayIn, ColorUtil.packColor(1.0F, 1.0F, 1.0F, 1.0F));
        MODEL.renderToBuffer(poseStack, bufferIn.getBuffer(RenderTypes.entityTranslucent(TEXTURE_JELLY)), combinedLightIn, combinedOverlayIn, ColorUtil.packColor(1.0F, 1.0F, 1.0F, 1.0F));
        poseStack.popPose();

    }

    public int getViewDistance() {
        return 128;
    }
}
