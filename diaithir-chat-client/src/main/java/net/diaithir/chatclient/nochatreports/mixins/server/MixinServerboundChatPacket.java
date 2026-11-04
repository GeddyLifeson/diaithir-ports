// Ported from No Chat Reports (WTFPL) https://github.com/Aizistral-Studios/No-Chat-Reports
// Modifications: none.
package net.diaithir.chatclient.nochatreports.mixins.server;

import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerboundChatPacket.class)
public class MixinServerboundChatPacket {

    @Inject(method = "signature", at = @At("RETURN"), cancellable = true)
    private void ncr$onGetSignature(CallbackInfoReturnable<MessageSignature> info) {
        info.setReturnValue(null);
    }
}
