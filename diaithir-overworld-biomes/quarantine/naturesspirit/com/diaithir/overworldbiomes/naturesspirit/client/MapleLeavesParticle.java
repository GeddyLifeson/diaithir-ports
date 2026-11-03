package com.diaithir.overworldbiomes.naturesspirit.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FallingLeavesParticle;
import net.minecraft.client.particle.SpriteSet;

public class MapleLeavesParticle extends FallingLeavesParticle {

  protected MapleLeavesParticle(ClientLevel level, double x, double y, double z, SpriteSet spriteSet) {
    super(level, x, y, z, spriteSet, 0.25F, 2.0F, false, true, 1.0F, 0.0F);
    float f = this.random.nextBoolean() ? 0.133333F : 0.19999999995F;
    this.quadSize = f;
    this.setSize(f, f);
    this.friction = 0.78F;
  }
}
