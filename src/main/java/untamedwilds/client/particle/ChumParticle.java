package untamedwilds.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
public class ChumParticle extends RisingParticle {
    private final SpriteSet spriteWithAge;

    public ChumParticle(ClientLevel world, double x, double y, double z, double motionX, double motionY, double motionZ, SpriteSet spriteWithAge) {
        super(world, x, y, z, motionX, Math.abs(motionY) * -1, motionZ, spriteWithAge.get(world.getRandom()));
        this.spriteWithAge = spriteWithAge;
        this.lifetime = this.random.nextInt(80) + 120;
        this.setSpriteFromAge(spriteWithAge);
    }

    @Override
    protected SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.spriteWithAge);
    }

    @Override
    public float getQuadSize(float scaleFactor) {
        float f = 0.6F + ((float)this.age + scaleFactor) / (float)this.lifetime;
        return this.quadSize * (1.0F + f * f * 0.5F);
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, net.minecraft.util.RandomSource random) {
            ChumParticle soulparticle = new ChumParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
            soulparticle.setAlpha(0.8F);
            return soulparticle;
        }
    }
}
