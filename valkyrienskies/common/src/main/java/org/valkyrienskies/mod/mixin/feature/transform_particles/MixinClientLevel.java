package org.valkyrienskies.mod.mixin.feature.transform_particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import org.joml.Matrix4dc;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

/**
 * Spawns particles requested at shipyard coordinates at the matching in-world position instead.
 *
 * <p>{@link MixinParticle} alone is not enough because the spawn path applies a distance cull before
 * a {@code Particle} is ever constructed - shipyard coordinates are millions of blocks from the
 * camera, so every particle on a ship was discarded here. Conversely this mixin alone is not enough
 * either, because not every particle is spawned through this path.
 *
 * <p>26.2 notes: {@code LevelRenderer.addParticleInternal} is gone; the funnel that all four
 * {@code addParticle}/{@code addAlwaysVisibleParticle} overloads reach - and that carries the
 * {@code camera.position().distanceToSqr(...) > 1024} cull - is now the private
 * {@code ClientLevel.doAddParticle}. It returns {@code void} rather than the spawned particle, so we
 * cancel and re-dispatch instead of substituting a return value.
 */
@Mixin(ClientLevel.class)
public abstract class MixinClientLevel {

    @Shadow
    private native void doAddParticle(ParticleOptions options, boolean alwaysSpawn, boolean canSpawnOnMinimal,
        double x, double y, double z, double velocityX, double velocityY, double velocityZ);

    @Inject(
        method = "doAddParticle(Lnet/minecraft/core/particles/ParticleOptions;ZZDDDDDD)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void vs$spawnParticleInWorld(final ParticleOptions options, final boolean alwaysSpawn,
        final boolean canSpawnOnMinimal, final double x, final double y, final double z,
        final double velocityX, final double velocityY, final double velocityZ, final CallbackInfo ci) {

        final ClientLevel self = ClientLevel.class.cast(this);
        final ClientShip ship = VSGameUtilsKt.getLoadedShipManagingPos(self, (int) x >> 4, (int) z >> 4);

        if (ship == null) {
            // vanilla behaviour
            return;
        }

        final Matrix4dc transform = ship.getRenderTransform().getShipToWorldMatrix();
        // in-world position
        final Vector3d p = transform.transformPosition(new Vector3d(x, y, z));

        // in-world velocity
        final Vector3d v = transform
            // Rotate velocity wrt ship transform
            .transformDirection(new Vector3d(velocityX, velocityY, velocityZ))
            // Tack on the ship's linear velocity (multiplied by 1/20 because particle velocity is per tick)
            .fma(0.05, ship.getVelocity());

        // Re-dispatch with world coordinates. This does not recurse: the new position is in world
        // space, so the shipyard lookup above returns null on the second pass.
        ci.cancel();
        doAddParticle(options, alwaysSpawn, canSpawnOnMinimal, p.x, p.y, p.z, v.x, v.y, v.z);
    }
}
