package net.id.paradise_lost.client.rendering.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.id.paradise_lost.block.blockentity.IncubatorBlockEntity;
import net.id.paradise_lost.item.misc.MoaEggItem;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class IncubatorBlockEntityRenderer implements BlockEntityRenderer<IncubatorBlockEntity, IncubatorBlockEntityRenderer.State> {

    public static class State extends BlockEntityRenderState {
        public final ItemStackRenderState item = new ItemStackRenderState();
        public float offsetHeight;
        public Direction itemFacing = Direction.NORTH;
        public boolean moaEgg;
    }

    private final ItemModelResolver itemModelResolver;

    public IncubatorBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.itemModelResolver = ctx.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(IncubatorBlockEntity entity, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
        state.item.clear();
        if (entity.hasItem()) {
            state.offsetHeight = entity.getOffsetHeight();
            state.itemFacing = entity.getItemFacing();
            state.moaEgg = entity.getItem().getItem() instanceof MoaEggItem;
            this.itemModelResolver.updateForTopItem(state.item, entity.getItem(), ItemDisplayContext.FIXED, entity.getLevel(), null, (int) entity.getBlockPos().asLong());
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.item.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5, state.offsetHeight, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRotForFacing(state.itemFacing)));
        if (state.moaEgg) {
            poseStack.scale(1F, 1F, 5F);
        } else {
            poseStack.scale(0.9F, 0.9F, 0.9F);
        }
        state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    private static float yRotForFacing(Direction facing) {
        return switch (facing) {
            case EAST -> 90.0F;
            case NORTH -> 180.0F;
            case WEST -> 270.0F;
            default -> 0.0F;
        };
    }
}
