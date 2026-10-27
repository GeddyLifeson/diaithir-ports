package cn.leolezury.eternalstarlight.common.client.compat;

import net.minecraft.client.particle.ParticleRenderType;

public final class ESParticleTypes {
	/** All particles drawn through {@link ESParticleGroup}; each particle picks its own RenderType. */
	public static final ParticleRenderType CUSTOM = new ParticleRenderType("eternal_starlight_custom");

	private ESParticleTypes() {
	}
}
