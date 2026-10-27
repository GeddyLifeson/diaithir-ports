package com.github.alexthe666.citadel.client.render.legacy;

import com.github.alexthe666.citadel.client.model.basic.BasicEntityModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;

import java.util.ArrayList;
import java.util.List;

/**
 * 1.21 LivingEntityRenderer behaviour (rotation, scale, hurt flash, death roll, layers) on top of the 26.1 submit pipeline,
 * for Citadel AdvancedEntityModel mobs. Subclasses override the same hooks the 1.21 class had.
 */
public abstract class LegacyMobRenderer<T extends LivingEntity, M extends BasicEntityModel<T>> extends EntityRenderer<T, LegacyRenderState> implements LegacyLayerParent<T, M> {
    protected M model;
    protected final List<LegacyLayer<T, M>> layers = new ArrayList<>();
    protected final EntityRendererProvider.Context context;

    protected LegacyMobRenderer(EntityRendererProvider.Context ctx, M model, float shadowRadius) {
        super(ctx);
        this.context = ctx;
        this.model = model;
        this.shadowRadius = shadowRadius;
    }

    public final boolean addLayer(LegacyLayer<T, M> layer) {
        return this.layers.add(layer);
    }

    @Override
    public M getModel() {
        return model;
    }

    public abstract Identifier getTextureLocation(T entity);

    @Override
    public LegacyRenderState createRenderState() {
        return new LegacyRenderState();
    }

    @Override
    public void extractRenderState(T entity, LegacyRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.entity = entity;
        state.partialTick = partialTick;
    }

    protected void scale(T entity, PoseStack poseStack, float partialTick) {
    }

    protected float getFlipDegrees(T entity) {
        return 90.0F;
    }

    protected boolean isShaking(T entity) {
        return entity.isFullyFrozen();
    }

    protected float getBob(T entity, float partialTick) {
        return (float) entity.tickCount + partialTick;
    }

    protected void setupRotations(T entity, PoseStack poseStack, float bob, float yBodyRot, float partialTick, float scale) {
        if (this.isShaking(entity)) {
            yBodyRot += (float) (Math.cos((double) entity.tickCount * 3.25D) * Math.PI * 0.4D);
        }
        if (!entity.hasPose(Pose.SLEEPING)) {
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yBodyRot));
        }
        if (entity.deathTime > 0) {
            float f = ((float) entity.deathTime + partialTick - 1.0F) / 20.0F * 1.6F;
            f = Mth.sqrt(f);
            if (f > 1.0F) f = 1.0F;
            poseStack.mulPose(Axis.ZP.rotationDegrees(f * this.getFlipDegrees(entity)));
        }
    }

    protected RenderType getRenderType(T entity, boolean visible, boolean visibleToPlayer, boolean glowing) {
        Identifier tex = this.getTextureLocation(entity);
        if (visibleToPlayer) return RenderTypes.entityTranslucent(tex);
        if (visible) return RenderTypes.entityCutout(tex);
        return null;
    }

    protected boolean isBodyVisible(T entity) {
        return !entity.isInvisible();
    }

    public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        model.young = entity.isBaby();
        float bodyRot = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
        float headRot = Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot);
        float netHead = headRot - bodyRot;
        if (entity.isPassenger() && entity.getVehicle() instanceof LivingEntity living) {
            bodyRot = Mth.rotLerp(partialTicks, living.yBodyRotO, living.yBodyRot);
            netHead = headRot - bodyRot;
            float d = Mth.wrapDegrees(netHead);
            if (d < -85.0F) d = -85.0F;
            if (d >= 85.0F) d = 85.0F;
            bodyRot = headRot - d;
            if (d * d > 2500.0F) bodyRot += d * 0.2F;
            netHead = headRot - bodyRot;
        }
        float pitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        float bob = this.getBob(entity, partialTicks);
        this.setupRotations(entity, poseStack, bob, bodyRot, partialTicks, entity.getScale());
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        this.scale(entity, poseStack, partialTicks);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        float limbAmount = 0.0F, limbPos = 0.0F;
        if (!entity.isPassenger() && entity.isAlive()) {
            limbAmount = entity.walkAnimation.speed(partialTicks);
            limbPos = entity.walkAnimation.position(partialTicks);
            if (entity.isBaby()) limbPos *= 3.0F;
            if (limbAmount > 1.0F) limbAmount = 1.0F;
        }
        model.prepareMobModel(entity, limbPos, limbAmount, partialTicks);
        model.setupAnim(entity, limbPos, limbAmount, bob, netHead, pitch);
        boolean visible = this.isBodyVisible(entity);
        RenderType type = this.getRenderType(entity, visible, false, false);
        if (type != null) {
            VertexConsumer vc = buffer.getBuffer(type);
            int overlay = OverlayTexture.pack(OverlayTexture.u(0.0F), OverlayTexture.v(entity.hurtTime > 0 || entity.deathTime > 0));
            model.renderToBuffer(poseStack, vc, packedLight, overlay, -1);
        }
        if (!entity.isSpectator()) {
            for (LegacyLayer<T, M> layer : this.layers) {
                layer.render(poseStack, buffer, packedLight, entity, limbPos, limbAmount, partialTicks, bob, netHead, pitch);
            }
        }
        poseStack.popPose();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void submit(LegacyRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        if (state.entity == null) return;
        RecordingBufferSource rec = new RecordingBufferSource();
        T e = (T) state.entity;
        poseStack.pushPose();
        render(e, e.getYRot(), state.partialTick, poseStack, rec, state.lightCoords);
        poseStack.popPose();
        rec.flush(collector);
    }
}
