package plus.dragons.createenchantmentindustry.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Registers this addon's content just before vanilla registries freeze, after Create's own registration. */
@Mixin(value = BuiltInRegistries.class, priority = 2000)
public class AddonRegistrationMixin {
    private static boolean ceiDone;

    @Inject(method = "freeze()V", at = @At("HEAD"))
    private static void cei$register(CallbackInfo ci) {
        if (!ceiDone) {
            ceiDone = true;
            new plus.dragons.createenchantmentindustry.common.CEICommon().onInitialize();
        }
    }
}
