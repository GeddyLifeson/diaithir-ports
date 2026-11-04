// Ported from No Chat Reports (WTFPL) https://github.com/Aizistral-Studios/No-Chat-Reports
// Modifications: reads feature client config.
package net.diaithir.chatclient.nochatreports.mixins.client;

import net.diaithir.chatclient.nochatreports.config.NCRClientConfig;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ToastManager.class)
public class MixinToastManager {

    @Inject(method = "addToast", at = @At("HEAD"), cancellable = true)
    private void ncr$onAddToast(Toast toast, CallbackInfo info) {
        if (NCRClientConfig.SPEC.isLoaded() && NCRClientConfig.HIDE_WARNING_TOAST.get()
                && toast instanceof SystemToast sys
                && sys.getToken() == SystemToast.SystemToastId.UNSECURE_SERVER_WARNING) {
            info.cancel();
        }
    }
}
