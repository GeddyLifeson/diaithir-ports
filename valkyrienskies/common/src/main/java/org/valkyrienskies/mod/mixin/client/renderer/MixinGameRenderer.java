package org.valkyrienskies.mod.mixin.client.renderer;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.core.internal.world.VsiClientShipWorld;
import org.valkyrienskies.mod.common.IShipObjectWorldClientProvider;
import org.valkyrienskies.mod.common.entity.ShipMountedToData;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.EntityDragger;
import org.valkyrienskies.mod.common.util.EntityDraggingInformation;
import org.valkyrienskies.mod.common.util.IEntityDraggingInformationProvider;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {

    @Shadow
    @Final
    private Minecraft minecraft;

    // 26.2 NOTE: the old camera-mounting hook (setupCameraWithMountedShip, which wrapped
    // LevelRenderer.prepareCullFrustum inside the now-deleted renderLevel) moved to MixinCamera,
    // injected right after Camera.update -> alignWithEntity so the ship transform flows into the
    // cull frustum and the extracted CameraRenderState.
    //
    // 26.2 NOTE: the crosshair-pick corrections (modifyCrosshairTargetBlocks / correctDistanceChecks)
    // used to wrap GameRenderer.pick(Entity,DDF), which no longer exists - picking moved to
    // Minecraft.pick(float) / pickBlockOrEntity(). TODO Phase 2b: reimplement those on MixinMinecraft.
    //
    // 26.2 NOTE: includeShipsIn wrapped GameRenderer.getDepthFar (also deleted; depthFar is now a
    // Camera field set in Camera.update). TODO Phase 2b: extend the far plane for ships in MixinCamera.

    /**
     * Update ship render transforms and correct entity interpolation positions BEFORE the frame's
     * render state is extracted. In 26.2 rendering is split into extract() (snapshot) then render()
     * (GPU submit); entity positions are captured during extract(), so this must run at extract HEAD
     * (the old code injected at renderLevel/render HEAD, which is now too late).
     */
    @Inject(method = "extract", at = @At("HEAD"))
    private void preExtract(final DeltaTracker deltaTracker, final boolean bl, final CallbackInfo ci) {
        final ClientLevel clientWorld = minecraft.level;
        if (clientWorld != null) {
            // Update ship render transforms
            final VsiClientShipWorld shipWorld =
                IShipObjectWorldClientProvider.class.cast(this.minecraft).getShipObjectWorld();
            if (shipWorld == null) {
                return;
            }

            final float partialTick = deltaTracker.getGameTimeDeltaPartialTick(true);
            shipWorld.updateRenderTransforms(partialTick);

            // Also update entity last tick positions, so that they interpolate correctly
            for (final Entity entity : clientWorld.entitiesForRendering()) {
                if (!EntityDragger.isDraggable(entity)) {
                    continue;
                }
                // The position we want to render [entity] at for this frame
                // This is set when an entity is mounted to a ship, or an entity is being dragged by a ship
                Vector3dc entityShouldBeHere = null;

                // First, try getting the ship the entity is mounted to, if one exists
                final ShipMountedToData shipMountedToData = VSGameUtilsKt.getShipMountedToData(entity, partialTick);

                if (shipMountedToData != null) {
                    final ClientShip shipMountedTo = (ClientShip) shipMountedToData.getShipMountedTo();
                    // If the entity is mounted to a ship then update their position
                    final Vector3dc passengerPos = shipMountedToData.getMountPosInShip();
                    entityShouldBeHere = shipMountedTo.getRenderTransform().getShipToWorld()
                        .transformPosition(passengerPos, new Vector3d());
                    entity.setPos(entityShouldBeHere.x(), entityShouldBeHere.y(), entityShouldBeHere.z());
                    entity.xo = entityShouldBeHere.x();
                    entity.yo = entityShouldBeHere.y();
                    entity.zo = entityShouldBeHere.z();
                    entity.xOld = entityShouldBeHere.x();
                    entity.yOld = entityShouldBeHere.y();
                    entity.zOld = entityShouldBeHere.z();
                    continue;
                }

                final EntityDraggingInformation entityDraggingInformation =
                    ((IEntityDraggingInformationProvider) entity).getDraggingInformation();
                final Long lastShipStoodOn = entityDraggingInformation.getLastShipStoodOn();
                // Then try getting [entityShouldBeHere] from [entityDraggingInformation]
                if (lastShipStoodOn != null && entityDraggingInformation.isEntityBeingDraggedByAShip()) {
                    final ClientShip shipObject =
                        VSGameUtilsKt.getShipObjectWorld(clientWorld).getLoadedShips().getById(lastShipStoodOn);
                    if (shipObject != null) {
                        entityDraggingInformation.setCachedLastPosition(
                            new Vector3d(entity.xo, entity.yo, entity.zo));
                        entityDraggingInformation.setRestoreCachedLastPosition(true);

                        // The velocity added to the entity by ship dragging
                        final Vector3dc entityAddedVelocity = entityDraggingInformation.getAddedMovementLastTick();

                        // The velocity of the entity before we added ship dragging
                        final double entityMovementX = entity.getX() - entityAddedVelocity.x() - entity.xo;
                        final double entityMovementY = entity.getY() - entityAddedVelocity.y() - entity.yo;
                        final double entityMovementZ = entity.getZ() - entityAddedVelocity.z() - entity.zo;

                        // Without ship dragging, the entity would've been here
                        final Vector3dc entityShouldBeHerePreTransform = new Vector3d(
                            entity.xo + entityMovementX * partialTick,
                            entity.yo + entityMovementY * partialTick,
                            entity.zo + entityMovementZ * partialTick
                        );

                        // Move [entityShouldBeHerePreTransform] with the ship, using the prev transform and the
                        // current render transform
                        entityShouldBeHere = shipObject.getRenderTransform().getShipToWorldMatrix()
                            .transformPosition(
                                shipObject.getPrevTickShipTransform().getWorldToShipMatrix()
                                    .transformPosition(entityShouldBeHerePreTransform, new Vector3d()));
                    }
                }

                // Apply entityShouldBeHere, if its present
                //
                // Also, don't run this if [tickDelta] is too small, getting so close to dividing by 0 could mess
                // something up
                if (entityShouldBeHere != null && partialTick < .99999) {
                    // Update the entity last tick positions such that the entity's render position will be
                    // interpolated to be [entityShouldBeHere]
                    entity.xo = (entityShouldBeHere.x() - (entity.getX() * partialTick)) / (1.0 - partialTick);
                    entity.yo = (entityShouldBeHere.y() - (entity.getY() * partialTick)) / (1.0 - partialTick);
                    entity.zo = (entityShouldBeHere.z() - (entity.getZ() * partialTick)) / (1.0 - partialTick);
                }
            }
        }
    }

    @Inject(method = "extract", at = @At("TAIL"))
    private void postExtract(final DeltaTracker deltaTracker, final boolean bl, final CallbackInfo ci) {
        final ClientLevel clientWorld = minecraft.level;
        if (clientWorld != null) {
            // Restore the entity last tick positions that were replaced during this frame
            for (final Entity entity : clientWorld.entitiesForRendering()) {
                final EntityDraggingInformation vsEntity =
                    ((IEntityDraggingInformationProvider) entity).getDraggingInformation();
                if (vsEntity.getRestoreCachedLastPosition()) {
                    vsEntity.setRestoreCachedLastPosition(false);
                    final Vector3dc cachedLastPosition = vsEntity.getCachedLastPosition();
                    if (cachedLastPosition != null) {
                        entity.xo = cachedLastPosition.x();
                        entity.yo = cachedLastPosition.y();
                        entity.zo = cachedLastPosition.z();
                    } else {
                        System.err.println("How was cachedLastPosition was null?");
                    }
                }
            }
        }
    }
}
