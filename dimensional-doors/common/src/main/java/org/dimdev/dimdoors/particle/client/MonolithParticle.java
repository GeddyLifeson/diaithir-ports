package org.dimdev.dimdoors.particle.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import org.dimdev.dimdoors.DimensionalDoors;
import org.dimdev.dimdoors.client.MonolithModel;
import org.dimdev.dimdoors.client.ModEntityModelLayers;
import org.jetbrains.annotations.Nullable;

/**
 * The monolith that flashes in front of the camera when a player is teleported to limbo. Rendered by
 * {@link MonolithParticleGroup}, modelled on vanilla's elder guardian particle.
 */
public class MonolithParticle extends Particle {
    public static final ParticleRenderType RENDER_TYPE = new ParticleRenderType("DIMDOORS_MONOLITH");
    public static final net.minecraft.resources.Identifier TEXTURE = DimensionalDoors.id("textures/mob/monolith/solid/monolith_14.png");

    public final MonolithModel model;

    public MonolithParticle(ClientLevel world, double x, double y, double z) {
        super(world, x, y, z);
        this.model = new MonolithModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModEntityModelLayers.MONOLITH));
        this.gravity = 0.0F;
        this.lifetime = 30;
    }

    public float progress(float partialTick) {
        return (this.age + partialTick) / this.lifetime;
    }

    @Override
    public ParticleRenderType getGroup() {
        return RENDER_TYPE;
    }

    public static class Factory implements ParticleProvider<ParticleOptions> {
        @Nullable
        @Override
        public Particle createParticle(ParticleOptions particleOptions, ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, RandomSource random) {
            return new MonolithParticle(world, x, y, z);
        }
    }
}
