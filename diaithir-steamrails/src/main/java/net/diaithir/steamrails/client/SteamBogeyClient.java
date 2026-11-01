package net.diaithir.steamrails.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.trains.bogey.BogeyBlockEntityRenderer;
import com.zurrtum.create.client.content.trains.bogey.BogeyRenderer;
import com.zurrtum.create.client.content.trains.bogey.BogeyVisual;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.diaithir.steamrails.SteamRails;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Drawing of the two steam bogey styles, in both of Create's paths: a render state for the block entity renderer (a bogey standing in the world, and
 * the fallback) and a Flywheel visual (instanced, also used on a moving carriage). Both run through the same geometry here. The part models are
 * drawn around their own centre (8,8,8), so every part is moved to a spot and then shifted by minus half a block.
 *
 * Geometry is in the bogey's frame below Create's usual 1.5078125 drop: wheels turn about an axle at y = {@code axleY}, z = +-1, and there are
 * two wheels per axle, one at each rail.
 */
public final class SteamBogeyClient {
    private SteamBogeyClient() {}

    /** Create lowers every bogey by this much before drawing. */
    public static final double DROP = -1.5078125;
    /** Half the distance between the two wheels of an axle. */
    public static final float RAIL_X = 0.3125F;

    private static PartialModel part(String name) {
        return PartialModel.of(Identifier.fromNamespaceAndPath(SteamRails.ID, "block/bogey/" + name));
    }

    public static final PartialModel DRIVER_WHEEL = part("driver_wheel");
    public static final PartialModel DRIVER_FRAME = part("driver_frame");
    public static final PartialModel DRIVER_ROD = part("driver_rod");
    public static final PartialModel PONY_WHEEL = part("pony_wheel");
    public static final PartialModel PONY_FRAME = part("pony_frame");

    /** One style's numbers: its parts, the axle height and the radius of the crank pin that drives the side rods (0 for none). */
    public record Style(PartialModel wheel, PartialModel frame, @Nullable PartialModel rod, float axleY, float crank) {}

    public static final Style DRIVER = new Style(DRIVER_WHEEL, DRIVER_FRAME, DRIVER_ROD, 1.0F, 0.28F);
    public static final Style PONY = new Style(PONY_WHEEL, PONY_FRAME, null, 0.75F, 0F);

    private static final float[] AXLE_Z = {-1F, 1F};
    private static final float[] RAILS = {-RAIL_X, RAIL_X};

    // ---- block entity renderer path ------------------------------------------------------------------------------------

    public static final class Renderer implements BogeyRenderer {
        private final Style style;

        public Renderer(Style style) {
            this.style = style;
        }

        @Override
        public BogeyBlockEntityRenderer.BogeyRenderState getRenderData(CompoundTag bogeyData, float wheelAngle, float tickProgress, int light,
                @Nullable CardinalLighting lighting, boolean inContraption) {
            BlockState air = Blocks.AIR.defaultBlockState();
            State s = new State(style, wheelAngle);
            s.frame = CachedBuffers.partial(style.frame(), air).cardinalLighting(lighting).light(light).extractRenderState();
            s.wheel = CachedBuffers.partial(style.wheel(), air).cardinalLighting(lighting).light(light).extractRenderState();
            if (style.rod() != null) s.rod = CachedBuffers.partial(style.rod(), air).cardinalLighting(lighting).light(light).extractRenderState();
            return s;
        }
    }

    static final class State implements BogeyBlockEntityRenderer.BogeyRenderState {
        final Style style;
        final float angle;
        SuperByteBufferRenderState frame;
        SuperByteBufferRenderState wheel;
        @Nullable SuperByteBufferRenderState rod;

        State(Style style, float angle) {
            this.style = style;
            this.angle = angle;
        }

        private void put(PoseStack m, SuperByteBufferRenderState buf, SubmitNodeCollector q, float x, float y, float z, float rotX) {
            m.pushPose();
            m.translate(x, y, z);
            if (rotX != 0) m.mulPose(new Quaternionf().rotateX((float) Math.toRadians(rotX)));
            m.translate(-0.5F, -0.5F, -0.5F);
            buf.submit(m, q);
            m.popPose();
        }

        @Override
        public void submit(PoseStack m, SubmitNodeCollector q) {
            m.translate(0.0, DROP, 0.0);
            put(m, frame, q, 0, style.axleY(), 0, 0);
            for (float z : AXLE_Z) for (float x : RAILS) put(m, wheel, q, x, style.axleY(), z, angle);
            if (rod != null) {
                double a = Math.toRadians(angle);
                put(m, rod, q, 0, style.axleY() + (float) (style.crank() * Math.cos(a)), (float) (style.crank() * Math.sin(a)), 0);
            }
        }
    }

    // ---- flywheel path -------------------------------------------------------------------------------------------------

    public static final class Visual implements BogeyVisual {
        private final Style style;
        private final TransformedInstance frame;
        private final TransformedInstance[] wheels = new TransformedInstance[4];
        private final @Nullable TransformedInstance rod;

        public Visual(Style style, VisualizationContext ctx, float partialTick, boolean inContraption) {
            this.style = style;
            this.frame = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.chunkPartial(style.frame())).createInstance();
            var wheelInstancer = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.chunkPartial(style.wheel()));
            for (int i = 0; i < wheels.length; i++) wheels[i] = wheelInstancer.createInstance();
            this.rod = style.rod() == null ? null : ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.chunkPartial(style.rod())).createInstance();
        }

        @Override
        public LongSet createSections(BlockPos pos) {
            return createSections(pos, -1, -1, -1, 1, 0, 1);
        }

        @Override
        public void update(CompoundTag bogeyData, float wheelAngle, PoseStack ps) {
            frame.setTransform(ps).translate(0F, style.axleY(), 0F).translate(-0.5F, -0.5F, -0.5F).setChanged();
            int i = 0;
            for (float z : AXLE_Z) {
                for (float x : RAILS) {
                    wheels[i++].setTransform(ps).translate(x, style.axleY(), z).rotateXDegrees(wheelAngle).translate(-0.5F, -0.5F, -0.5F).setChanged();
                }
            }
            if (rod != null) {
                double a = Math.toRadians(wheelAngle);
                rod.setTransform(ps).translate(0F, style.axleY() + (float) (style.crank() * Math.cos(a)), (float) (style.crank() * Math.sin(a)))
                        .translate(-0.5F, -0.5F, -0.5F).setChanged();
            }
        }

        @Override
        public void hide() {
            frame.setZeroTransform().setChanged();
            for (TransformedInstance w : wheels) w.setZeroTransform().setChanged();
            if (rod != null) rod.setZeroTransform().setChanged();
        }

        @Override
        public void updateLight(int packedLight) {
            frame.light(packedLight).setChanged();
            for (TransformedInstance w : wheels) w.light(packedLight).setChanged();
            if (rod != null) rod.light(packedLight).setChanged();
        }

        @Override
        public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
            consumer.accept(frame);
            for (TransformedInstance w : wheels) consumer.accept(w);
            if (rod != null) consumer.accept(rod);
        }

        @Override
        public void delete() {
            frame.delete();
            for (TransformedInstance w : wheels) w.delete();
            if (rod != null) rod.delete();
        }
    }
}
