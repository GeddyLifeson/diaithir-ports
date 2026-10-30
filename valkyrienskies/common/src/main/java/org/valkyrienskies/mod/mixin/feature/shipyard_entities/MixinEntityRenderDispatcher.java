package org.valkyrienskies.mod.mixin.feature.shipyard_entities;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4dc;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.entity.ShipMountedToData;
import org.valkyrienskies.mod.mixinducks.client.render.EntityRenderStateDuck;

/**
 * Draws entities that belong to a ship at their in-world position and orientation.
 *
 * <p>Two cases: an entity standing in the shipyard (a dropped item on a deck, a mob) has a position
 * in shipyard space, millions of blocks from the camera; and an entity riding a ship (a player on a
 * helm seat) sits at a mount offset that only means anything once the ship transform is applied.
 * Without this, a seated player was drawn detached from the deck and shipyard entities were culled
 * outright.
 *
 * <p>26.2 notes: {@code render(...)} is gone - a frame is now {@code extractEntity(Entity, float)}
 * then {@code submit(EntityRenderState, ...)}, and only the first still has the {@code Entity}. So
 * extract resolves the ship, rewrites the render state's world position and stashes the ship on the
 * state (see {@link EntityRenderStateDuck}); submit then only has to add the rotation. This replaces
 * upstream's popPose/pushPose trick, which relied on doing all of it in one method.
 */
@Mixin(value = EntityRenderDispatcher.class, priority = 500)
public class MixinEntityRenderDispatcher {

    @Inject(method = "distanceToSqr(Lnet/minecraft/world/entity/Entity;)D", at = @At("HEAD"), cancellable = true)
    private void vs$preDistanceToSqr(final Entity entity, final CallbackInfoReturnable<Double> cir) {
        // entity seems to be null sometimes when the "real camera" mod is used
        if (entity == null) {
            return;
        }
        final Vec3 pos = entity.position();
        cir.setReturnValue(VSGameUtilsKt.squaredDistanceToInclShips(entity, pos.x, pos.y, pos.z));
    }

    /**
     * Resolves the entity's ship and moves the extracted render state into world space.
     */
    @Inject(
        method = "extractEntity(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;",
        at = @At("RETURN")
    )
    private <E extends Entity> void vs$extractShipEntity(final E entity, final float partialTick,
        final CallbackInfoReturnable<EntityRenderState> cir) {

        final EntityRenderState state = cir.getReturnValue();
        if (state == null) {
            return;
        }

        // A rider's own position is kept in world space, so it cannot be transformed like the cases
        // below - the seat's mount position is the only shipyard-space quantity available. Note that
        // is the position the rider is *attached* at, not where it stands: vanilla's positionRider
        // places a rider at ridingPos minus its vehicle attachment point, so that has to be
        // subtracted here too or the entity is drawn floating above the seat.
        final ShipMountedToData mountedTo = VSGameUtilsKt.getShipMountedToData(entity, partialTick);
        if (mountedTo != null && mountedTo.getShipMountedTo() instanceof final ClientShip ship) {
            final Vector3d mountPos = new Vector3d(mountedTo.getMountPosInShip());
            final Entity vehicle = entity.getVehicle();
            if (vehicle != null) {
                final Vec3 attachment = entity.getVehicleAttachmentPoint(vehicle);
                mountPos.sub(attachment.x, attachment.y, attachment.z);
            }
            final Vector3dc inWorld = ship.getRenderTransform().getShipToWorld().transformPosition(mountPos);
            state.x = inWorld.x();
            state.y = inWorld.y();
            state.z = inWorld.z();
            ((EntityRenderStateDuck) state).vs$setShip(ship);
            return;
        }

        final Ship standingOn = VSGameUtilsKt.getLoadedShipManagingPos(entity.level(), entity.blockPosition());
        if (standingOn instanceof final ClientShip ship) {
            final Vector3dc inWorld = ship.getRenderTransform().getShipToWorld()
                .transformPosition(new Vector3d(state.x, state.y, state.z));
            state.x = inWorld.x();
            state.y = inWorld.y();
            state.z = inWorld.z();
            ((EntityRenderStateDuck) state).vs$setShip(ship);
            return;
        }

        // Render states are pooled and reused across frames, so a stale ship has to be cleared.
        ((EntityRenderStateDuck) state).vs$setShip(null);
    }

