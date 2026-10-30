package rpggods.client.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import rpggods.blockentity.BrazierBlockEntity;

public class BrazierBlockEntityRenderer implements BlockEntityRenderer<BrazierBlockEntity, BrazierBlockEntityRenderer.State> {

    protected final ItemModelResolver itemModelResolver;

    public BrazierBlockEntityRenderer(final BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(BrazierBlockEntity blockEntity, State state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        final ItemStack stack = blockEntity.getItem(0);
        state.empty = stack.isEmpty();
        if (!state.empty && blockEntity.getLevel() != null) {
            this.itemModelResolver.updateForTopItem(state.item, stack, ItemDisplayContext.GROUND, blockEntity.getLevel(), null, 0);
            state.ticks = blockEntity.getLevel().getGameTime() + blockEntity.getBlockPos().hashCode() + partialTicks;
        } else {
            state.item.clear();
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.empty) {
            final float speed = 0.125F;
            poseStack.pushPose();
            final float offsetY = 0.065F * Mth.cos(state.ticks * speed);
            poseStack.translate(0.5D, 0.95D + offsetY, 0.5D);
            poseStack.mulPose(Axis.YP.rotation(state.ticks * speed * Mth.PI * 0.125F));
            state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }

    public static class State extends net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState {
        public final ItemStackRenderState item = new ItemStackRenderState();
        public boolean empty = true;
        public float ticks;
    }
}
