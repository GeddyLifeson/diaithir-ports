package org.valkyrienskies.mod.forge.mixin.feature.ship_blockentity_frustum;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

/**
 * Replaces the old ClientHooks.isBlockEntityRendererVisible mixin: in 26.1 the frustum check for block entities
 * happens in BlockEntityRenderDispatcher.tryExtractRenderState. Block entities that belong to a ship are checked
 * against the frustum using their render bounding box transformed into world space.
 */
@Mixin(BlockEntityRenderDispatcher.class)
public class MixinBlockEntityRenderDispatcher {

    @WrapOperation(
        method = "tryExtractRenderState(Lnet/minecraft/world/level/block/entity/BlockEntity;FLnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;Lnet/minecraft/client/renderer/culling/Frustum;)Lnet/minecraft/client/renderer/blockentity/state/BlockEntityRenderState;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/culling/Frustum;isVisible(Lnet/minecraft/world/phys/AABB;)Z"
        )
    )
    private boolean vs$shipBlockEntityVisible(final Frustum frustum, final AABB renderAABB,
        final Operation<Boolean> original, @Local(argsOnly = true) final BlockEntity blockEntity) {
        if (original.call(frustum, renderAABB)) {
            return true;
        }
        if (blockEntity.getLevel() == null) {
            return false;
        }
        final Ship ship = VSGameUtilsKt.getShipManagingPos(blockEntity.getLevel(), blockEntity.getBlockPos());
        if (!(ship instanceof ClientShip clientShip)) {
            return false;
        }
        return original.call(frustum, VSGameUtilsKt.transformRenderAABBToWorld(clientShip, renderAABB));
    }
}
