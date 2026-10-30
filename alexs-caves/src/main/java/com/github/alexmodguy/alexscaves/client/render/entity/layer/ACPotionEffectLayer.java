package com.github.alexmodguy.alexscaves.client.render.entity.layer;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.render.ACRenderTypes;
import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import com.github.alexmodguy.alexscaves.server.potion.DarknessIncarnateEffect;
import com.github.alexmodguy.alexscaves.server.potion.IrradiatedEffect;
import com.github.alexthe666.citadel.client.render.legacy.RecordingBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;

/** Radiation glow, bubble, darkness incarnate and sugar rush overlays (ported from upstream ACPotionEffectLayer). */
public class ACPotionEffectLayer<S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends RenderLayer<S, M> {
    private static final Identifier TEXTURE_BUBBLE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/deep_one/bubble.png");
    private static final Identifier TEXTURE_WATER = Identifier.withDefaultNamespace("textures/block/water_still.png");
    public static final Identifier INSIDE_BUBBLE_TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/misc/inside_bubble.png");
    public static final Identifier TEXTURE_DARKNESS = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/darkness_incarnate.png");
    public static final Identifier TEXTURE_SUGAR_RUSH = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/sugar_rush.png");

    public static final ContextKey<Data> KEY = new ContextKey<>(Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "potion_effects"));

    public static class Data {
        public int irradiated;
        public boolean bubbled;
        public float darkness;
        public boolean sugarRush;
        public float bodyYaw;
        public float width, height;
        public int waterColor = 0x3F76E4;
        public float ageInTicks;
        public com.github.alexmodguy.alexscaves.client.render.misc.RaygunBeam.Ray ray;
        public net.minecraft.world.phys.Vec3[] trail;
        public net.minecraft.world.phys.Vec3 entityPos = net.minecraft.world.phys.Vec3.ZERO;
        public net.minecraft.world.phys.Vec3 trailHeight = net.minecraft.world.phys.Vec3.ZERO;
    }

    private static final Identifier TRAIL_TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/particle/teletor_trail.png");

    private final RenderLayerParent<S, M> parent;

    public ACPotionEffectLayer(RenderLayerParent<S, M> parent) {
        super(parent);
        this.parent = parent;
    }

    /** Fills the per-entity data used by the layer. Registered through RegisterRenderStateModifiersEvent. */
    public static void extract(LivingEntity living, LivingEntityRenderState state) {
        com.github.alexmodguy.alexscaves.client.render.misc.ACArmPoses.extract(living, state);
        Data d = null;
        boolean irradiated = living.hasEffect(ACEffectRegistry.IRRADIATED) && AlexsCaves.CLIENT_CONFIG.radiationGlowEffect.get();
        boolean bubbled = (living.hasEffect(ACEffectRegistry.BUBBLED) || AlexsCaves.PROXY.hasBubbledEffectVisual(living.getId())) && living.isAlive();
        boolean dark = living.hasEffect(ACEffectRegistry.DARKNESS_INCARNATE) && AlexsCaves.CLIENT_CONFIG.radiationGlowEffect.get() && living.isAlive();
        boolean sugar = living.hasEffect(ACEffectRegistry.SUGAR_RUSH);
        float pt0 = state.partialTick;
        var ray = com.github.alexmodguy.alexscaves.client.render.misc.RaygunBeam.extract(living, pt0, false);
        if (irradiated || bubbled || dark || sugar || ray != null) {
            d = new Data();
            float pt = state.partialTick;
            d.ray = ray;
            if (dark) {
                d.trail = new net.minecraft.world.phys.Vec3[60];
                for (int i = 0; i < 60; i++) d.trail[i] = AlexsCaves.PROXY.getDarknessTrailPosFor(living, i + 5, pt);
                d.entityPos = living.getPosition(pt);
                d.trailHeight = new net.minecraft.world.phys.Vec3(0, living.getBbHeight() * 0.8F, 0);
            }
            if (irradiated) d.irradiated = living.getEffect(ACEffectRegistry.IRRADIATED).getAmplifier() + 1;
            d.bubbled = bubbled;
            if (dark) d.darkness = DarknessIncarnateEffect.getIntensity(living, pt, 25F);
            d.sugarRush = sugar;
            d.bodyYaw = net.minecraft.util.Mth.rotLerp(pt, living.yBodyRotO, living.yBodyRot);
            d.width = living.getBbWidth();
            d.height = living.getBbHeight();
            d.ageInTicks = living.tickCount + pt;
            if (bubbled) d.waterColor = living.level().getBiome(living.blockPosition()).value().getWaterColor();
        }
        state.setRenderData(KEY, d);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, S state, float yRot, float xRot) {
        Data d = state.getRenderData(KEY);
        if (d == null) return;
        Identifier texture = parent instanceof LivingEntityRenderer r ? r.getTextureLocation(state) : null;
        int overlay = LivingEntityRenderer.getOverlayCoords(state, 0);
        M model = getParentModel();
        if (d.irradiated > 0 && texture != null) {
            int level = d.irradiated;
            float alpha = level >= IrradiatedEffect.BLUE_LEVEL ? 0.9F : Math.min(level * 0.33F, 1F);
            collector.order(1).submitModel(model, state, poseStack, level >= IrradiatedEffect.BLUE_LEVEL ? ACRenderTypes.getBlueRadiationGlow(texture) : ACRenderTypes.getRadiationGlow(texture), light, overlay, ARGB.colorFromFloat(alpha, 1F, 1F, 1F), null, state.outlineColor, null);
        }
        if (d.bubbled) {
            float size = (float) Math.ceil(Math.max(d.height, d.width));
            poseStack.pushPose();
            poseStack.translate(0, 1.4 - size * 0.5F, 0);
            poseStack.mulPose(Axis.YP.rotationDegrees(180 - d.bodyYaw));
            poseStack.scale(1.1F, 1.1F, 1.1F);
            float waterAnimOffset = (float) (Math.round(d.ageInTicks * 0.4)) % 16.0F;
            RecordingBufferSource rec = new RecordingBufferSource();
            renderBubble(poseStack, rec.getBuffer(ACRenderTypes.getBubbledCull(TEXTURE_WATER)), size - 0.1F, light, size * 0.5F, size * 0.5F * 0.0625F, -0.0625F * waterAnimOffset, true, d.waterColor);
            renderBubble(poseStack, rec.getBuffer(ACRenderTypes.getBubbledNoCull(TEXTURE_BUBBLE)), size, light, 1, 1, 0, false, d.waterColor);
            poseStack.popPose();
            rec.flush(collector);
        }
        if (d.darkness > 0 && texture != null) {
            collector.order(2).submitModel(model, state, poseStack, RenderTypes.entityTranslucent(texture), 0, overlay, ARGB.colorFromFloat(d.darkness, 0F, 0F, 0F), null, state.outlineColor, null);
        }
        if (d.ray != null) {
            net.minecraft.world.phys.Vec3 cam = net.minecraft.client.Minecraft.getInstance().gameRenderer.getMainCamera().position();
            com.github.alexmodguy.alexscaves.client.render.misc.RaygunBeam.submit(collector.order(3), poseStack, d.ray.from().subtract(cam), d.ray.to().subtract(cam), d.ray);
        }
        if (d.trail != null && d.darkness > 0) {
            submitTrail(collector, poseStack, d, state, light);
        }
        if (d.sugarRush && model instanceof HumanoidModel<?>) {
            collector.order(1).submitModel(model, state, poseStack, RenderTypes.entityCutout(TEXTURE_SUGAR_RUSH), light, overlay, -1, null, state.outlineColor, null);
        }
    }

    private void submitTrail(SubmitNodeCollector collector, PoseStack poseStack, Data d, S state, int light) {
        net.minecraft.world.phys.Vec3 cam = net.minecraft.client.Minecraft.getInstance().gameRenderer.getMainCamera().position();
        Matrix4f inv = new Matrix4f(poseStack.last().pose()).invert();
        net.minecraft.world.phys.Vec3 offset = new net.minecraft.world.phys.Vec3(0, d.height * 0.5F, 0);
        float h = (float) d.trailHeight.y;
        int color = ARGB.colorFromFloat(Math.min(1F, d.darkness), 0F, 0F, 0F);
        collector.order(2).submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(TRAIL_TEXTURE), (pose, vc) -> {
            net.minecraft.world.phys.Vec3 prev = d.entityPos.add(offset).subtract(cam);
            int n = d.trail.length;
            for (int i = 0; i < n; i++) {
                net.minecraft.world.phys.Vec3 cur = d.trail[i].add(offset).subtract(cam);
                float u1 = i / (float) n, u2 = u1 + 1F / n;
                float[][] pts = {{(float) prev.x, (float) prev.y - h, (float) prev.z, u1, 1F}, {(float) cur.x, (float) cur.y - h, (float) cur.z, u2, 1F},
                        {(float) cur.x, (float) cur.y + h, (float) cur.z, u2, 0F}, {(float) prev.x, (float) prev.y + h, (float) prev.z, u1, 0F}};
                for (int pass = 0; pass < 2; pass++) {
                    for (int k = 0; k < 4; k++) {
                        float[] q = pts[pass == 0 ? k : 3 - k];
                        org.joml.Vector3f p = inv.transformPosition(new org.joml.Vector3f(q[0], q[1], q[2]));
                        vc.addVertex(pose, p.x, p.y, p.z).setColor(color).setUv(q[3], q[4]).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0F, 1F, 0F);
                    }
                }
                prev = cur;
            }
        });
    }

    private static void renderBubble(PoseStack poseStack, VertexConsumer consumer, float size, int packedLight, float textureScaleXZ, float textureScaleY, float uvOffset, boolean water, int waterColor) {
        Matrix4f m = poseStack.last().pose();
        float s = size * -0.5F;
        float e = size * 0.5F;
        face(m, consumer, packedLight, s, e, s, e, e, e, e, e, textureScaleXZ, textureScaleY, uvOffset, water, waterColor);
        face(m, consumer, packedLight, s, e, e, s, s, s, s, s, textureScaleXZ, textureScaleY, uvOffset, water, waterColor);
        face(m, consumer, packedLight, e, e, e, s, s, e, e, s, textureScaleXZ, textureScaleY, uvOffset, water, waterColor);
        face(m, consumer, packedLight, s, s, s, e, s, e, e, s, textureScaleXZ, textureScaleY, uvOffset, water, waterColor);
        face(m, consumer, packedLight, s, e, s, s, s, s, e, e, textureScaleXZ, textureScaleY, uvOffset, water, waterColor);
        face(m, consumer, packedLight, s, e, e, e, e, e, s, s, textureScaleXZ, textureScaleY, uvOffset, water, waterColor);
    }

    private static void face(Matrix4f m, VertexConsumer vc, int light, float f1, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float sxz, float sy, float uv, boolean water, int waterColor) {
        int r = 255, g = 255, b = 255, a = water ? 200 : 255;
        if (water) {
            r = waterColor >> 16 & 255;
            g = waterColor >> 8 & 255;
            b = waterColor & 255;
        }
        int ov = OverlayTexture.NO_OVERLAY;
        vc.addVertex(m, f1, f3, f5).setColor(r, g, b, a).setUv(0, sy + uv).setOverlay(ov).setLight(light).setNormal(0F, -1F, 0F);
        vc.addVertex(m, f2, f3, f6).setColor(r, g, b, a).setUv(sxz, sy + uv).setOverlay(ov).setLight(light).setNormal(0F, -1F, 0F);
        vc.addVertex(m, f2, f4, f7).setColor(r, g, b, a).setUv(sxz, uv).setOverlay(ov).setLight(light).setNormal(0F, -1F, 0F);
        vc.addVertex(m, f1, f4, f8).setColor(r, g, b, a).setUv(0, uv).setOverlay(ov).setLight(light).setNormal(0F, -1F, 0F);
    }
}
