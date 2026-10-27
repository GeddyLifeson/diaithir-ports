package cn.leolezury.eternalstarlight.common.client.compat;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.Entity;

/**
 * Generic render state carrying a reference to the source entity so that the pre-26.1 style
 * (entity based) models, renderers and layers can keep reading entity data on the render thread.
 */
public class ESRenderState extends LivingEntityRenderState {
	public Entity entity;
	public float entityYaw;
	public float partialTicks;
}
