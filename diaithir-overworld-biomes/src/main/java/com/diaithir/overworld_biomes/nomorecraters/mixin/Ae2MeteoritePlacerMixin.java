package com.diaithir.overworld_biomes.nomorecraters.mixin;

import com.diaithir.overworld_biomes.nomorecraters.MeteorHook;
import com.diaithir.overworld_biomes.nomorecraters.MeteorPlacement;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Soft-target mixin on AE2's meteorite placer. The target is referenced by string so the class loads
 * inertly when AE2 is absent ({@code remap=false}, {@code require=0} via the config defaultRequire).
 */
@Mixin(targets = "appeng.worldgen.meteorite.MeteoritePlacer", remap = false)
public abstract class Ae2MeteoritePlacerMixin {
    @Inject(method = "place", at = @At("HEAD"), cancellable = true, require = 0)
    private void diaithir$bury(WorldGenLevel level, BlockPos pos, int radius, CallbackInfo ci) {
        MeteorPlacement.Result r = MeteorHook.apply(level, pos, radius);
        if (r != null && r.cancelled()) {
            ci.cancel();
        }
    }
}
