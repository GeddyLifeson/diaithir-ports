package com.github.alexmodguy.alexscaves.client.render.misc;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.render.ACRenderTypes;
import com.github.alexmodguy.alexscaves.server.enchantment.ACEnchantmentRegistry;
import com.github.alexmodguy.alexscaves.server.item.RaygunItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** 26.1 port of RaygunRenderHelper: two crossed ribbons from the gun to the ray hit position, submitted as custom geometry. */
public class RaygunBeam {
    private static final Identifier RAY = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/raygun/raygun_ray.png");
    private static final Identifier BLUE_RAY = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/raygun/raygun_blue_ray.png");

    /** World-space description of one beam, extracted on the render-state thread. */
    public record Ray(Vec3 from, Vec3 to, boolean blue, float age) {}

    /** Beam for the raygun held in either hand of a living entity, or null when not firing. */
    public static Ray extract(LivingEntity entity, float partialTick, boolean firstPerson) {
        if (!entity.isUsingItem()) return null;
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = entity.getItemInHand(hand);
            if (!(stack.getItem() instanceof RaygunItem) || RaygunItem.getUseTime(stack) < 5) continue;
            Vec3 to = RaygunItem.getLerpedRayPosition(stack, partialTick);
            if (to == null) continue;
            boolean left = (hand == InteractionHand.MAIN_HAND) == (entity.getMainArm() == HumanoidArm.LEFT);
            boolean blue = false;
            var level = Minecraft.getInstance().level;
            if (level != null) {
                var holder = ACEnchantmentRegistry.getEnchantmentHolder(level, ACEnchantmentRegistry.GAMMA_RAY);
                blue = holder.isPresent() && stack.getEnchantmentLevel(holder.get()) > 0;
            }
            Vec3 from = entity.getPosition(partialTick).add(gunOffset(entity, partialTick, left));
            return new Ray(from, to, blue, entity.tickCount + partialTick);
        }
        return null;
    }

    public static Ray extractFirstPersonHand(LivingEntity entity, InteractionHand hand, float partialTick) {
        ItemStack stack = entity.getItemInHand(hand);
        if (!entity.isUsingItem() || !(stack.getItem() instanceof RaygunItem) || RaygunItem.getUseTime(stack) < 5) return null;
        Vec3 to = RaygunItem.getLerpedRayPosition(stack, partialTick);
        if (to == null) return null;
        boolean blue = false;
        var level = Minecraft.getInstance().level;
        if (level != null) {
            var holder = ACEnchantmentRegistry.getEnchantmentHolder(level, ACEnchantmentRegistry.GAMMA_RAY);
            blue = holder.isPresent() && stack.getEnchantmentLevel(holder.get()) > 0;
        }
        return new Ray(Vec3.ZERO, to, blue, entity.tickCount + partialTick);
    }

    private static Vec3 gunOffset(LivingEntity entity, float partialTicks, boolean left) {
        int i = left ? -1 : 1;
        float yBodyRot = Mth.lerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
        Vec3 offset = new Vec3(entity.getBbWidth() * -0.5F * i, entity.getBbHeight() * 0.8F, 0).yRot((float) Math.toRadians(-yBodyRot));
        return offset.add(entity.getViewVector(partialTicks).normalize().scale(1.5F));
    }

    /**
     * Submits the beam as custom geometry. camFrom/camTo are camera-relative world points. The pose stack's own matrix is inverted
     * so the geometry lands where the points say (pose stacks in entity layers map local space to camera-relative space).
     */
    public static void submit(OrderedSubmitNodeCollector collector, PoseStack poseStack, Vec3 camFrom, Vec3 camTo, Ray ray) {
        Matrix4f inv = new Matrix4f(poseStack.last().pose()).invert();
        collector.submitCustomGeometry(poseStack, ACRenderTypes.getRaygunRay(ray.blue() ? BLUE_RAY : RAY, false), (pose, vc) -> emit(vc, pose, inv, camFrom, camTo, ray));
    }

    /** Draws the beam straight into a buffer source with a pose stack that already carries the camera rotation (level render stage). */
    public static void renderImmediate(PoseStack poseStack, net.minecraft.client.renderer.MultiBufferSource.BufferSource source, Vec3 camFrom, Vec3 camTo, Ray ray) {
        var type = ACRenderTypes.getRaygunRay(ray.blue() ? BLUE_RAY : RAY, false);
        emit(source.getBuffer(type), poseStack.last(), new Matrix4f(), camFrom, camTo, ray);
        source.endBatch(type);
    }

    private static void emit(VertexConsumer vc, PoseStack.Pose pose, Matrix4f inv, Vec3 camFrom, Vec3 camTo, Ray ray) {
        Vec3 axisVec = camTo.subtract(camFrom);
        float length = (float) axisVec.length();
        if (length < 0.05F) return;
        Vector3f a = new Vector3f((float) axisVec.x / length, (float) axisVec.y / length, (float) axisVec.z / length);
        Vector3f ref = Math.abs(a.y) > 0.95F ? new Vector3f(1, 0, 0) : new Vector3f(0, 1, 0);
        Vector3f u = new Vector3f(a).cross(ref).normalize();
        Vector3f v = new Vector3f(a).cross(u).normalize();
        float spin = ray.age() * 3.0F * Mth.DEG_TO_RAD;
        Vector3f u2 = new Vector3f(u).mul(Mth.cos(spin)).add(new Vector3f(v).mul(Mth.sin(spin)));
        Vector3f v2 = new Vector3f(v).mul(Mth.cos(spin)).sub(new Vector3f(u).mul(Mth.sin(spin)));
        float scroll = -1.0F * (ray.age() * 0.25F % 1.0F);
        float v0 = -1.0F + scroll;
        float v1 = length + v0;
        float w = 1.3F;
        Vector3f start = new Vector3f((float) camFrom.x, (float) camFrom.y, (float) camFrom.z);
        Vector3f end = new Vector3f(start).fma(length, a);
        Vector3f[] tri = new Vector3f[]{
                start, new Vector3f(end).fma(-w, u2), new Vector3f(end).fma(w, u2),
                start, new Vector3f(end).fma(w, v2), new Vector3f(end).fma(-w, v2)};
        float[][] uv = {{0.5F, v0}, {0F, v1}, {1F, v1}, {0.5F, v0}, {1F, v1}, {0F, v1}};
        for (int t = 0; t < 2; t++) {
            for (int pass = 0; pass < 2; pass++) {
                for (int k = 0; k < 4; k++) {
                    int kk = pass == 0 ? k : 3 - k;
                    int idx = t * 3 + Math.min(kk, 2);
                    Vector3f p = inv.transformPosition(new Vector3f(tri[idx]));
                    vertex(vc, pose, p, uv[idx][0], uv[idx][1]);
                }
            }
        }
    }

    private static void vertex(VertexConsumer vc, PoseStack.Pose pose, Vector3f p, float u, float v) {
        vc.addVertex(pose, p.x, p.y, p.z).setColor(255, 255, 255, 255).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
