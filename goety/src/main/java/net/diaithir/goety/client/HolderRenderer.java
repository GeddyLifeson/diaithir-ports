package net.diaithir.goety.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.diaithir.goety.rite.HolderBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Shows the item resting on an altar or a pedestal, turning slowly. */
public class HolderRenderer implements BlockEntityRenderer<HolderBlockEntity, HolderRenderer.State> {
    public static class State extends BlockEntityRenderState {
        public final ItemStackRenderState item = new ItemStackRenderState();
        public float age;
        public float height;
    }

    private final ItemModelResolver items;
    private final float height;

    public HolderRenderer(BlockEntityRendererProvider.Context ctx, float height) {
        this.items = ctx.itemModelResolver();
        this.height = height;
    }

    @Override public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(HolderBlockEntity be, State state, float partial, Vec3 camera, ModelFeatureRenderer.@Nullable CrumblingOverlay overlay) {
        BlockEntityRenderer.super.extractRenderState(be, state, partial, camera, overlay);
        state.height = height;
        state.age = be.getLevel() == null ? 0 : be.getLevel().getGameTime() + partial;
        items.updateForTopItem(state.item, be.held(), ItemDisplayContext.GROUND, be.getLevel(), null, (int) be.getBlockPos().asLong());
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.item.isEmpty()) return;
        pose.pushPose();
        pose.translate(0.5, state.height + Math.sin(state.age / 10.0) * 0.03, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(state.age * 2.0F));
        pose.scale(0.6F, 0.6F, 0.6F);
        state.item.submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        pose.popPose();
    }
}
