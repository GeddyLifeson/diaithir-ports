package com.github.alexmodguy.alexscaves.client.particle;

import com.github.alexthe666.citadel.client.render.legacy.LegacyBuffers;
import com.github.alexthe666.citadel.client.render.legacy.RecordingBufferSource;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState;
import net.minecraft.resources.Identifier;

import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * A particle that draws itself the 1.21 way: render(VertexConsumer, Camera, partialTick), writing camera-relative vertices.
 * Its group records the call and replays the vertices through the 26.1 submit pipeline.
 */
public abstract class LegacyParticle extends Particle {
    public static final ParticleRenderType TYPE = new ParticleRenderType("alexscaves:legacy");
    private static final Identifier PARTICLE_ATLAS = Identifier.withDefaultNamespace("textures/atlas/particles.png");

    // TextureSheetParticle look-alike state for particles that draw their own quads
    protected float quadSize = 0.1F * (this.random.nextFloat() * 0.5F + 0.5F) * 2.0F;
    protected float rCol = 1.0F, gCol = 1.0F, bCol = 1.0F, alpha = 1.0F;
    protected float roll, oRoll;
    protected TextureAtlasSprite sprite;

    protected LegacyParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
    }

    protected LegacyParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
        super(level, x, y, z, xd, yd, zd);
    }

    public void setColor(float r, float g, float b) {
        this.rCol = r;
        this.gCol = g;
        this.bCol = b;
    }

    protected void setAlpha(float a) {
        this.alpha = a;
    }

    public float getQuadSize(float partialTick) {
        return this.quadSize;
    }

    @Override
    public Particle scale(float scale) {
        this.quadSize *= scale;
        return super.scale(scale);
    }

    protected void setSprite(TextureAtlasSprite s) {
        this.sprite = s;
    }

    public void pickSprite(SpriteSet sprites) {
        this.sprite = sprites.get(this.random);
    }

    public void setSpriteFromAge(SpriteSet sprites) {
        if (!this.removed) this.sprite = sprites.get(this.age, this.lifetime);
    }

    protected float getU0() {
        return sprite == null ? 0 : sprite.getU0();
    }

    protected float getU1() {
        return sprite == null ? 1 : sprite.getU1();
    }

    protected float getV0() {
        return sprite == null ? 0 : sprite.getV0();
    }

    protected float getV1() {
        return sprite == null ? 1 : sprite.getV1();
    }

    @Override
    public ParticleRenderType getGroup() {
        return TYPE;
    }

    /** Render type for the VertexConsumer passed to render(); the particle sheet by default. */
    protected RenderType legacyRenderType() {
        return RenderTypes.entityTranslucent(PARTICLE_ATLAS);
    }

    protected int getLightColor(float partialTick) {
        return super.getLightCoords(partialTick);
    }

    @Override
    protected int getLightCoords(float partialTick) {
        return getLightColor(partialTick);
    }

    public void setColor(int rgb) {
        this.rCol = ((rgb >> 16) & 255) / 255.0F;
        this.gCol = ((rgb >> 8) & 255) / 255.0F;
        this.bCol = (rgb & 255) / 255.0F;
    }

    /** Default 1.21 behaviour: a camera-facing sprite quad. */
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        Vec3 cam = camera.position();
        float x = (float) (Mth.lerp(partialTick, this.xo, this.x) - cam.x());
        float y = (float) (Mth.lerp(partialTick, this.yo, this.y) - cam.y());
        float z = (float) (Mth.lerp(partialTick, this.zo, this.z) - cam.z());
        Quaternionf q;
        if (this.roll == 0.0F) {
            q = camera.rotation();
        } else {
            q = new Quaternionf(camera.rotation());
            q.rotateZ(Mth.lerp(partialTick, this.oRoll, this.roll));
        }
        Vector3f[] v = {new Vector3f(-1, -1, 0), new Vector3f(-1, 1, 0), new Vector3f(1, 1, 0), new Vector3f(1, -1, 0)};
        float size = getQuadSize(partialTick);
        for (Vector3f p : v) {
            p.rotate(q);
            p.mul(size);
            p.add(x, y, z);
        }
        float u0 = getU0(), u1 = getU1(), v0 = getV0(), v1 = getV1();
        int light = getLightColor(partialTick);
        int color = ARGB.colorFromFloat(this.alpha, this.rCol, this.gCol, this.bCol);
        consumer.addVertex(v[0].x(), v[0].y(), v[0].z()).setUv(u1, v1).setColor(color).setLight(light);
        consumer.addVertex(v[1].x(), v[1].y(), v[1].z()).setUv(u1, v0).setColor(color).setLight(light);
        consumer.addVertex(v[2].x(), v[2].y(), v[2].z()).setUv(u0, v0).setColor(color).setLight(light);
        consumer.addVertex(v[3].x(), v[3].y(), v[3].z()).setUv(u0, v1).setColor(color).setLight(light);
    }

    public static class Group extends ParticleGroup<LegacyParticle> {
        public Group(ParticleEngine engine) {
            super(engine);
        }

        @Override
        public ParticleGroupRenderState extractRenderState(Frustum frustum, Camera camera, float partialTickTime) {
            List<LegacyParticle> visible = new ArrayList<>();
            for (LegacyParticle p : this.particles) {
                if (frustum.pointInFrustum(p.x, p.y, p.z)) visible.add(p);
            }
            return (collector, cameraState) -> {
                for (LegacyParticle p : visible) {
                    RecordingBufferSource rec = new RecordingBufferSource();
                    LegacyBuffers.set(rec);
                    try {
                        p.render(rec.getBuffer(p.legacyRenderType()), camera, partialTickTime);
                    } catch (RuntimeException e) {
                        p.remove();
                    } finally {
                        LegacyBuffers.set(null);
                    }
                    rec.flush(collector);
                }
            };
        }
    }
}
