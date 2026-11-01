package com.github.alexmodguy.alexscaves.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;

/**
 * 1.21 TextureSheetParticle look-alike on top of 26.1's SingleQuadParticle: constructors without a sprite,
 * pickSprite/setSpriteFromAge, and a settable layer. Ported particle classes extend this unchanged.
 */
public abstract class ACSpriteParticle extends SingleQuadParticle {
    protected SingleQuadParticle.Layer layer = SingleQuadParticle.Layer.TRANSLUCENT;

    protected ACSpriteParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z, placeholder());
    }

    protected ACSpriteParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
        super(level, x, y, z, xd, yd, zd, placeholder());
    }

    private static TextureAtlasSprite placeholder() {
        return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.PARTICLES).missingSprite();
    }

    public void pickSprite(SpriteSet sprites) {
        this.setSprite(sprites.get(this.random));
    }

    @Override
    protected SingleQuadParticle.Layer getLayer() {
        return layer;
    }

    /** 1.21 name of getLightCoords. */
    protected int getLightColor(float partialTick) {
        return super.getLightCoords(partialTick);
    }

    @Override
    protected int getLightCoords(float partialTick) {
        return getLightColor(partialTick);
    }
}
