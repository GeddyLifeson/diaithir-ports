// Ported from No Chat Reports (WTFPL) https://github.com/Aizistral-Studios/No-Chat-Reports
// Modifications: none.
package net.diaithir.chatclient.nochatreports.mixins.server;

import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public class MixinPlayerList {

    @Inject(method = "verifyChatTrusted", at = @At("HEAD"), cancellable = true)
    private void ncr$onVerifyChatTrusted(PlayerChatMessage message, CallbackInfoReturnable<Boolean> info) {
        info.setReturnValue(true);
    }
}
