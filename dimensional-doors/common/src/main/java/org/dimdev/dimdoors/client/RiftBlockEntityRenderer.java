package org.dimdev.dimdoors.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import org.dimdev.dimdoors.block.entity.RiftBlockEntity;
import org.dimdev.dimdoors.item.ModItems;
import org.jspecify.annotations.Nullable;

import java.util.List;

public abstract class RiftBlockEntityRenderer<T extends RiftBlockEntity, R extends RiftRenderState> implements BlockEntityRenderer<T, R> {
    protected final BlockEntityRendererProvider.Context context;

    public RiftBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void extractRenderState(T blockEntity, R state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.closing = blockEntity.closing;
        state.size = blockEntity.size;
    }

    @Override
    public void submit(R state, PoseStack matrices, SubmitNodeCollector collector, CameraRenderState camera) {
        var minecraft = Minecraft.getInstance();

        if (minecraft.player != null && minecraft.player.getItemInHand(InteractionHand.MAIN_HAND).is(ModItems.RIFT_CONFIGURATION_TOOL)) {
            matrices.pushPose();

            matrices.translate(0.5D, 1.25D, 0.5D);
            matrices.mulPose(camera.orientation);
            matrices.scale(0.025F, -0.025F, 0.025F);

            renderTextLines(List.of(Component.literal("Closing: " + state.closing), Component.literal("Size: " + state.size)), matrices, collector);

            matrices.popPose();
        }
    }

    protected void renderTextLines(List<Component> lines, PoseStack poseStack, SubmitNodeCollector collector) {
        Font font = context.font();
        float backgroundOpacity = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
        int backgroundColor = (int) (backgroundOpacity * 255.0F) << 24;

        int lineHeight = font.lineHeight;
        float startY = -((lines.size() - 1) * lineHeight) / 2.0F;

        for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
            Component line = lines.get(lineIndex);
            float textX = (float) (-font.width(line) / 2);
            float textY = startY + lineIndex * lineHeight;
            collector.submitText(poseStack, textX, textY, line.getVisualOrderText(), false, Font.DisplayMode.SEE_THROUGH, LightCoordsUtil.FULL_BRIGHT, -1, backgroundColor, 0);
        }
    }
}
