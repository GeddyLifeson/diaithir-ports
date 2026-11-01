package com.github.ysbbbbbb.kaleidoscopetavern.client.render.block;

import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.deco.SandwichBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.client.render.block.state.TextBlockRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;

public class SandwichBlockEntityRender extends TextBlockEntityRender<SandwichBlockEntity, TextBlockRenderState> {
    private static final float TEXT_SCALE = 0.01f;
    private static final int LINE_HEIGHT = 10;
    private static final int MAX_LINES = 8;
    private static final int MAX_WIDTH = 55;

    public SandwichBlockEntityRender(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public TextBlockRenderState createRenderState() {
        return new TextBlockRenderState();
    }

    @Override
    public void submit(TextBlockRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                       CameraRenderState camera) {
        if (state.text == null || state.text.isEmpty()) {
            return;
        }

        poseStack.pushPose();

        switch (state.facing) {
            case SOUTH -> {
                poseStack.translate(0.5, 1.06, 0.56);
                poseStack.mulPose(Axis.XN.rotationDegrees(22.5f));
            }
            case NORTH -> {
                poseStack.translate(0.5, 1.06, 0.44);
                poseStack.mulPose(Axis.XP.rotationDegrees(22.5f));
            }
            case EAST -> {
                poseStack.translate(0.56, 1.06, 0.5);
                poseStack.mulPose(Axis.ZP.rotationDegrees(22.5f));
            }
            case WEST -> {
                poseStack.translate(0.44, 1.06, 0.5);
                poseStack.mulPose(Axis.ZN.rotationDegrees(22.5f));
            }
            default -> {
            }
        }

        poseStack.mulPose(Axis.YN.rotationDegrees(state.facing.get2DDataValue() * 90));
        doTextRender(state, poseStack, submitNodeCollector, state.text, MAX_WIDTH, TEXT_SCALE, MAX_LINES, LINE_HEIGHT);

        poseStack.popPose();
    }

    @Override
    protected Component getRenderText(String text) {
        return Component.literal(text).withStyle(ChatFormatting.BOLD);
    }
}
