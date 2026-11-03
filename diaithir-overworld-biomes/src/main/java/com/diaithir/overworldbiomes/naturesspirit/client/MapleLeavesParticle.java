package com.diaithir.overworldbiomes.naturesspirit.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FallingLeavesParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class MapleLeavesParticle extends FallingLeavesParticle {

  protected MapleLeavesParticle(ClientLevel level, double x, double y, double z, TextureAtlasSprite sprite) {
    super(level, x, y, z, sprite, 0.25F, 2.0F, false, true, 1.0F, 0.0F);
  }
}
