package org.valkyrienskies.mod.mixin.feature.render_blockentity_distance_check;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4dc;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

/**
 * This mixin fixes {@link BlockEntity}s belonging to ships not rendering.
 *
 * <p>Block entities are culled by {@code BlockEntityRenderer.shouldRender(be, cameraPos)}, a plain
 * distance test against the block's own position. A ship's blocks live in shipyard space, millions
 * of blocks from the camera, so that test always fails and the block entity is dropped before a
 * render state is ever extracted - a chest or a ship helm's wheel simply vanished the moment its
 * ship was assembled. When the vanilla test fails, re-test using the block's position transformed
 * into world space by the ship's render transform.
 *
 * <p>26.1 notes: the cull moved from {@code render} into {@code tryExtractRenderState} - and into
 * the Frustum overload NeoForge patches in, since the 3-arg one is only a delegate, so the
 * descriptor has to name the Frustum version. The dispatcher also lost its {@code level}/{@code
 * camera} fields, so the level comes from {@link Minecraft} and the camera position from the wrapped
 * call's own argument. {@code BlockEntityRenderer} is now {@code <T, S>}, hence the wildcard.
 */
@Mixin(BlockEntityRenderDispatcher.class)
public class MixinBlockEntityRenderDispatcher {

    /**
     * Frustum-culls a ship's block entities by their transformed world position.
     *
     * <p>{@code tryExtractRenderState} runs this check <em>before</em> the distance check below and
     * bails out with {@code null} when it fails, so it has to be fixed too or the distance fix never
     * even gets a chance to run. {@code getRenderBoundingBox} returns an AABB in the block entity's
     * own (shipyard) coordinates - millions of blocks from the camera - so it can never intersect the
     * view frustum and every ship block entity was discarded here.
     *
     * <p>When the plain test fails, retry with the box transformed into world space by the ship's
     * render transform.
     */
    @WrapOperation(
        method = "tryExtractRenderState(Lnet/minecraft/world/level/block/entity/BlockEntity;FLnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;Lnet/minecraft/client/renderer/culling/Frustum;)Lnet/minecraft/client/renderer/blockentity/state/BlockEntityRenderState;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/culling/Frustum;isVisible(Lnet/minecraft/world/phys/AABB;)Z"
        )
    )
    private boolean vs$isShipBlockEntityInFrustum(final Frustum instance, final AABB aabb,
        final Operation<Boolean> operation, @Local(argsOnly = true) final BlockEntity blockEntity) {

        if (operation.call(instance, aabb)) {
            return true;
        }

        final Level level = Minecraft.getInstance().level;
        if (level == null) {
            return false;
        }
        final Ship nullableShip = VSGameUtilsKt.getLoadedShipManagingPos(level, blockEntity.getBlockPos());
        if (nullableShip instanceof final ClientShip ship) {
            // Transform all eight corners and take their bounds - the ship transform includes
            // rotation, so transforming only min/max would not give a correct enclosing box.
            final Matrix4dc s2w = ship.getRenderTransform().getShipToWorld();
            double minX = Double.POSITIVE_INFINITY;
            double minY = Double.POSITIVE_INFINITY;
            double minZ = Double.POSITIVE_INFINITY;
            double maxX = Double.NEGATIVE_INFINITY;
            double maxY = Double.NEGATIVE_INFINITY;
            double maxZ = Double.NEGATIVE_INFINITY;
            for (int corner = 0; corner < 8; corner++) {
                final Vector3d p = s2w.transformPosition(new Vector3d(
                    (corner & 1) == 0 ? aabb.minX : aabb.maxX,
                    (corner & 2) == 0 ? aabb.minY : aabb.maxY,
                    (corner & 4) == 0 ? aabb.minZ : aabb.maxZ
                ));
                minX = Math.min(minX, p.x);
                minY = Math.min(minY, p.y);
                minZ = Math.min(minZ, p.z);
                maxX = Math.max(maxX, p.x);
                maxY = Math.max(maxY, p.y);
                maxZ = Math.max(maxZ, p.z);
            }
            return operation.call(instance, new AABB(minX, minY, minZ, maxX, maxY, maxZ));
        }

        return false;
    }

    @WrapOperation(
        method = "tryExtractRenderState(Lnet/minecraft/world/level/block/entity/BlockEntity;FLnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;Lnet/minecraft/client/renderer/culling/Frustum;)Lnet/minecraft/client/renderer/blockentity/state/BlockEntityRenderState;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderer;shouldRender(Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/phys/Vec3;)Z"
        )
    )
    private <E extends BlockEntity> boolean vs$isBlockEntityInRenderRange(
        final BlockEntityRenderer<E, ?> instance, final E blockEntity, final Vec3 cameraPos,
        final Operation<Boolean> operation) {

        if (operation.call(instance, blockEntity, cameraPos)) {
            return true;
        }

        // If by default was false, then check if this BlockEntity belongs to a ship
        final Level level = Minecraft.getInstance().level;
        if (level == null) {
            return false;
        }
        final BlockPos bePos = blockEntity.getBlockPos();
        final Ship nullableShip = VSGameUtilsKt.getLoadedShipManagingPos(level, bePos);
        if (nullableShip instanceof final ClientShip ship) {
            final Matrix4dc m = ship.getRenderTransform().getShipToWorld();

            return new Vec3(
                m.m00() * bePos.getX() + m.m10() * bePos.getY() + m.m20() * bePos.getZ() + m.m30(),
                m.m01() * bePos.getX() + m.m11() * bePos.getY() + m.m21() * bePos.getZ() + m.m31(),
                m.m02() * bePos.getX() + m.m12() * bePos.getY() + m.m22() * bePos.getZ() + m.m32()
            ).closerThan(cameraPos, instance.getViewDistance());
        }

        return false;
    }
}
