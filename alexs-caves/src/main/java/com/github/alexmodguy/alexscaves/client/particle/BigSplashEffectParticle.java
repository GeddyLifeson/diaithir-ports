package com.github.alexmodguy.alexscaves.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

public class BigSplashEffectParticle extends ACSpriteParticle {

    public BigSplashEffectParticle(ClientLevel level, double x, double y, double z, double xMotion, double yMotion, double zMotion) {
        super(level, x, y, z);
        this.quadSize = 0.1F * (this.random.nextFloat() * 0.5F + 0.5F) * 2.0F;
        this.friction = 0.98F;
        this.gravity = 0.04F;
        this.xd = xMotion;
        this.yd = yMotion;
        this.zd = zMotion;
        this.lifetime = (int) (15.0D / (Math.random() * 0.8D + 0.2D));

    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.lifetime-- <= 0) {
            this.remove();
            return;
        }
        this.yd -= this.gravity;
        this.move(this.xd, this.yd, this.zd);
        this.xd *= this.friction;
        this.yd *= this.friction;
        this.zd *= this.friction;
        if (this.onGround) {
            if (Math.random() < 0.5D) this.remove();
            this.xd *= 0.7F;
            this.zd *= 0.7F;
        }
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Factory(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double x, double y, double z, double xMotion, double yMotion, double zMotion, net.minecraft.util.RandomSource randomSource) {
            BigSplashEffectParticle splashparticle = new BigSplashEffectParticle(clientLevel, x, y, z, xMotion, yMotion, zMotion);
            splashparticle.pickSprite(this.sprite);
            return splashparticle;
        }
    }
}
