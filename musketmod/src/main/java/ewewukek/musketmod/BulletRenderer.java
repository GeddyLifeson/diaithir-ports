package ewewukek.musketmod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class BulletRenderer extends EntityRenderer<BulletEntity, BulletRenderer.State> {
    private static final RenderType RENDER_TYPE = RenderTypes.entityCutout(MusketMod.resource("textures/entity/bullet.png"));
    private static final RenderType RENDER_TYPE_FIRE = RenderTypes.entityCutout(MusketMod.resource("textures/entity/bullet_fire.png"));

    public static class State extends EntityRenderState {
        boolean firstTick;
        boolean onFire;
        int pelletCount;
    }

    public BulletRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(BulletEntity bullet, State state, float partialTick) {
        super.extractRenderState(bullet, state, partialTick);
        state.firstTick = bullet.isFirstTick();
        state.onFire = bullet.isOnFire();
        state.pelletCount = bullet.pelletCount();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.firstTick) return;

        poseStack.pushPose();

        if (state.pelletCount == 1) {
            poseStack.scale(0.1f, 0.1f, 0.1f);
        } else {
            poseStack.scale(state.onFire ? 0.075f : 0.05f, 0.05f, 0.05f);
        }

        // billboarding
        poseStack.mulPose(camera.orientation);
        poseStack.mulPose(Axis.YP.rotationDegrees(180));

        int light = state.lightCoords;
        collector.submitCustomGeometry(poseStack, state.onFire ? RENDER_TYPE_FIRE : RENDER_TYPE, (pose, builder) -> {
            addVertex(builder, pose, -1, -1, 0, 1, light);
            addVertex(builder, pose,  1, -1, 1, 1, light);
            addVertex(builder, pose,  1,  1, 1, 0, light);
            addVertex(builder, pose, -1,  1, 0, 0, light);
        });

        poseStack.popPose();

        super.submit(state, poseStack, collector, camera);
    }

    private static void addVertex(VertexConsumer builder, PoseStack.Pose pose, float x, float y, float u, float v, int light) {
        builder.addVertex(pose, x, y, 0)
               .setColor(-1)
               .setUv(u, v)
               .setOverlay(OverlayTexture.NO_OVERLAY)
               .setLight(light)
               .setNormal(pose, 0, 0, 1);
    }
}
