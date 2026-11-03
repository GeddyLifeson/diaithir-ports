// SPDX-License-Identifier: GPL-3.0-only
// Folded from MrCrayfish's Vehicle Mod (Unofficial 1.20.1 port by bay4lly; original by MrCrayfish, GPL). Rewritten for NeoForge 26.1.2; art is not used.
package net.diaithir.vehicles.crayfish.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.diaithir.vehicles.crayfish.TankTrailer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

/** Draws the tank trailer as a long fuel tank block (placeholder body until the redrawn trailer model lands). */
public class TrailerRenderer extends EntityRenderer<TankTrailer, TrailerRenderer.State> {
    private static final BlockDisplayContext CONTEXT = BlockDisplayContext.create();
    private final BlockModelResolver resolver;

    public static class State extends EntityRenderState {
        public final BlockModelRenderState body = new BlockModelRenderState();
        public float yRot;
        public float xRot;
    }

    public TrailerRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.shadowRadius = 0.8F;
        this.resolver = ctx.getBlockModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TankTrailer e, State s, float pt) {
        super.extractRenderState(e, s, pt);
        s.yRot = e.getYRot();
        s.xRot = e.getXRot();
        Block b = BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("diaithirvehicles", "fuel_tank"));
        resolver.update(s.body, b.defaultBlockState(), CONTEXT);
    }

    @Override
    public void submit(State s, PoseStack ps, SubmitNodeCollector c, CameraRenderState cam) {
        ps.pushPose();
        ps.mulPose(Axis.YP.rotationDegrees(180.0F - s.yRot));
        ps.mulPose(Axis.XP.rotationDegrees(s.xRot));
        ps.translate(0.0F, 0.35F, 0.0F);
        ps.scale(1.2F, 0.9F, 1.8F);
        ps.translate(-0.5F, 0.0F, -0.5F);
        s.body.submit(ps, c, s.lightCoords, OverlayTexture.NO_OVERLAY, s.outlineColor);
        ps.popPose();
        super.submit(s, ps, c, cam);
    }
}
