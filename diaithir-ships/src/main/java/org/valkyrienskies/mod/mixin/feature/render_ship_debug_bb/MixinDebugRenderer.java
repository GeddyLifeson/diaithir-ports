package org.valkyrienskies.mod.mixin.feature.render_ship_debug_bb;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4dc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.primitives.AABBdc;
import org.joml.primitives.AABBic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.core.api.ships.properties.ShipTransform;
import org.valkyrienskies.core.internal.world.VsiClientShipWorld;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

@Mixin(DebugRenderer.class)
public class MixinDebugRenderer {

    /**
     * This mixin renders ship bounding boxes and center of masses.
     *
     * <p>They get rendered when entity hitboxes are shown.
     */
    @Inject(method = "emitGizmos", at = @At("HEAD"))
    private void vs$emitShipGizmos(final Frustum frustum, final double cameraX, final double cameraY,
        final double cameraZ, final float partialTicks, final CallbackInfo ci) {
        final Minecraft minecraft = Minecraft.getInstance();
        final ClientLevel world = minecraft.level;
        if (world == null || !minecraft.debugEntries.isCurrentlyEnabled(DebugScreenEntries.ENTITY_HITBOXES)) {
            return;
        }
        final VsiClientShipWorld shipObjectClientWorld = VSGameUtilsKt.getShipObjectWorld(world);

        for (final ClientShip shipObjectClient : shipObjectClientWorld.getLoadedShips()) {
            final ShipTransform shipRenderTransform = shipObjectClient.getRenderTransform();
            final Vector3dc shipRenderPosition = shipRenderTransform.getShipPositionInWorldCoordinates();

            // Center of mass
            final double renderRadius = .25;
            final AABB shipCenterOfMassBox =
                new AABB(shipRenderPosition.x() - renderRadius, shipRenderPosition.y() - renderRadius,
                    shipRenderPosition.z() - renderRadius, shipRenderPosition.x() + renderRadius,
                    shipRenderPosition.y() + renderRadius, shipRenderPosition.z() + renderRadius);
            Gizmos.cuboid(shipCenterOfMassBox,
                GizmoStyle.stroke(ARGB.colorFromFloat(1.0F, 250.0F / 255.0F, 194.0F / 255.0F, 19.0F / 255.0F)));

            // Render the ship's voxel AABB, which is not axis aligned in world space
            final AABBic shipVoxelAABBic = shipObjectClient.getShipAABB();
            if (shipVoxelAABBic != null) {
                final Matrix4dc shipToWorld = shipRenderTransform.getShipToWorld();
                final int red = ARGB.colorFromFloat(1.0F, 1.0F, 0.0F, 0.0F);
                final double[] xs = {shipVoxelAABBic.minX(), shipVoxelAABBic.maxX()};
                final double[] ys = {shipVoxelAABBic.minY(), shipVoxelAABBic.maxY()};
                final double[] zs = {shipVoxelAABBic.minZ(), shipVoxelAABBic.maxZ()};
                final Vec3[] corners = new Vec3[8];
                for (int i = 0; i < 8; i++) {
                    final Vector3d corner = shipToWorld.transformPosition(
                        new Vector3d(xs[i & 1], ys[(i >> 1) & 1], zs[(i >> 2) & 1]));
                    corners[i] = VectorConversionsMCKt.toMinecraft(corner);
                }
                for (int i = 0; i < 8; i++) {
                    for (int bit = 1; bit < 8; bit <<= 1) {
                        if ((i & bit) == 0) {
                            Gizmos.line(corners[i], corners[i | bit], red);
                        }
                    }
                }
            }

            // Render the ship's render AABB
            final AABBdc shipRenderAABBdc = shipObjectClient.getRenderAABB();
            final AABB shipRenderAABB = VectorConversionsMCKt.toMinecraft(shipRenderAABBdc);
            Gizmos.cuboid(shipRenderAABB,
                GizmoStyle.stroke(ARGB.colorFromFloat(1.0F, 234.0F / 255.0F, 0.0F, 217.0F / 255.0F)));
        }
    }
}
