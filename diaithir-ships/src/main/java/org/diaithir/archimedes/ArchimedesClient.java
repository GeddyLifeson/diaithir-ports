package org.diaithir.archimedes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.RenderShape;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Client side: draws a ship's blocks as moving blocks around the entity. */
public final class ArchimedesClient {
    private ArchimedesClient() {}

    public static void init(IEventBus bus) {
        bus.addListener((EntityRenderersEvent.RegisterRenderers e) -> e.registerEntityRenderer(ArchimedesMod.SHIP.get(), ShipRenderer::new));
    }

    public static class ShipRenderState extends EntityRenderState {
        float yaw;
        double cx, cz;
        final List<Piece> pieces = new ArrayList<>();
    }

    record Piece(BlockPos local, MovingBlockRenderState state) {}

    static class ShipRenderer extends EntityRenderer<ShipEntity, ShipRenderState> {
        ShipRenderer(EntityRendererProvider.Context ctx) { super(ctx); }

        @Override public boolean shouldRender(ShipEntity e, Frustum f, double x, double y, double z) { return true; }
        @Override public ShipRenderState createRenderState() { return new ShipRenderState(); }

        @Override
        public void extractRenderState(ShipEntity ship, ShipRenderState s, float partial) {
            super.extractRenderState(ship, s, partial);
            s.yaw = Mth.rotLerp(partial, ship.yRotO, ship.getYRot());
            s.cx = ship.chunk().centerX();
            s.cz = ship.chunk().centerZ();
            s.pieces.clear();
            if (!(ship.level() instanceof ClientLevel level)) return;
            var biome = level.getBiome(ship.blockPosition());
            for (var e : ship.chunk().blocks.entrySet()) {
                if (e.getValue().getRenderShape() != RenderShape.MODEL) continue;
                var m = new MovingBlockRenderState();
                m.blockState = e.getValue();
                m.blockPos = BlockPos.containing(ship.toWorld(e.getKey()));
                m.randomSeedPos = e.getKey();
                m.biome = biome;
                m.cardinalLighting = level.cardinalLighting();
                m.lightEngine = level.getLightEngine();
                s.pieces.add(new Piece(e.getKey(), m));
            }
        }

        @Override
        public void submit(ShipRenderState s, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
            pose.pushPose();
            pose.mulPose(Axis.YP.rotationDegrees(-s.yaw));
            pose.translate(-s.cx, 0, -s.cz);
            for (Piece p : s.pieces) {
                pose.pushPose();
                pose.translate(p.local.getX(), p.local.getY(), p.local.getZ());
                collector.submitMovingBlock(pose, p.state);
                pose.popPose();
            }
            pose.popPose();
        }
    }
}
