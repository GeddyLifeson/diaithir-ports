package org.valkyrienskies.mod.mixin.feature.tick_ship_chunks;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

/**
 * These methods fix random ticking on ship chunks
 */
@Mixin(ChunkMap.class)
public abstract class MixinChunkMap {

    @Shadow
    @Final
    ServerLevel level;

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(
        method = "playerIsCloseEnoughForSpawning",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ChunkMap;euclideanDistanceSquared(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/phys/Vec3;)D")
    )
    private double vs$distanceInclShips(final ChunkPos chunkPos, final net.minecraft.world.phys.Vec3 pos,
        final com.llamalad7.mixinextras.injector.wrapoperation.Operation<Double> original) {
        final double d = chunkPos.x() * 16 + 8;
        final double e = chunkPos.z() * 16 + 8;
        return VSGameUtilsKt.squaredDistanceBetweenInclShips(level, pos.x, 0, pos.z, d, 0, e);
    }

    @Inject(method = "anyPlayerCloseEnoughForSpawning", at = @At("RETURN"), cancellable = true)
    void noPlayersCloseForSpawning(final ChunkPos chunkPos, final CallbackInfoReturnable<Boolean> cir) {
        if (VSGameUtilsKt.isChunkInShipyard(level, chunkPos.x(), chunkPos.z())) {
            if (!cir.getReturnValue()) {
                final ServerShip ship = VSGameUtilsKt.getShipObjectWorld(level).getLoadedShips()
                    .getByChunkPos(chunkPos.x(), chunkPos.z(), VSGameUtilsKt.getDimensionId(level));
                if (ship != null) {
                    cir.setReturnValue(true);
                }
            }
        }
    }

}
