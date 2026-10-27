package com.github.alexmodguy.alexscaves.client.render;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/** 26.1 stand-ins for the upstream custom render types (custom shaders are not ported): vanilla glow / translucent types. */
public class ACRenderTypes {

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
        return RenderTypes.entityTranslucent(locationIn);
    }

    public static RenderType getRadiationGlow(Identifier locationIn) {
        return RenderTypes.eyes(locationIn);
    }

    public static RenderType getBlueRadiationGlow(Identifier locationIn) {
        return RenderTypes.eyes(locationIn);
    }

    public static RenderType getGelTriangles(Identifier locationIn) {
        return RenderTypes.entityTranslucent(locationIn);
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
        return RenderTypes.entityTranslucent(locationIn);
    }

    public static RenderType getCaveMapBackground(Identifier locationIn, boolean showBackground) {
        return RenderTypes.entityTranslucent(locationIn);
    }

    public static RenderType getBookWidget(Identifier locationIn, boolean sepia) {
        return RenderTypes.entityTranslucent(locationIn);
    }

    public static RenderType getBubbledCull(Identifier locationIn) {
        return RenderTypes.entityTranslucent(locationIn);
    }

    public static RenderType getBubbledNoCull(Identifier locationIn) {
        return RenderTypes.entityTranslucent(locationIn);
    }

    public static RenderType getRaygunRay(Identifier locationIn, boolean irradiated) {
        return RenderTypes.eyes(locationIn);
    }

    public static RenderType getTremorzillaBeam(Identifier locationIn, boolean irradiated) {
        return RenderTypes.eyes(locationIn);
    }

    public static RenderType getPurpleWitch(Identifier locationIn) {
        return RenderTypes.entityTranslucent(locationIn);
    }

    public static RenderType getWatcherAppearance(Identifier locationIn) {
        return RenderTypes.entityTranslucent(locationIn);
    }

    public static RenderType entityTranslucent(Identifier tex) {
        return RenderTypes.entityTranslucent(tex);
    }
}
