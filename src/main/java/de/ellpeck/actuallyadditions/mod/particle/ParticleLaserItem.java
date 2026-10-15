/*
 * This file ("ParticleLaserItem.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.particle;

import net.minecraft.util.RandomSource;

import net.minecraft.world.item.ItemDisplayContext;

import net.minecraft.client.renderer.item.ItemStackRenderState;

import net.minecraft.client.renderer.LevelRenderer;

import net.minecraft.client.Minecraft;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.util.Util;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

//TODO: Figure out how to make items render ghostly (translucent)
public class ParticleLaserItem extends Particle {

    private final double otherX;
    private final double otherY;
    private final double otherZ;

    private final ItemStack stack;

    private ParticleLaserItem(ClientLevel world, double posX, double posY, double posZ, ItemStack stack, double motionY) {
        this(world, posX, posY, posZ, stack, motionY, 0, 0, 0);
    }

    public ParticleLaserItem(ClientLevel world, double posX, double posY, double posZ, ItemStack stack, double motionY, double otherX, double otherY, double otherZ) {
        super(world, posX + (world.getRandom().nextDouble() - 0.5) / 8, posY, posZ + (world.getRandom().nextDouble() - 0.5) / 8);
        this.stack = stack;
        this.otherX = otherX;
        this.otherY = otherY;
        this.otherZ = otherZ;

        this.xd = 0;
        this.yd = motionY;
        this.zd = 0;

        this.lifetime = 10;
        this.hasPhysics = false;
    }

    @Override
    public void remove() {
        super.remove();

        if (this.otherX != 0 || this.otherY != 0 || this.otherZ != 0) {
            this.level.addParticle(Factory.createData(this.stack, 0, 0, 0),
                    this.otherX, this.otherY, this.otherZ, 0, -0.025, 0);
        }
    }


    @Override
    public ParticleRenderType getGroup() {
        return AAParticleGroups.ITEMS;
    }

    /** The item spinning at the particle's position, lit by the block it is in. */
    Floating extract(Vec3 camera) {
        ItemStackRenderState state = new ItemStackRenderState();
        Minecraft.getInstance().getItemModelResolver().updateForTopItem(state, this.stack, ItemDisplayContext.FIXED, this.level, null, 0);
        PoseStack pose = new PoseStack();
        pose.translate(this.x - camera.x, this.y - camera.y, this.z - camera.z);
        pose.scale(0.3F, 0.3F, 0.3F);
        pose.mulPose(Axis.YP.rotationDegrees((float) (Util.getMillis() / 600D * 40D % 360)));
        return new Floating(state, pose, LevelRenderer.getLightCoords(this.level, BlockPos.containing(this.x, this.y, this.z)));
    }

    record Floating(ItemStackRenderState state, PoseStack pose, int light) {}

    public static class Factory implements ParticleProvider<LaserItemParticleData> {
        public Factory(SpriteSet sprite) {

        }

        @Override
        public Particle createParticle(LaserItemParticleData data, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
            return new ParticleLaserItem(worldIn, x, y, z, data.stack(), ySpeed, data.outputX(), data.outputY(), data.outputZ());
        }

        public static ParticleOptions createData(ItemStack stack, double outputX, double outputY, double outputZ) {
            return new LaserItemParticleData(stack, outputX, outputY, outputZ);
        }
    }
}
