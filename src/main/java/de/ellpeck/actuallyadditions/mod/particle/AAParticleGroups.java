package de.ellpeck.actuallyadditions.mod.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import de.ellpeck.actuallyadditions.mod.ActuallyAdditions;
import de.ellpeck.actuallyadditions.mod.blocks.render.RenderTypes;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.neoforged.neoforge.client.event.RegisterParticleGroupsEvent;

import java.util.List;

/** 26.1 draws particles by group; Actually Additions' laser beams and laser-carried items each get one. */
public final class AAParticleGroups {
    public static final ParticleRenderType BEAMS = new ParticleRenderType(ActuallyAdditions.MODID + ":beams");
    public static final ParticleRenderType ITEMS = new ParticleRenderType(ActuallyAdditions.MODID + ":laser_items");

    private AAParticleGroups() {}

    public static void register(RegisterParticleGroupsEvent event) {
        event.register(BEAMS, Beams::new);
        event.register(ITEMS, Items::new);
    }

    private static class Beams extends ParticleGroup<ParticleBeam> {
        Beams(ParticleEngine engine) {
            super(engine);
        }

        @Override
        public ParticleGroupRenderState extractRenderState(Frustum frustum, Camera camera, float partialTickTime) {
            List<ParticleBeam.Beam> beams = this.particles.stream().map(beam -> beam.extract(camera.position())).toList();
            return (collector, cameraState) -> collector.submitCustomGeometry(new PoseStack(), RenderTypes.LASER,
                (pose, buffer) -> beams.forEach(beam -> beam.draw(buffer)));
        }
    }

    private static class Items extends ParticleGroup<ParticleLaserItem> {
        Items(ParticleEngine engine) {
            super(engine);
        }

        @Override
        public ParticleGroupRenderState extractRenderState(Frustum frustum, Camera camera, float partialTickTime) {
            List<ParticleLaserItem.Floating> items = this.particles.stream().map(item -> item.extract(camera.position())).toList();
            return (collector, cameraState) -> items.forEach(item -> item.state().submit(item.pose(), collector, item.light(), OverlayTexture.NO_OVERLAY, 0));
        }
    }
}
