package net.diaithir.vehicles.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.diaithir.vehicles.tracks.TrackedChassis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;

/** Placeholder body for the tracked chassis: a hull block between two track blocks, until the redrawn model lands. */
public class ChassisRenderer extends EntityRenderer<TrackedChassis, ChassisRenderer.State> {
    private static final BlockDisplayContext CONTEXT = BlockDisplayContext.create();
    private final BlockModelResolver resolver;

    public static class State extends EntityRenderState {
        public final BlockModelRenderState hull = new BlockModelRenderState();
        public final BlockModelRenderState track = new BlockModelRenderState();
        public float yRot;
    }

    public ChassisRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.shadowRadius = 1.5F;
        this.resolver = ctx.getBlockModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TrackedChassis e, State s, float pt) {
        super.extractRenderState(e, s, pt);
        s.yRot = e.getYRot();
        resolver.update(s.hull, BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("diaithirbridges", "carrier_tank")).defaultBlockState(), CONTEXT);
        resolver.update(s.track, Blocks.POLISHED_BLACKSTONE.defaultBlockState(), CONTEXT);
    }

    @Override
    public void submit(State s, PoseStack ps, SubmitNodeCollector c, CameraRenderState cam) {
        ps.pushPose();
        ps.mulPose(Axis.YP.rotationDegrees(180.0F - s.yRot));
        part(s.hull, ps, c, s, 0.0F, 0.35F, 2.0F, 0.7F, 3.2F);
        part(s.track, ps, c, s, -1.25F, 0.0F, 0.6F, 0.6F, 3.6F);
        part(s.track, ps, c, s, 1.25F, 0.0F, 0.6F, 0.6F, 3.6F);
        ps.popPose();
        super.submit(s, ps, c, cam);
    }

    private static void part(BlockModelRenderState b, PoseStack ps, SubmitNodeCollector c, State s, float x, float y, float w, float h, float l) {
        ps.pushPose();
        ps.translate(x, y, 0.0F);
        ps.scale(w, h, l);
        ps.translate(-0.5F, 0.0F, -0.5F);
        b.submit(ps, c, s.lightCoords, OverlayTexture.NO_OVERLAY, s.outlineColor);
        ps.popPose();
    }
}
