package org.dimdev.dimdoors.particle.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.model.Model;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.dimdev.dimdoors.client.MonolithRenderState;

import java.util.List;

public class MonolithParticleGroup extends ParticleGroup<MonolithParticle> {
    public MonolithParticleGroup(ParticleEngine engine) {
        super(engine);
    }

    @Override
    public ParticleGroupRenderState extractRenderState(Frustum frustum, Camera camera, float partialTickTime) {
        return new State(this.particles.stream().map(p -> Entry.fromParticle(p, camera, partialTickTime)).toList());
    }

    private record Entry(Model<MonolithRenderState> model, PoseStack poseStack, RenderType renderType) {
        static Entry fromParticle(MonolithParticle particle, Camera camera, float partialTickTime) {
            float delta = particle.progress(partialTickTime);
            PoseStack poseStack = new PoseStack();
            poseStack.pushPose();
            poseStack.mulPose(camera.rotation());
            poseStack.mulPose(Axis.XP.rotationDegrees(150.0F * delta - 60.0F));
            poseStack.scale(-1.0F, -1.0F, 1.0F);
            poseStack.translate(0.0D, -1.101D, 1.5D);
            return new Entry(particle.model, poseStack, RenderTypes.entitySolid(MonolithParticle.TEXTURE));
        }
    }

    private record State(List<Entry> entries) implements ParticleGroupRenderState {
        @Override
        public void submit(SubmitNodeCollector collector, CameraRenderState camera) {
            MonolithRenderState state = new MonolithRenderState();
            for (Entry e : this.entries) {
                collector.submitModel(e.model, state, e.poseStack, e.renderType, 15728880, OverlayTexture.NO_OVERLAY, -1, null, 0, null);
            }
        }
    }
}
