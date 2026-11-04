package immersive_melodies.mixin;

import immersive_melodies.client.MelodyProgressManager;
import net.minecraft.world.entity.animal.allay.Allay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Allay.class)
public class AllayEntityMixin {
    @Inject(method = "isDancing()Z", at = @At("HEAD"), cancellable = true)
    void immersiveMelodiesIsDancing(CallbackInfoReturnable<Boolean> cir) {
        if (MelodyProgressManager.INSTANCE.isClose(((Allay) (Object) this).position(), 5.0f)) {
            cir.setReturnValue(true);
        }
    }
}
