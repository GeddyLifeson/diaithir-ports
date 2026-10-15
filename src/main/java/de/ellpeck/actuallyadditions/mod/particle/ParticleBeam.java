/*
 * This file ("ParticleBeam.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.particle;

import net.minecraft.util.RandomSource;

import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.VertexConsumer;
import de.ellpeck.actuallyadditions.mod.util.AssetUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.Nullable;

public class ParticleBeam extends Particle {

    private final double endX;
    private final double endY;
    private final double endZ;
    private final int color;
    private final double rotationTime;
    private final float size;
    private final float alpha;

    public ParticleBeam(ClientLevel world, double startX, double startY, double startZ, double endX, double endY, double endZ,
                        int color, int maxAge, double rotationTime, float size) {
        super(world, startX, startY, startZ);
        this.endX = endX;
        this.endY = endY;
        this.endZ = endZ;
        this.color = color;
        this.rotationTime = rotationTime;
        this.size = size;
        this.lifetime = maxAge;
        this.alpha = ARGB.alpha(color) / 255.0F;
    }


    @Override
    public ParticleRenderType getGroup() {
        return AAParticleGroups.BEAMS;
    }

    /** The beam as it should be drawn this frame, fading out over its life. */
    Beam extract(Vec3 camera) {
        float ageRatio = (float) this.age / (float) this.lifetime;
        return new Beam(camera, this.x + 0.5, this.y + 0.5, this.z + 0.5, this.endX + 0.5, this.endY + 0.5, this.endZ + 0.5,
            (float) this.rotationTime, this.alpha - ageRatio * this.alpha, this.size, this.color);
    }

    record Beam(Vec3 camera, double x, double y, double z, double endX, double endY, double endZ, float rotationTime, float alpha, float size, int color) {
        void draw(VertexConsumer buffer) {
            AssetUtil.renderLaserParticle(buffer, camera, x, y, z, endX, endY, endZ, rotationTime, alpha, size, color);
        }
    }


    public static class Factory implements ParticleProvider<BeamParticleData> {
        public Factory(SpriteSet sprite) {

        }

        @Override
        public Particle createParticle(BeamParticleData data, ClientLevel worldIn, double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
            return new ParticleBeam(worldIn, x, y, z, data.endX(), data.endY(), data.endZ(), data.color(), data.maxAge(),
                    data.rotationTime(), data.size());
        }

        public static ParticleOptions createData(double endX, double endY, double endZ, int color,
                                                 int maxAge, double rotationTime, float size) {
            return new BeamParticleData(endX, endY, endZ, color, maxAge, rotationTime, size);
        }

        public static ParticleOptions createData(double endX, double endY, double endZ, int color, float alpha,
                                                 int maxAge, double rotationTime, float size) {
            return new BeamParticleData(endX, endY, endZ, ARGB.color((int)(alpha * 255F), color), maxAge, rotationTime, size);
        }
    }
}
