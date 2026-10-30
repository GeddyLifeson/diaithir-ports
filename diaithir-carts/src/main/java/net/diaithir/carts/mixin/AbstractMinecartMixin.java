package net.diaithir.carts.mixin;

import net.diaithir.carts.CartLogic;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Vanilla caps every cart at 0.4 blocks per tick (a game rule in the new movement). This lifts the cap to the rail the cart is riding when that rail is one of ours. */
@Mixin(AbstractMinecart.class)
abstract class AbstractMinecartMixin {
    @Inject(method = "getMaxSpeed", at = @At("RETURN"), cancellable = true)
    private void diaithircarts$railCap(ServerLevel level, CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(CartLogic.cap((AbstractMinecart) (Object) this, cir.getReturnValue()));
    }
}
