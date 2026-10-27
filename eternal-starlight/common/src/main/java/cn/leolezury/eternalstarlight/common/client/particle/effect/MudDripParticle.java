package cn.leolezury.eternalstarlight.common.client.particle.effect;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.DripParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.material.Fluids;

/**
 * Re-implementation of vanilla's (now private) hang/fall/land drip particles for mud.
 */
public class MudDripParticle extends DripParticle {
	public enum Kind {
		HANG,
		FALL,
		LAND
	}

	private final Kind kind;
	private final ParticleOptions next;

	public MudDripParticle(ClientLevel level, double x, double y, double z, Kind kind, ParticleOptions next, TextureAtlasSprite sprite) {
		super(level, x, y, z, Fluids.EMPTY, sprite);
		this.kind = kind;
		this.next = next;
		switch (kind) {
			case HANG -> {
				this.gravity *= 0.02F;
				this.lifetime = 40;
			}
			case FALL -> this.lifetime = (int) (64.0 / (this.random.nextFloat() * 0.8 + 0.2));
			case LAND -> this.lifetime = (int) (16.0 / (this.random.nextFloat() * 0.8 + 0.2));
		}
	}

	@Override
	protected void preMoveUpdate() {
		if (this.lifetime-- <= 0) {
			this.remove();
			if (this.kind == Kind.HANG && this.next != null) {
				this.level.addParticle(this.next, this.x, this.y, this.z, this.xd, this.yd, this.zd);
			}
		}
	}

	@Override
	protected void postMoveUpdate() {
		switch (this.kind) {
			case HANG -> {
				this.xd *= 0.02;
				this.yd *= 0.02;
				this.zd *= 0.02;
			}
			case FALL -> {
				if (this.onGround) {
					this.remove();
					if (this.next != null) {
						this.level.addParticle(this.next, this.x, this.y, this.z, 0.0, 0.0, 0.0);
					}
				}
			}
			default -> {
			}
		}
	}
}
