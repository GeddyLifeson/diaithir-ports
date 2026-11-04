// Ported from Disable Accessibility Screen (MIT) https://github.com/DaFuqs/DisableAccessibilityScreen (26.2-neoforge)
// Modifications: package moved into diaithir-clientcore.
package net.diaithir.client.disableaccessibility.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow @Final public Options options;

    @Inject(method = "onResourceLoadFinished(Lnet/minecraft/client/Minecraft$GameLoadCookie;)V", at = @At("HEAD"))
    private void diaithir$disableAccessibilityScreen(@org.spongepowered.asm.mixin.injection.Coerce Object loadCookie, CallbackInfo ci) {
        this.options.onboardAccessibility = false;
    }
}
