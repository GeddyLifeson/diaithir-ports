package com.mrh0.createaddition.client;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mrh0.createaddition.config.CommonConfig;
import com.mrh0.createaddition.energy.IWireNode;
import com.mrh0.createaddition.energy.WireType;
import com.mrh0.createaddition.util.Util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

/** Draws the catenary wires leaving a wire node (connector, redstone relay). Uses the vanilla leash render type. */
public class WireNodeRenderer<T extends BlockEntity> implements BlockEntityRenderer<T, WireNodeRenderer.State> {
    private static final float HANG = 0.5f;
    private static final int SEGMENTS = 24;

    /** One wire: translate to (tx,ty,tz) from the block origin, then run to (x,y,z). Lights: block/sky at the other end, then at this end. */
    public record Seg(float tx, float ty, float tz, float x, float y, float z, WireType type, float dis, int bOther, int bThis, int sOther, int sThis) {}

    public record Bulb(Seg seg, float x, float y, float z, com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState model) {}
    private static final int[] BULB_COLORS = {0xFF2020, 0x20FF20, 0x2040FF};

    public static class State extends BlockEntityRenderState {
        public final List<Seg> segs = new ArrayList<>();
        public final List<Bulb> bulbs = new ArrayList<>();
    }

    public WireNodeRenderer() {}

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox(T be) {
        return IWireNode.wireBounds(be, null);
    }

    @Override
    public boolean shouldRender(T be, Vec3 cameraPos) {
        double d = getViewDistance();
        return getRenderBoundingBox(be).distanceToSqr(cameraPos) < d * d;
    }