    /**
     * Adds the ship's rotation and scale on top of the position translate.
     *
     * <p>{@code ordinal = 0} picks the translate that places the entity itself; the later ones in
     * this method position the fire overlay and the shadow, which are already relative to it.
     */
    @WrapOperation(
        method = "submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lnet/minecraft/client/renderer/state/level/CameraRenderState;DDDLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V",
            ordinal = 0
        )
    )
    private void vs$applyShipOrientation(final PoseStack poseStack, final double x, final double y,
        final double z, final Operation<Void> original,
        @Local(argsOnly = true) final EntityRenderState state) {

        original.call(poseStack, x, y, z);

        final ClientShip ship = ((EntityRenderStateDuck) state).vs$getShip();
        if (ship == null) {
            return;
        }
        poseStack.mulPose(new Quaternionf(ship.getRenderTransform().getShipToWorldRotation()));
        final Vector3dc scale = ship.getRenderTransform().getShipToWorldScaling();
        poseStack.scale((float) scale.x(), (float) scale.y(), (float) scale.z());
    }

    /**
     * Frustum-culls shipyard entities by their transformed world bounding box.
     *
     * <p>Their own bounding box is in shipyard space and can never intersect the view frustum, so
     * every entity on a ship failed the vanilla test and was dropped before extract ran.
     */
    @ModifyReturnValue(method = "shouldRender", at = @At("RETURN"))
    private boolean vs$shouldRenderShipEntity(final boolean original, final Entity entity,
        final Frustum frustum, final double camX, final double camY, final double camZ) {

        if (original) {
            return true;
        }

        final Ship nullableShip = VSGameUtilsKt.getLoadedShipManagingPos(entity.level(), entity.blockPosition());
        if (!(nullableShip instanceof final ClientShip ship)) {
            return false;
        }

        // 26.2 removed Entity.getBoundingBoxForCulling(); the renderer-specific culling box now lives
        // on EntityRenderer, and the plain bounding box is what this coarse pre-test needs anyway.
        AABB box = entity.getBoundingBox().inflate(0.5);
        if (box.hasNaN() || box.getSize() == 0.0) {
            box = new AABB(entity.getX() - 2.0, entity.getY() - 2.0, entity.getZ() - 2.0,
                entity.getX() + 2.0, entity.getY() + 2.0, entity.getZ() + 2.0);
        }

        // Transform all eight corners and take their bounds - the ship transform includes rotation,
        // so transforming only min/max would not give a correct enclosing box. (AABBd itself lives in
        // joml-primitives, which isn't on the classpath here.)
        final Matrix4dc shipToWorld = ship.getRenderTransform().getShipToWorld();
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;
        for (int corner = 0; corner < 8; corner++) {
            final Vector3d p = shipToWorld.transformPosition(new Vector3d(
                (corner & 1) == 0 ? box.minX : box.maxX,
                (corner & 2) == 0 ? box.minY : box.maxY,
                (corner & 4) == 0 ? box.minZ : box.maxZ
            ));
            minX = Math.min(minX, p.x);
            minY = Math.min(minY, p.y);
            minZ = Math.min(minZ, p.z);
            maxX = Math.max(maxX, p.x);
            maxY = Math.max(maxY, p.y);
            maxZ = Math.max(maxZ, p.z);
        }
        return frustum.isVisible(new AABB(minX, minY, minZ, maxX, maxY, maxZ));
    }
}
