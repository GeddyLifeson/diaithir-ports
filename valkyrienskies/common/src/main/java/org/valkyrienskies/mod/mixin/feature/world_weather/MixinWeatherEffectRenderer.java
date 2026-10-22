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
import net.minecraft.client.renderer.WeatherEffectRenderer;
import org.spongepowered.asm.mixin.injection.At;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.CompatUtil;

/**
 * Makes rain land on ships, and stops rain and snow being drawn through a ship's deck.
 *
 * <p>Ships live in shipyard space and are invisible to the world heightmap, so splash particles
 * landed on whatever was below a deck and weather columns were drawn from below it. In 26.1 the
 * splash tick is {@link WeatherEffectRenderer#tickRainParticles} and the column heights are read in
 * {@link WeatherEffectRenderer#extractRenderState}.
 */
@Mixin(WeatherEffectRenderer.class)
public class MixinWeatherEffectRenderer {

    @WrapOperation(
        method = "extractRenderState",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;getHeight(Lnet/minecraft/world/level/levelgen/Heightmap$Types;II)I"
        )
    )
    private int vs$includeShipsInWeatherRenderOcclusion(
        final Level level, final Types types, final int x, final int z, final Operation<Integer> original
    ) {
        return CompatUtil.INSTANCE.getWorldHeightIncludingShips(level, types, x, z);
    }


    @WrapOperation(
        method = "tickRainParticles",
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
        method = "tickRainParticles",
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
        method = "tickRainParticles",
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
        method = "tickRainParticles",
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
