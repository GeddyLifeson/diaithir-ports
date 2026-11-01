package com.github.ysbbbbbb.kaleidoscopetavern.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.DripParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class TapDripParticle extends DripParticle {
    private final ParticleOptions fallingParticle;

    public TapDripParticle(ClientLevel pLevel, double pX, double pY, double pZ, Fluid pType, ParticleOptions fallingParticle, TextureAtlasSprite sprite) {
        super(pLevel, pX, pY, pZ, pType, sprite);
        this.fallingParticle = fallingParticle;
        this.gravity *= 0.02F;
        this.lifetime = 18;
    }

    public static class WaterTapDripParticle implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public WaterTapDripParticle(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Override
        public Particle createParticle(
                SimpleParticleType options, ClientLevel level,
                double x, double y, double z,
                double xAux, double yAux, double zAux,
                RandomSource random
        ) {
            DripParticle dripparticle = new TapDripParticle(level, x, y, z, Fluids.WATER, ParticleTypes.FALLING_DRIPSTONE_WATER, this.sprite.get(random));
            dripparticle.setColor(0.2F, 0.3F, 1.0F);
            return dripparticle;
        }
    }

    public static class LavaTapDripParticle implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public LavaTapDripParticle(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Override
        public Particle createParticle(
                SimpleParticleType options, ClientLevel level,
                double x, double y, double z,
                double xAux, double yAux, double zAux,
                RandomSource random
        ) {
            return new TapDripParticle(level, x, y, z, Fluids.LAVA, ParticleTypes.FALLING_DRIPSTONE_LAVA, this.sprite.get(random));
        }
    }

    @Override
    protected void preMoveUpdate() {
        super.preMoveUpdate();
        this.level.addParticle(this.fallingParticle, this.x, this.y, this.z, this.xd, this.yd, this.zd);
    }

    @Override
    protected void postMoveUpdate() {
        this.xd *= 0.02;
        this.yd *= 0.02;
        this.zd *= 0.02;
    }
}
