package cn.leolezury.eternalstarlight.common.client.compat;

import cn.leolezury.eternalstarlight.common.client.ESRenderType;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Stand-in for the removed TextureSheetParticle: a billboard quad particle that may override
 * {@link #render(VertexConsumer, Camera, float)} to write custom geometry.
 */
public abstract class ESTextureSheetParticle extends Particle {
	protected float quadSize;
	protected float rCol = 1.0F;
	protected float gCol = 1.0F;
	protected float bCol = 1.0F;
	protected float alpha = 1.0F;
	protected float roll;
	protected float oRoll;
	protected TextureAtlasSprite sprite;

	protected ESTextureSheetParticle(ClientLevel level, double x, double y, double z) {
		super(level, x, y, z);
		this.quadSize = 0.1F * (this.random.nextFloat() * 0.5F + 0.5F) * 2.0F;
	}

	protected ESTextureSheetParticle(ClientLevel level, double x, double y, double z, double xa, double ya, double za) {
		super(level, x, y, z, xa, ya, za);
		this.quadSize = 0.1F * (this.random.nextFloat() * 0.5F + 0.5F) * 2.0F;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}

	/** Old API name. */
	public abstract ParticleRenderType getRenderType();

	@Override
	public ParticleRenderType getGroup() {
		return ESParticleTypes.CUSTOM;
	}

	/** The render type the geometry of this particle is drawn with. */
	public RenderType getESRenderType() {
		return ESRenderType.PARTICLE_TRANSLUCENT;
	}

	public void render(VertexConsumer consumer, Camera camera, float partialTicks) {
		this.renderRotatedQuad(consumer, camera, new Quaternionf(camera.rotation()), partialTicks);
	}

	protected void renderRotatedQuad(VertexConsumer consumer, Camera camera, Quaternionf rotation, float partialTicks) {
		Vec3 camPos = camera.position();
		float px = (float) (Mth.lerp(partialTicks, this.xo, this.x) - camPos.x());
		float py = (float) (Mth.lerp(partialTicks, this.yo, this.y) - camPos.y());
		float pz = (float) (Mth.lerp(partialTicks, this.zo, this.z) - camPos.z());
		if (this.roll != 0.0F) {
			rotation.rotateZ(Mth.lerp(partialTicks, this.oRoll, this.roll));
		}
		Vector3f[] verts = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
		float size = this.getQuadSize(partialTicks);
		for (Vector3f v : verts) {
			v.rotate(rotation).mul(size).add(px, py, pz);
		}
		float u0 = this.getU0();
		float u1 = this.getU1();
		float v0 = this.getV0();
		float v1 = this.getV1();
		int light = this.getLightColor(partialTicks);
		consumer.addVertex(verts[0].x(), verts[0].y(), verts[0].z()).setUv(u1, v1).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(light);
		consumer.addVertex(verts[1].x(), verts[1].y(), verts[1].z()).setUv(u1, v0).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(light);
		consumer.addVertex(verts[2].x(), verts[2].y(), verts[2].z()).setUv(u0, v0).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(light);
		consumer.addVertex(verts[3].x(), verts[3].y(), verts[3].z()).setUv(u0, v1).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(light);
	}

	public float getQuadSize(float partialTicks) {
		return this.quadSize;
	}

	@Override
	public Particle scale(float scale) {
		this.quadSize *= scale;
		return super.scale(scale);
	}

	protected int getLightColor(float partialTick) {
		BlockPos pos = BlockPos.containing(this.x, this.y, this.z);
		return this.level.hasChunkAt(pos) ? LevelRenderer.getLightCoords(this.level, pos) : 0;
	}

	protected float getU0() {
		return this.sprite.getU0();
	}

	protected float getU1() {
		return this.sprite.getU1();
	}

	protected float getV0() {
		return this.sprite.getV0();
	}

	protected float getV1() {
		return this.sprite.getV1();
	}

	protected void setSprite(TextureAtlasSprite sprite) {
		this.sprite = sprite;
	}

	public void pickSprite(SpriteSet sprites) {
		this.setSprite(sprites.get(this.random));
	}

	public void setSpriteFromAge(SpriteSet sprites) {
		if (!this.removed) {
			this.setSprite(sprites.get(this.age, this.lifetime));
		}
	}

	public void setColor(float r, float g, float b) {
		this.rCol = r;
		this.gCol = g;
		this.bCol = b;
	}

	protected void setAlpha(float alpha) {
		this.alpha = alpha;
	}
}
