package org.valkyrienskies.mod.mixin.feature.world_weather;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.CompatUtil;

/**
 * Makes rain land on ships instead of falling through them.
 *
 * <p>The weather tick picks a random column near the camera, asks for its surface height and spawns
 * a splash particle there. Ships live in shipyard space and are invisible to the world heightmap, so
 * rain fell straight through a deck and splashed on whatever was below.
 *
 * <p>26.2 notes: {@code LevelRenderer.tickRain(Camera)} is gone - this whole routine moved to
 * {@link ClientLevel#tickWeatherEffects()}, and the surface lookups that used to be interface calls
 * on {@code LevelReader} are now virtual calls on {@code ClientLevel} itself, so the wrapped
 * descriptors name {@code ClientLevel}. The render-side counterpart moved too; see
 * {@link MixinWeatherEffectRenderer}.
 */
@Mixin(ClientLevel.class)
public class MixinClientLevel {

    @WrapOperation(
        method = "tickWeatherEffects()V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/ClientLevel;getHeightmapPos(Lnet/minecraft/world/level/levelgen/Heightmap$Types;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/BlockPos;"
        )
    )
    private BlockPos vs$includeShipsInWeatherTickOcclusion(
        final ClientLevel instance, final Types types, final BlockPos pos, final Operation<BlockPos> original,
        @Share("weatherSurfaceLookupPos") final LocalRef<BlockPos> weatherSurfaceLookupPos
    ) {
        weatherSurfaceLookupPos.set(null);
        final Level level = instance;
        final BlockPos vanillaHeight = original.call(instance, types, pos);
        final BlockPos worldHeight = BlockPos.containing(
            // 26.2 removed BlockPos.getCenter() in favour of the Vec3 factory.
            CompatUtil.INSTANCE.toSameSpaceAs(level, Vec3.atCenterOf(vanillaHeight), (Ship) null, null)
        );
        final BlockHitResult shipSurfaceHit =
            CompatUtil.INSTANCE.getShipHeightmapHitAboveWorldHeight(level, types, worldHeight);
        weatherSurfaceLookupPos.set(shipSurfaceHit == null ? null : shipSurfaceHit.getBlockPos().immutable());
        return shipSurfaceHit == null
            ? worldHeight
            : new BlockPos(worldHeight.getX(),
                Mth.floor(Math.nextDown(shipSurfaceHit.getLocation().y)) + 1, worldHeight.getZ());
    }

    @WrapOperation(
        method = "tickWeatherEffects()V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/ClientLevel;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"
        )
    )
    private BlockState vs$includeShipsInWeatherTickSurfaceState(
        final ClientLevel instance, final BlockPos pos, final Operation<BlockState> original,
        @Share("weatherSurfaceLookupPos") final LocalRef<BlockPos> weatherSurfaceLookupPos
    ) {
        final BlockPos lookupPos = weatherSurfaceLookupPos.get();
        return original.call(instance, lookupPos != null ? lookupPos : pos);
    }

    @WrapOperation(
        method = "tickWeatherEffects()V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/ClientLevel;getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;"
        )
    )
    private FluidState vs$includeShipsInWeatherTickSurfaceFluid(
        final ClientLevel instance, final BlockPos pos, final Operation<FluidState> original,
        @Share("weatherSurfaceLookupPos") final LocalRef<BlockPos> weatherSurfaceLookupPos
    ) {
        final BlockPos lookupPos = weatherSurfaceLookupPos.get();
        return original.call(instance, lookupPos != null ? lookupPos : pos);
    }

    @WrapOperation(
        method = "tickWeatherEffects()V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;"
        )
    )
    private VoxelShape vs$includeShipsInWeatherTickCollisionShape(
        final BlockState state, final BlockGetter levelReader, final BlockPos pos,
        final Operation<VoxelShape> original,
        @Share("weatherSurfaceLookupPos") final LocalRef<BlockPos> weatherSurfaceLookupPos
    ) {
        final BlockPos lookupPos = weatherSurfaceLookupPos.get();
        // This fixes ship surface lookup, but vanilla still interprets the returned shape in
        // world-axis block space. Solutions welcome?
        return original.call(state, levelReader, lookupPos != null ? lookupPos : pos);
    }
}
