package org.valkyrienskies.mod.mixin.feature.world_weather;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.valkyrienskies.mod.common.CompatUtil;

/**
 * Stops rain and snow being drawn straight through a ship's deck.
 *
 * <p>Each weather column is drawn from the world surface height upwards; ships are not in the world
 * heightmap, so columns started below a deck and the rain visibly passed through it.
 *
 * <p>26.2 notes: {@code LevelRenderer.renderSnowAndRain} is gone. Weather now extracts its own
 * render state in {@link WeatherEffectRenderer#extractRenderState}, which is where the surface
 * height is read - the call is the same {@code getHeight(Heightmap$Types, int, int)}, just on
 * {@link ClientLevel} instead of {@code Level}. The tick-side counterpart is
 * {@link MixinClientLevel}.
 */
@Mixin(WeatherEffectRenderer.class)
public class MixinWeatherEffectRenderer {

    @WrapOperation(
        method = "extractRenderState(Lnet/minecraft/client/multiplayer/ClientLevel;FLnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/state/level/WeatherRenderState;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/ClientLevel;getHeight(Lnet/minecraft/world/level/levelgen/Heightmap$Types;II)I"
        )
    )
    private int vs$includeShipsInWeatherRenderOcclusion(
        final ClientLevel level, final Types types, final int x, final int z, final Operation<Integer> original
    ) {
        return CompatUtil.INSTANCE.getWorldHeightIncludingShips(level, types, x, z);
    }
}