    @Override
    public void extractRenderState(T be, State state, float partialTicks, Vec3 cameraPosition, @Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderState.extractBase(be, state, breakProgress);
        state.segs.clear();
        state.bulbs.clear();
        Level level = be.getLevel();
        if (level == null) return;
        IWireNode te = (IWireNode) be;
        BlockPos here = be.getBlockPos();
        for (int i = 0; i < te.getNodeCount(); i++) {
            if (!te.hasConnection(i)) continue;
            Vec3 d1 = te.getNodeOffset(i);
            IWireNode wn = te.getWireNode(i);
            // The other end may be outside the client's loaded chunks; aim at its block centre then.
            Vec3 d2 = wn != null ? wn.getNodeOffset(te.getOtherNodeIndex(i)) : Vec3.ZERO;
            BlockPos other = te.getNodePos(i);
            if (other == null) continue;
            float tx = other.getX() - here.getX();
            float ty = other.getY() - here.getY();
            float tz = other.getZ() - here.getZ();
            float dis = (float) Math.sqrt(tx * tx + ty * ty + tz * tz);
            state.segs.add(new Seg(tx + .5f + (float) d2.x, ty + .5f + (float) d2.y, tz + .5f + (float) d2.z,
                -tx - (float) d2.x + (float) d1.x, -ty - (float) d2.y + (float) d1.y, -tz - (float) d2.z + (float) d1.z,
                te.getNodeType(i), dis,
                level.getBrightness(LightLayer.BLOCK, other), level.getBrightness(LightLayer.BLOCK, here),
                level.getBrightness(LightLayer.SKY, other), level.getBrightness(LightLayer.SKY, here)));
        }
        // A wire being placed runs from its node to the player.
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            Util.Triple<BlockPos, Integer, WireType> held = Util.getWireNodeOfSpools(player.getMainHandItem(), player.getOffhandItem());
            if (held != null && held.a.equals(here)) {
                Vec3 d1 = te.getNodeOffset(held.b);
                Vec3 pp = player.getPosition(partialTicks);
                float tx = (float) pp.x - here.getX();
                float ty = (float) pp.y - here.getY();
                float tz = (float) pp.z - here.getZ();
                float dis = (float) Math.sqrt(tx * tx + ty * ty + tz * tz);
                BlockPos pb = player.blockPosition();
                state.segs.add(new Seg(tx + .5f, ty + .5f, tz + .5f, -tx + (float) d1.x, -ty + (float) d1.y, -tz + (float) d1.z, held.c, dis,
                    level.getBrightness(LightLayer.BLOCK, pb), level.getBrightness(LightLayer.BLOCK, here),
                    level.getBrightness(LightLayer.SKY, pb), level.getBrightness(LightLayer.SKY, here)));
            }
        }
        addBulbs(state, be.getBlockState());
    }

    private static void addBulbs(State state, BlockState bs) {
        for (Seg s : new ArrayList<>(state.segs)) {
            if (!s.type.isFestive()) continue;
            boolean main = s.x + s.y + s.z > 0;
            for (int idx = 3; idx < SEGMENTS; idx += 3) {
                float part = (float) idx / SEGMENTS;
                float v = s.y;
                float cat = v > 0 ? v * part * part : v - v * (1 - part) * (1 - part);
                int col = BULB_COLORS[main ? 2 - (idx / 3) % 3 : (idx / 3) % 3];
                state.bulbs.add(new Bulb(s, s.x * part, cat + hang(part, s.dis) - 0.03f, s.z * part,
                    com.zurrtum.create.client.catnip.render.CachedBuffers.partial(com.mrh0.createaddition.index.CAPartials.SMALL_LIGHT, bs).color(col).light(0xF000F0).extractRenderState()));
            }
        }
    }

    @Override
    public void submit(State state, PoseStack stack, SubmitNodeCollector queue, CameraRenderState camera) {
        for (Bulb b : state.bulbs) {
            stack.pushPose();
            stack.translate(b.seg.tx, b.seg.ty, b.seg.tz);
            stack.translate(b.x, b.y, b.z);
            b.model.submit(stack, queue);
            stack.popPose();
        }
        for (Seg s : state.segs) {
            stack.pushPose();
            stack.translate(s.tx, s.ty, s.tz);
            queue.submitCustomGeometry(stack, RenderTypes.leash(), (pose, vc) -> {
                Matrix4f m = pose.pose();
                float f = (float) (Mth.invSqrt(s.x * s.x + s.z * s.z) * 0.025F / 2.0F);
                float o1 = s.z * f;
                float o2 = s.x * f;
                wirePart(vc, m, s, 0.025F, 0.025F, o1, o2);
                wirePart(vc, m, s, 0.025F, 0.0F, o1, o2);
            });
            stack.popPose();
        }
    }

    private static float hang(float f, float dis) {
        return (float) Math.sin(-f * (float) Math.PI) * (HANG * dis / (float) CommonConfig.SMALL_CONNECTOR_MAX_LENGTH.get());
    }

    private static void wirePart(VertexConsumer vc, Matrix4f m, Seg s, float a, float b, float o1, float o2) {
        for (int j = 0; j < SEGMENTS; ++j) {
            float f = (float) j / (SEGMENTS - 1);
            int k = (int) Mth.lerp(f, (float) s.bOther, (float) s.bThis);
            int l = (int) Mth.lerp(f, (float) s.sOther, (float) s.sThis);
            int light = LightCoordsUtil.pack(k, l);
            wireVert(vc, m, light, s, a, b, j, false, o1, o2);
            wireVert(vc, m, light, s, a, b, j + 1, true, o1, o2);
        }
    }

    private static void wireVert(VertexConsumer vc, Matrix4f m, int light, Seg s, float a, float b, int index, boolean sw, float o1, float o2) {
        int cr = s.type.getRed(), cg = s.type.getGreen(), cb = s.type.getBlue();
        if (index % 2 == 0) {
            cr = (int) (cr * 0.7F);
            cg = (int) (cg * 0.7F);
            cb = (int) (cb * 0.7F);
        }
        float part = (float) index / (float) SEGMENTS;
        float x = s.x, y = s.y, z = s.z;
        // Component of the wire direction along "up", used for the catenary shape.
        float v = y;
        float catenary = (v > 0.0F ? v * part * part : v - v * (1.0F - part) * (1.0F - part));
        float hangScalar = hang((float) index / SEGMENTS, s.dis);
        float fx = x * part;
        float fy = catenary + hangScalar;
        float fz = z * part;

        if (Math.abs(x) + Math.abs(z) < Math.abs(y)) {
            boolean p = b > 0;
            float c = 0.015f;
            if (!sw) vc.addVertex(m, fx - c, fy, fz + (p ? -c : c)).setColor(cr, cg, cb, 255).setLight(light);
            vc.addVertex(m, fx + c, fy, fz + (p ? c : -c)).setColor(cr, cg, cb, 255).setLight(light);
            if (sw) vc.addVertex(m, fx - c, fy, fz + (p ? -c : c)).setColor(cr, cg, cb, 255).setLight(light);
        } else {
            if (!sw) vc.addVertex(m, fx + o1, fy + a - b, fz - o2).setColor(cr, cg, cb, 255).setLight(light);
            vc.addVertex(m, fx - o1, fy + b, fz + o2).setColor(cr, cg, cb, 255).setLight(light);
            if (sw) vc.addVertex(m, fx + o1, fy + a - b, fz - o2).setColor(cr, cg, cb, 255).setLight(light);
        }
    }
}
