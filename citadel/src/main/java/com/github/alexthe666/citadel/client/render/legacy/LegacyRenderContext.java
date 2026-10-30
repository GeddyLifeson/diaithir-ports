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

    private static final java.util.Set<net.minecraft.world.entity.EntityType<?>> FAILED = java.util.concurrent.ConcurrentHashMap.newKeySet();

    /** Snapshot of the ambient state, so nested legacy renders restore the outer render's context. */
    record Saved(SubmitNodeCollector collector, CameraRenderState camera, EntityRendererProvider.Context context, RecordingBufferSource buffers) {
    }

    static Saved begin(SubmitNodeCollector c, CameraRenderState cam, EntityRendererProvider.Context ctx, RecordingBufferSource rec) {
        Saved prev = new Saved(collector, camera, context, LegacyBuffers.peek());
        collector = c;
        camera = cam;
        context = ctx;
        LegacyBuffers.set(rec);
        return prev;
    }

    static void end(Saved prev) {
        collector = prev.collector();
        camera = prev.camera();
        context = prev.context();
        LegacyBuffers.set(prev.buffers());
    }

    /**
     * Runs a legacy render callback with the ambient state bound. The pose stack and ambient state are always restored;
     * a failing renderer is logged once per entity type and skipped (returns false) instead of crashing the frame.
     */
    static boolean guarded(SubmitNodeCollector c, CameraRenderState cam, EntityRendererProvider.Context ctx, RecordingBufferSource rec,
                           PoseStack poseStack, net.minecraft.world.entity.EntityType<?> type, Runnable body) {
        Saved prev = begin(c, cam, ctx, rec);
        poseStack.pushPose();
        PoseStack.Pose base = poseStack.last();
        try {
            body.run();
            return true;
        } catch (RuntimeException | LinkageError ex) {
            if (FAILED.add(type)) {
                com.mojang.logging.LogUtils.getLogger().error("Legacy renderer failed for {}, skipping", type, ex);
            }
            return false;
        } finally {
            // drop any poses the callback or its layers left behind, then our own
            while (!poseStack.isEmpty() && poseStack.last() != base) {
                poseStack.popPose();
            }
            poseStack.popPose();
            end(prev);
        }
    }

    public static void beginPublic(SubmitNodeCollector c) {
        collector = c;
    }

    public static void endPublic() {
        collector = null;
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
        if (collector == null) return;
        BlockModelRenderState bs = new BlockModelRenderState();
        Minecraft.getInstance().getBlockModelResolver().update(bs, state, BlockDisplayContext.create());
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

    public static void renderItemStack(ItemStack stack, ItemDisplayContext display, PoseStack poseStack, int light) {
        if (collector == null || stack.isEmpty()) return;
        ItemStackRenderState st = new ItemStackRenderState();
        Minecraft.getInstance().getItemModelResolver().updateForTopItem(st, stack, display, Minecraft.getInstance().level, null, 0);
        st.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
    }

    /** Draws the 12 edges of a box as lines (1.21 LevelRenderer.renderLineBox replacement). */
    public static void renderLineBox(PoseStack poseStack, com.mojang.blaze3d.vertex.VertexConsumer vc, net.minecraft.world.phys.AABB b, float r, float g, float bl, float a) {
        org.joml.Matrix4f m = poseStack.last().pose();
        float x0 = (float) b.minX, y0 = (float) b.minY, z0 = (float) b.minZ, x1 = (float) b.maxX, y1 = (float) b.maxY, z1 = (float) b.maxZ;
        float[][] e = {{x0,y0,z0,x1,y0,z0},{x0,y1,z0,x1,y1,z0},{x0,y0,z1,x1,y0,z1},{x0,y1,z1,x1,y1,z1},
            {x0,y0,z0,x0,y1,z0},{x1,y0,z0,x1,y1,z0},{x0,y0,z1,x0,y1,z1},{x1,y0,z1,x1,y1,z1},
            {x0,y0,z0,x0,y0,z1},{x1,y0,z0,x1,y0,z1},{x0,y1,z0,x0,y1,z1},{x1,y1,z0,x1,y1,z1}};
        for (float[] l : e) {
            float nx = l[3] - l[0], ny = l[4] - l[1], nz = l[5] - l[2];
            vc.addVertex(m, l[0], l[1], l[2]).setColor(r, g, bl, a).setNormal(nx, ny, nz).setLineWidth(1.5F);
            vc.addVertex(m, l[3], l[4], l[5]).setColor(r, g, bl, a).setNormal(nx, ny, nz).setLineWidth(1.5F);
        }
    }
}
