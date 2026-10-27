package com.github.alexthe666.citadel.client.render.legacy;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Ambient collector for legacy renderers. While a legacy render() runs, items, blocks and nested entities can be submitted
 * straight to the 26.1 pipeline (they cannot be captured as plain vertices).
 */
public final class LegacyRenderContext {
    private static SubmitNodeCollector collector;
    private static CameraRenderState camera;
    private static EntityRendererProvider.Context context;

    private LegacyRenderContext() {
    }

    static void begin(SubmitNodeCollector c, CameraRenderState cam, EntityRendererProvider.Context ctx) {
        collector = c;
        camera = cam;
        context = ctx;
    }

    static void end() {
        collector = null;
        camera = null;
        context = null;
    }

    public static boolean active() {
        return collector != null;
    }

    public static CameraRenderState camera() {
        return camera;
    }

    public static void renderItem(LivingEntity entity, ItemStack stack, ItemDisplayContext display, PoseStack poseStack, int light) {
        if (collector == null || stack.isEmpty()) return;
        ItemStackRenderState st = new ItemStackRenderState();
        Minecraft.getInstance().getItemModelResolver().updateForLiving(st, stack, display, entity);
        st.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
    }

    public static void renderItem(Entity entity, ItemStack stack, ItemDisplayContext display, PoseStack poseStack, int light) {
        if (collector == null || stack.isEmpty()) return;
        ItemStackRenderState st = new ItemStackRenderState();
        Minecraft.getInstance().getItemModelResolver().updateForNonLiving(st, stack, display, entity);
        st.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
    }

    public static void renderBlock(BlockState state, PoseStack poseStack, int light) {
        if (collector == null || context == null) return;
        BlockModelRenderState bs = new BlockModelRenderState();
        context.getBlockModelResolver().update(bs, state, BlockDisplayContext.create());
        bs.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
    }

    /** Draws another entity with its own renderer at (x, y, z) relative to the current pose. */
    @SuppressWarnings("unchecked")
    public static void renderEntity(Entity entity, double x, double y, double z, float yaw, float partialTicks, PoseStack poseStack, int light) {
        if (collector == null || camera == null) return;
        var dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderState st = dispatcher.extractEntity(entity, partialTicks);
        st.lightCoords = light;
        dispatcher.submit(st, camera, st.x - x, st.y - y, st.z - z, poseStack, collector);
    }
}
