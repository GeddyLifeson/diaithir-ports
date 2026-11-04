// Ported from No Chat Reports (WTFPL) https://github.com/Aizistral-Studios/No-Chat-Reports
// Modifications: uses SafetyState.
package net.diaithir.chatclient.nochatreports.mixins.client;

import com.mojang.realmsclient.dto.RealmsServer;
import net.diaithir.chatclient.nochatreports.client.NCRClient;
import net.diaithir.chatclient.nochatreports.client.SafetyState;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.realms.RealmsConnect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RealmsConnect.class)
public class MixinRealmsConnect {

    @Inject(method = "connect", at = @At("HEAD"))
    private void ncr$onConnect(RealmsServer realmsServer, ServerAddress serverAddress, CallbackInfo info) {
        if (!NCRClient.enabled()) {
            return;
        }
        SafetyState.reset();
        SafetyState.setOnRealms(true);
        SafetyState.setAllowChatSigning(true);
    }
}
