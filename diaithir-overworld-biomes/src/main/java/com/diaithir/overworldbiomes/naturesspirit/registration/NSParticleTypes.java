package com.diaithir.overworldbiomes.naturesspirit.registration;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NSParticleTypes {

  public static final String MOD_ID = "natures_spirit";

  public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, MOD_ID);

  public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RED_MAPLE_LEAVES_PARTICLE_HOLDER =
      PARTICLE_TYPES.register("red_maple_leaves", () -> new SimpleParticleType(false));
  public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ORANGE_MAPLE_LEAVES_PARTICLE_HOLDER =
      PARTICLE_TYPES.register("orange_maple_leaves", () -> new SimpleParticleType(false));
  public static final DeferredHolder<ParticleType<?>, SimpleParticleType> YELLOW_MAPLE_LEAVES_PARTICLE_HOLDER =
      PARTICLE_TYPES.register("yellow_maple_leaves", () -> new SimpleParticleType(false));
  public static final DeferredHolder<ParticleType<?>, SimpleParticleType> MILK_PARTICLE_HOLDER =
      PARTICLE_TYPES.register("milk", () -> new SimpleParticleType(false));
  public static final DeferredHolder<ParticleType<?>, SimpleParticleType> CALCITE_BUBBLE_PARTICLE_HOLDER =
      PARTICLE_TYPES.register("calcite_bubble", () -> new SimpleParticleType(false));

  public static SimpleParticleType redMapleLeavesParticle() {
    return RED_MAPLE_LEAVES_PARTICLE_HOLDER.get();
  }

  public static SimpleParticleType orangeMapleLeavesParticle() {
    return ORANGE_MAPLE_LEAVES_PARTICLE_HOLDER.get();
  }

  public static SimpleParticleType yellowMapleLeavesParticle() {
    return YELLOW_MAPLE_LEAVES_PARTICLE_HOLDER.get();
  }

  public static SimpleParticleType milkParticle() {
    return MILK_PARTICLE_HOLDER.get();
  }

  public static SimpleParticleType calciteBubbleParticle() {
    return CALCITE_BUBBLE_PARTICLE_HOLDER.get();
  }

  public static void init(IEventBus modBus) {
    PARTICLE_TYPES.register(modBus);
  }

  public static void registerParticleTypes(IEventBus modBus) {
    init(modBus);
  }
}
