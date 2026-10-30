package net.diaithir.steamrails.mixin;

import com.zurrtum.create.content.trains.entity.Train;
import net.diaithir.steamrails.TrainPower;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Lets pressure replace Create's fuel timer as the thing that sets a steam train's speed. Only trains with a working steam carriage are touched. */
@Mixin(Train.class)
abstract class TrainMixin {
    @Inject(method = "maxSpeed", at = @At("RETURN"), cancellable = true)
    private void diaithirsteamrails$speed(CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(TrainPower.maxSpeed((Train) (Object) this, cir.getReturnValue()));
    }

    @Inject(method = "maxTurnSpeed", at = @At("RETURN"), cancellable = true)
    private void diaithirsteamrails$turn(CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(TrainPower.maxTurnSpeed((Train) (Object) this, cir.getReturnValue()));
    }

    @Inject(method = "acceleration", at = @At("RETURN"), cancellable = true)
    private void diaithirsteamrails$accel(CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(TrainPower.acceleration((Train) (Object) this, cir.getReturnValue()));
    }
}
