package cn.leolezury.eternalstarlight.common.client.compat;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SpriteSet;

/**
 * Stand-in for the old SimpleAnimatedParticle, built on {@link ESTextureSheetParticle} so that subclasses can override
 * render(VertexConsumer, Camera, float).
 */
public abstract class ESSimpleAnimatedParticle extends ESTextureSheetParticle {
	protected final SpriteSet sprites;
	private float fadeR;
	private float fadeG;
	private float fadeB;
	private boolean hasFade;

	protected ESSimpleAnimatedParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites, float gravity) {
		super(level, x, y, z);
		this.friction = 0.91F;
		this.gravity = gravity;
		this.sprites = sprites;
		this.setSprite(sprites.first());
	}

	public void setColor(int rgb) {
		float r = ((rgb & 0xFF0000) >> 16) / 255.0F;
		float g = ((rgb & 0xFF00) >> 8) / 255.0F;
		float b = ((rgb & 0xFF)) / 255.0F;
		this.setColor(r, g, b);
	}

	public void setFadeColor(int rgb) {
		this.fadeR = ((rgb & 0xFF0000) >> 16) / 255.0F;
		this.fadeG = ((rgb & 0xFF00) >> 8) / 255.0F;
		this.fadeB = ((rgb & 0xFF)) / 255.0F;
		this.hasFade = true;
	}

	@Override
	public net.minecraft.client.particle.ParticleRenderType getRenderType() {
		return ESParticleTypes.CUSTOM;
	}

	@Override
	public void tick() {
		super.tick();
		this.setSpriteFromAge(this.sprites);
		if (this.age > this.lifetime / 2) {
			this.setAlpha(1.0F - ((float) this.age - (float) (this.lifetime / 2)) / (float) this.lifetime);
			if (this.hasFade) {
				this.rCol += (this.fadeR - this.rCol) * 0.2F;
				this.gCol += (this.fadeG - this.gCol) * 0.2F;
				this.bCol += (this.fadeB - this.bCol) * 0.2F;
			}
		}
	}

	@Override
	protected int getLightColor(float partialTick) {
		return 15728880;
	}
}
