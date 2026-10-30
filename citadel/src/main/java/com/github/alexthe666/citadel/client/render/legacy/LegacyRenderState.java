package com.github.alexthe666.citadel.client.render.legacy;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.Entity;

/** Render state that carries the live entity so 1.21-style renderers and models can keep reading it. */
public class LegacyRenderState extends LivingEntityRenderState {
    public Entity entity;
    public float partialTick;
}
