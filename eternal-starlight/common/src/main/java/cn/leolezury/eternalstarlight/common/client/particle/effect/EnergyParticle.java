package cn.leolezury.eternalstarlight.common.client.particle.effect;

import net.minecraft.util.RandomSource;
import cn.leolezury.eternalstarlight.common.client.compat.ESParticleTypes;
import cn.leolezury.eternalstarlight.common.client.compat.ESTextureSheetParticle;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.core.particles.SimpleParticleType;

public class EnergyParticle extends ESTextureSheetParticle {
	protected EnergyParticle(ClientLevel clientLevel, double x, double y, double z, double dx, double dy, double dz) {
		super(clientLevel, x, y, z, dx, dy, dz);
		this.friction = 0.96F;
		this.xd = this.xd * 0.01F + dx;
		this.yd = this.yd * 0.01F + dy;
		this.zd = this.zd * 0.01F + dz;
		this.lifetime = (int) (8.0 / (this.random.nextFloat() * 0.8 + 0.2)) + 4;
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ESParticleTypes.CUSTOM;
	}

	@Override
	public void move(double x, double y, double z) {
		this.setBoundingBox(this.getBoundingBox().move(x, y, z));
		this.setLocationFromBoundingbox();
	}

	@Override
	public float getQuadSize(float f) {
		float progress = (this.age + f) / this.lifetime;
		return this.quadSize * (1.0F - progress * progress * 0.5F);
	}

	@Override
	public int getLightColor(float f) {
		return LightCoordsUtil.FULL_BRIGHT;
	}

	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprite;

		public Provider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		@Override
		public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy, double dz, RandomSource random) {
			EnergyParticle particle = new EnergyParticle(level, x, y, z, dx, dy, dz);
			particle.pickSprite(this.sprite);
			return particle;
		}
	}
}
