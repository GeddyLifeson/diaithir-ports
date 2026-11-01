package net.diaithir.branding.mixin;

import java.util.List;
import net.neoforged.neoforge.internal.BrandingControl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The title screen corner shows the pack, not the loader and mod count; NeoForge update notices are dropped. */
@Mixin(BrandingControl.class)
abstract class BrandingControlMixin {
    @Shadow
    private static List<String> brandings;
    @Shadow
    private static List<String> brandingsNoMC;
    @Shadow
    private static List<String> overCopyrightBrandings;

    @Inject(method = "computeBranding", at = @At("HEAD"), cancellable = true)
    private static void diaithir$branding(CallbackInfo ci) {
        brandings = List.of("Diaithir · Age I");
        brandingsNoMC = brandings;
        ci.cancel();
    }

    @Inject(method = "computeOverCopyrightBrandings", at = @At("HEAD"), cancellable = true)
    private static void diaithir$noStatusLine(CallbackInfo ci) {
        overCopyrightBrandings = List.of();
        ci.cancel();
    }
}
