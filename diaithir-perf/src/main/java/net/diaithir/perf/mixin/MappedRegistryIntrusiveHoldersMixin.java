package net.diaithir.perf.mixin;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Some boots died at registry freeze with "Some intrusive holders were not registered: []". Mods build Items and
 * Blocks on parallel modloading-worker threads, each construction adds to this registry's IdentityHashMap of
 * unregistered intrusive holders, and the map is not thread safe. A lost update leaves isEmpty() false while the
 * values are gone, so the freeze check throws with an empty list. A synchronized wrapper makes the puts and removes
 * atomic; the next boot passed because the race did not repeat.
 */
@Mixin(MappedRegistry.class)
abstract class MappedRegistryIntrusiveHoldersMixin<T> {
    @Unique private static final Class<?> diaithirperf$SYNCHRONIZED =
            Collections.synchronizedMap(new IdentityHashMap<>()).getClass();

    @Shadow private Map<T, Holder.Reference<T>> unregisteredIntrusiveHolders;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void diaithirperf$synchronizeIntrusiveHolders(CallbackInfo ci) {
        Map<T, Holder.Reference<T>> holders = unregisteredIntrusiveHolders;
        if (holders != null && holders.getClass() != diaithirperf$SYNCHRONIZED) {
            unregisteredIntrusiveHolders = Collections.synchronizedMap(holders);
        }
    }
}
