// Ported from No Chat Reports (WTFPL) https://github.com/Aizistral-Studios/No-Chat-Reports
// Modifications: reads feature client config.
package net.diaithir.chatclient.nochatreports.mixins.client;

import net.diaithir.chatclient.nochatreports.config.NCRClientConfig;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GuiMessageTag.class)
public class MixinGuiMessageTag {

    @Inject(method = {"system", "systemSinglePlayer"}, at = @At("HEAD"), cancellable = true)
    private static void ncr$onSystem(CallbackInfoReturnable<GuiMessageTag> info) {
        if (NCRClientConfig.SPEC.isLoaded() && NCRClientConfig.HIDE_SYSTEM_INDICATORS.get()) {
            info.setReturnValue(null);
        }
    }
}
