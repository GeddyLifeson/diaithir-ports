package org.valkyrienskies.mod.mixin.feature.world_border;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.mod.mixinducks.world.OfLevel;

// See MixinLevel in this package for why this is split into a separate client-only mixin.
@Mixin(ClientLevel.class)
public class MixinClientLevel {

    @Inject(
        at = @At("RETURN"),
        method = "getWorldBorder"
    )
    private void setWorldBorderLevel(final CallbackInfoReturnable<WorldBorder> cir) {
        ((OfLevel) cir.getReturnValue()).setLevel(Level.class.cast(this));
    }

}
