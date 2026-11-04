package net.diaithir.animation.neo.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.diaithir.animation.Controller;
import net.diaithir.animation.Pose;
import net.diaithir.animation.Spec;
import net.diaithir.animation.neo.DiaithirAnimation;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Draws a machine or block with its own model, moved by the pose from its rule. Installed by BlockEntityDispatcherMixin
 * only for block entity types that have no renderer of their own and whose blocks use the normal model.
 */
public final class AnimatedBlockRenderer implements BlockEntityRenderer<BlockEntity, AnimatedBlockRenderer.View> {
    public static final class View extends BlockEntityRenderState {
        @Nullable MovingBlockRenderState block;
        Pose pose = Pose.NONE;
    }

    private static final String[] FLAGS = {"working", "active", "running", "lit", "powered", "open", "on", "enabled", "triggered", "crafting", "burning"};
    private static final class Entry {
        final Controller c; long last;
        Entry(Controller c) { this.c = c; }
    }
    private final Map<Long, Entry> live = new ConcurrentHashMap<>();

    @Override
    public View createRenderState() { return new View(); }

    /** Working flag from a boolean block state property, or null when the block has none. */
    @Nullable
    static Boolean flag(BlockState s) {
        for (Property<?> p : s.getProperties()) {
            if (p instanceof BooleanProperty bp) {
                for (String f : FLAGS) if (f.equals(p.getName())) return s.getValue(bp);
            }
        }
        return null;
    }

    @Override
    public void extractRenderState(BlockEntity be, View state, float partialTicks, Vec3 camera,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, camera, breakProgress);
        state.block = null;
        state.pose = Pose.NONE;
        if (!(be.getLevel() instanceof ClientLevel level)) return;
        BlockState bs = be.getBlockState();
        String id = BuiltInRegistries.BLOCK.getKey(bs.getBlock()).toString();
        long key = be.getBlockPos().asLong();
        Entry e = live.get(key);
        if (e == null) {
            var rule = DiaithirAnimation.rules().match(id);
            if (rule.isEmpty()) return;
            e = new Entry(new Controller(rule.get().spec()));
            live.put(key, e);
        }
        Boolean f = flag(bs);
        net.diaithir.animation.State s = DiaithirAnimation.override(level.dimension().identifier().toString() + "|" + key);
        e.c.setState(s != null ? s : f == null ? net.diaithir.animation.State.IDLE : f ? net.diaithir.animation.State.WORKING : net.diaithir.animation.State.DONE);
        long now = System.nanoTime();
        float dt = e.last == 0 ? 0f : Math.min(0.1f, (now - e.last) / 1.0e9f);
        e.last = now;
        e.c.tick(dt);
        state.pose = e.c.pose();
        MovingBlockRenderState m = new MovingBlockRenderState();
        m.randomSeedPos = be.getBlockPos();
        m.blockPos = be.getBlockPos();
        m.blockState = bs;
        m.biome = level.getBiome(be.getBlockPos());
        m.cardinalLighting = level.cardinalLighting();
        m.lightEngine = level.getLightEngine();
        state.block = m;
    }

    @Override
    public void submit(View state, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.block == null) return;
        Pose p = state.pose;
        ps.pushPose();
        ps.translate(0.5, 0.5, 0.5);
        ps.translate(p.ox(), p.oy(), p.oz());
        if (p.ry() != 0) ps.mulPose(Axis.YP.rotationDegrees(p.ry()));
        if (p.rx() != 0) ps.mulPose(Axis.XP.rotationDegrees(p.rx()));
        if (p.rz() != 0) ps.mulPose(Axis.ZP.rotationDegrees(p.rz()));
        if (p.scale() != 1f) ps.scale(p.scale(), p.scale(), p.scale());
        ps.translate(-0.5, -0.5, -0.5);
        collector.submitMovingBlock(ps, state.block);
        ps.popPose();
    }

    /** Kinds this renderer can show on a whole-block model. SCROLL needs UVs and AIM needs a target, so they stay static. */
    public static boolean supports(Spec s) {
        return switch (s.kind()) {
            case SCROLL, AIM -> false;
            default -> true;
        };
    }
}
