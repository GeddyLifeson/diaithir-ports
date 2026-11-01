package org.valkyrienskies.mod.mixin.feature.world_border;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.mod.mixinducks.world.OfLevel;

// 26.2: Level no longer owns a `worldBorder` field directly - ServerLevel.getWorldBorder() now
// lazily loads/caches a per-dimension WorldBorder via the SavedData system. Tag the returned
// border with its owning Level each call (idempotent - harmless to repeat once already tagged).
// See MixinClientLevel in this package for the ClientLevel-side equivalent (client-only class,
// needs to live in the "client" mixins list, not here).
@Mixin(ServerLevel.class)
public class MixinLevel {

    @Inject(
        at = @At("RETURN"),
        method = "getWorldBorder"
    )
    private void setWorldBorderLevel(final CallbackInfoReturnable<WorldBorder> cir) {
        ((OfLevel) cir.getReturnValue()).setLevel(Level.class.cast(this));
    }

}
