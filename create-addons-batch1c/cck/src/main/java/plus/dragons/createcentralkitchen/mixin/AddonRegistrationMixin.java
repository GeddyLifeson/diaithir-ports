package plus.dragons.createcentralkitchen.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Registers this addon's content just before vanilla registries freeze, after Create's own registration. */
@Mixin(value = BuiltInRegistries.class, priority = 2000)
public class AddonRegistrationMixin {
    private static boolean cckDone;

    @Inject(method = "freeze()V", at = @At("HEAD"))
    private static void cck$register(CallbackInfo ci) {
        if (!cckDone) {
            cckDone = true;
            System.out.println("[addon-register] " + AddonRegistrationMixin.class.getName());
            plus.dragons.createcentralkitchen.common.CCKCommon.register();
        }
    }
}
