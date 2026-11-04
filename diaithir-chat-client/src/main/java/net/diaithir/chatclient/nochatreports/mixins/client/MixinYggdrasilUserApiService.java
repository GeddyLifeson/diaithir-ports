// Ported from No Chat Reports (WTFPL) https://github.com/Aizistral-Studios/No-Chat-Reports
// Modifications: reads feature client config.
package net.diaithir.chatclient.nochatreports.mixins.client;

import com.mojang.authlib.minecraft.TelemetrySession;
import com.mojang.authlib.yggdrasil.YggdrasilUserApiService;
import java.util.concurrent.Executor;
import net.diaithir.chatclient.nochatreports.config.NCRClientConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = YggdrasilUserApiService.class, remap = false)
public class MixinYggdrasilUserApiService {

    @Inject(method = "newTelemetrySession", at = @At("HEAD"), cancellable = true)
    private void ncr$onCreateTelemetrySession(Executor executor, CallbackInfoReturnable<TelemetrySession> info) {
        if (NCRClientConfig.SPEC.isLoaded() && NCRClientConfig.DISABLE_TELEMETRY.get()) {
            info.setReturnValue(TelemetrySession.DISABLED);
        }
    }
}
