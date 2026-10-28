package com.github.alexmodguy.alexscaves.client.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.function.Function;

/**
 * 26.1 render types. Types with a custom core shader upstream (irradiated, bubbled, gel, red ghost, purple witch)
 * use the pipelines in {@link ACRenderPipelines}; the rest are vanilla glow / translucent stand-ins.
 */
public class ACRenderTypes {

    private static Function<Identifier, RenderType> entityType(String name, RenderPipeline pipeline, boolean lit) {
        return entityType(name, pipeline, lit, null);
    }

    private static Function<Identifier, RenderType> entityType(String name, RenderPipeline pipeline, boolean lit, net.minecraft.client.renderer.rendertype.OutputTarget output) {
        return Util.memoize(texture -> {
            RenderSetup.RenderSetupBuilder b = RenderSetup.builder(pipeline).withTexture("Sampler0", texture);
            if (lit) {
                b.useLightmap().useOverlay();
            }
            if (output != null) {
                b.setOutputTarget(output);
            }
            return RenderType.create(name, b.sortOnUpload().createRenderSetup());
        });
    }

    private static final Function<Identifier, RenderType> IRRADIATED = entityType("alexscaves_irradiated", ACRenderPipelines.IRRADIATED, false, GlowBloom.IRRADIATED.output);
    private static final Function<Identifier, RenderType> BLUE_IRRADIATED = entityType("alexscaves_blue_irradiated", ACRenderPipelines.BLUE_IRRADIATED, false, GlowBloom.IRRADIATED.output);
    private static final Function<Identifier, RenderType> PURPLE_WITCH = entityType("alexscaves_purple_witch", ACRenderPipelines.PURPLE_WITCH, false, GlowBloom.WITCH.output);
    private static final Function<Identifier, RenderType> RED_GHOST = entityType("alexscaves_red_ghost", ACRenderPipelines.RED_GHOST, true);
    private static final Function<Identifier, RenderType> BUBBLED = entityType("alexscaves_bubbled", ACRenderPipelines.BUBBLED, true);
    private static final Function<Identifier, RenderType> BUBBLED_NO_CULL = entityType("alexscaves_bubbled_no_cull", ACRenderPipelines.BUBBLED_NO_CULL, true);
    private static final Function<Identifier, RenderType> GEL = entityType("alexscaves_gel", ACRenderPipelines.GEL, true);
    private static final Function<Identifier, RenderType> GEL_TRIANGLES = entityType("alexscaves_gel_triangles", ACRenderPipelines.GEL_TRIANGLES, true);

    public static RenderType itemEntityTranslucentCull(Identifier resourceLocation) {
        return RenderTypes.entityTranslucent(resourceLocation);
    }

    public static RenderType getParticleTrail(Identifier resourceLocation) {
        return RenderTypes.eyes(resourceLocation);
    }

    public static RenderType getVoidBeingCloud(Identifier resourceLocation) {
        return RenderTypes.eyes(resourceLocation);
    }

    public static RenderType getEyesAlphaEnabled(Identifier locationIn) {
        return RenderTypes.eyes(locationIn);
    }

    public static RenderType getAmbersolShine() {
        return RenderTypes.lightning();
    }

    public static RenderType getNucleeperLights() {
        return RenderTypes.lightning();
    }

    public static RenderType getHologramLights() {
        return RenderTypes.lightning();
    }

    public static RenderType getCrucibleItemBeam() {
        return RenderTypes.lightning();
    }

    public static RenderType getSubmarineLights() {
        return RenderTypes.lightning();
    }

    public static RenderType getGel(Identifier locationIn) {
        return GEL.apply(locationIn);
    }

    public static RenderType getRadiationGlow(Identifier locationIn) {
        GlowBloom.IRRADIATED.request();
        return IRRADIATED.apply(locationIn);
    }

    public static RenderType getBlueRadiationGlow(Identifier locationIn) {
        GlowBloom.IRRADIATED.request();
        return BLUE_IRRADIATED.apply(locationIn);
    }

    public static RenderType getGelTriangles(Identifier locationIn) {
        return GEL_TRIANGLES.apply(locationIn);
    }

    public static RenderType getSubmarineMask() {
        return RenderTypes.waterMask();
    }

    public static RenderType getGhostly(Identifier texture) {
        return RenderTypes.entityTranslucent(texture);
    }

    public static RenderType getTeslaBulb(Identifier resourceLocation) {
        return RenderTypes.eyes(resourceLocation);
    }

    public static RenderType getRainbow(Identifier resourceLocation) {
        return RenderTypes.eyes(resourceLocation);
    }

    public static RenderType getHologram(Identifier locationIn) {
        return RenderTypes.entityTranslucent(locationIn);
    }

    public static RenderType getRedGhost(Identifier locationIn) {
        return RED_GHOST.apply(locationIn);
    }

    public static RenderType getCaveMapBackground(Identifier locationIn, boolean showBackground) {
        return RenderTypes.entityTranslucent(locationIn);
    }

    public static RenderType getBookWidget(Identifier locationIn, boolean sepia) {
        return RenderTypes.entityTranslucent(locationIn);
    }

    public static RenderType getBubbledCull(Identifier locationIn) {
        return BUBBLED.apply(locationIn);
    }

    public static RenderType getBubbledNoCull(Identifier locationIn) {
        return BUBBLED_NO_CULL.apply(locationIn);
    }

    public static RenderType getRaygunRay(Identifier locationIn, boolean irradiated) {
        return RenderTypes.eyes(locationIn);
    }

    public static RenderType getTremorzillaBeam(Identifier locationIn, boolean irradiated) {
        return RenderTypes.eyes(locationIn);
    }

    public static RenderType getPurpleWitch(Identifier locationIn) {
        GlowBloom.WITCH.request();
        return PURPLE_WITCH.apply(locationIn);
    }

    public static RenderType getWatcherAppearance(Identifier locationIn) {
        return RenderTypes.entityTranslucent(locationIn);
    }

    public static RenderType entityTranslucent(Identifier tex) {
        return RenderTypes.entityTranslucent(tex);
    }
}
