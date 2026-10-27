package org.dimdev.dimdoors.particle.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

/**
 * TODO 26.1: the old particle drew the monolith model in front of the camera with an immediate
 * buffer. 26.1 has no immediate mode for particles, so this is currently an invisible placeholder
 * that only keeps the particle type registered.
 */
public class MonolithParticle extends Particle {

    public MonolithParticle(ClientLevel world, double x, double y, double z) {
        super(world, x, y, z);
        this.lifetime = 30;
    }

    @Override
    public ParticleRenderType getGroup() {
        return ParticleRenderType.NO_RENDER;
    }

    public static class Factory implements ParticleProvider<ParticleOptions> {
        @Nullable
        @Override
        public Particle createParticle(ParticleOptions particleOptions, ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, RandomSource random) {
            return new MonolithParticle(world, x, y, z);
        }
    }
}
