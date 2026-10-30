package com.github.ysbbbbbb.kaleidoscopetavern.init;

import com.github.ysbbbbbb.kaleidoscopetavern.KaleidoscopeTavern;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public interface ModParticles {
    DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, KaleidoscopeTavern.MOD_ID);

    Supplier<SimpleParticleType> WATER_TAP_DRIP = PARTICLES.register("water_tap_drip", () -> new SimpleParticleType(true));
    Supplier<SimpleParticleType> LAVA_TAP_DRIP = PARTICLES.register("lava_tap_drip", () -> new SimpleParticleType(true));
}
