// Ported from No Chat Reports (WTFPL) https://github.com/Aizistral-Studios/No-Chat-Reports
// Modifications: none.
package net.diaithir.chatclient.nochatreports.mixins.server;

import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DedicatedServer.class)
public class MixinDedicatedServer {

    @Inject(method = "enforceSecureProfile", at = @At("RETURN"), cancellable = true)
    private void ncr$onEnforceSecureProfile(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
