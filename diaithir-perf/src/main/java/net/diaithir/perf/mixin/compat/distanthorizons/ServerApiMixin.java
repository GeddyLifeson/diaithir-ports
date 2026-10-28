package net.diaithir.perf.mixin.compat.distanthorizons;

import net.diaithir.perf.DhIgnore;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Distant Horizons builds a full set of per-level pools (generation queue, progress updater, update and request
 * queues, chunk-save timer: about 15 threads) for every server level that loads, even for dimensions on its own
 * ignored-dimension list, which only stops rendering and chunk updates. This pack registers 72 Backrooms dimensions
 * at world load, so a single-player world carried over 1,100 threads. A level on the ignored list now gets no DH level.
 * Optional: a DH update that renames the method only turns this off.
 */
@Mixin(targets = "com.seibel.distanthorizons.core.api.internal.ServerApi", remap = false)
abstract class ServerApiMixin {
    @Inject(method = "serverLevelLoadEvent", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void diaithirperf$noPoolsForIgnoredLevels(@Coerce Object levelWrapper, CallbackInfo ci) {
        if (DhIgnore.ignored(levelWrapper)) ci.cancel();
    }
}
