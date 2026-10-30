package org.valkyrienskies.mod.mixin.server.command.level;

import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Relative;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.IEntityDraggingInformationProvider;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

// 26.2: teleportTo/dismountTo are declared directly on Entity now (ServerPlayer no longer
// overrides them), so this mixin retargets from ServerPlayer to Entity to match.
@Mixin(Entity.class)
public abstract class MixinServerPlayer {

    @Shadow
    public abstract void teleportTo(double d, double e, double f);

    @Shadow
    public abstract boolean teleportTo(ServerLevel serverLevel, double d, double e, double f, Set<Relative> set, float g, float h, boolean setCamera);

    @Shadow
    public abstract net.minecraft.world.phys.Vec3 getLookAngle();

    @Shadow
    public abstract void setYRot(float f);

    @Shadow
    public abstract void setXRot(float f);

    @Shadow
    public abstract float getYRot();

    @Shadow
    public abstract float getXRot();

    @Inject(
        at = @At("HEAD"),
        method = "teleportTo(DDD)V",
        cancellable = true
    )
    private void beforeTeleportTo(final double x, final double y, final double z, final CallbackInfo ci) {
        ServerLevel level = (ServerLevel) ((Entity) (Object) this).level();
        final Ship ship = VSGameUtilsKt.getShipManagingPos(level, x, y, z);
        if (ship != null) {
            ci.cancel();
            final Vector3d inWorld = VSGameUtilsKt.toWorldCoordinates(ship, x, y, z);
            this.teleportTo(inWorld.x, inWorld.y, inWorld.z);
        }
    }

    @Inject(
        at = @At("HEAD"),
        method = "dismountTo",
        cancellable = true
    )
    private void beforeDismountTo(final double x, final double y, final double z, final CallbackInfo ci) {
        ServerLevel level = (ServerLevel) ((Entity) (Object) this).level();
        final Ship ship = VSGameUtilsKt.getShipManagingPos(level, x, y, z);
        if (ship != null) {
            ci.cancel();

            Vector3d lookVector = VectorConversionsMCKt.toJOML(this.getLookAngle());
            final Vector3d transformedLook = ship.getTransform().getShipToWorld().transformDirection(lookVector);
            final double yaw = Math.atan2(-transformedLook.x, transformedLook.z) * 180.0 / Math.PI;
            final double pitch = Math.atan2(-transformedLook.y, Math.sqrt((transformedLook.x * transformedLook.x) + (transformedLook.z * transformedLook.z))) * 180.0 / Math.PI;
            this.setYRot((float) yaw);
            this.setXRot((float) pitch);

            //Predict the position 2 ticks ahead for dismount
            final Vector3d inWorld = ship.getTransform().getShipToWorld().transformPosition(x, y, z, new Vector3d());
            final Vector3d inWorldPrev = ship.getPrevTickTransform().getShipToWorld().transformPosition(x, y, z, new Vector3d());
            final Vector3d inWorldNext = inWorld.mul(3, new Vector3d()).sub(inWorldPrev.mul(2, new Vector3d()));
            this.teleportTo(level, inWorldNext.x, inWorldNext.y, inWorldNext.z, Set.of(), this.getYRot(), this.getXRot(), false);
            ((IEntityDraggingInformationProvider) this).vs$dragImmediately(ship);
        }
    }
}
