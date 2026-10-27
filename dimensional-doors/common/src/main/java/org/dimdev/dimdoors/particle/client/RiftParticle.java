package org.dimdev.dimdoors.particle.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.util.RandomSource;

public class RiftParticle extends SingleQuadParticle {
    private final SpriteSet sprites;

    public RiftParticle(ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, float color, int ageSpread, SpriteSet spriteProvider) {
        super(world, x, y, z, 0, 0, 0, spriteProvider.first());
        this.gravity = 0f;
        this.friction = 0.91F;
        this.sprites = spriteProvider;
        this.xd = velocityX;
        this.yd = velocityY;
        this.zd = velocityZ;

        this.quadSize *= 0.55f;
        this.lifetime = ageSpread - ageSpread / 2 + this.random.nextInt(ageSpread);

        this.setColor(color, color, color);
        this.setSpriteFromAge(spriteProvider);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.removed) {
            this.setSpriteFromAge(this.sprites);
            this.setAlpha(1 - (float) age / lifetime);
        }
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    @Override
    protected int getLightCoords(float partialTick) {
        return 15728880;
    }

    public static class Factory implements ParticleProvider<RiftParticleOptions> {
        private final SpriteSet spriteProvider;

        public Factory(SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(RiftParticleOptions riftParticleOptions, ClientLevel clientWorld, double x, double y, double z, double velocityX, double velocityY, double velocityZ, RandomSource random) {
            return new RiftParticle(clientWorld, x, y, z, velocityX, velocityY, velocityZ, riftParticleOptions.color(), riftParticleOptions.averageAge(), this.spriteProvider);
        }
    }
}
