// Ported from No Chat Reports (WTFPL) https://github.com/Aizistral-Studios/No-Chat-Reports
// Modifications: none.
package net.diaithir.chatclient.nochatreports.mixins.server;

import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.network.protocol.game.ServerboundChatCommandSignedPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerboundChatCommandSignedPacket.class)
public class MixinServerboundChatCommandSignedPacket {

    @Inject(method = "argumentSignatures", at = @At("RETURN"), cancellable = true)
    private void ncr$onGetSignatures(CallbackInfoReturnable<ArgumentSignatures> info) {
        info.setReturnValue(ArgumentSignatures.EMPTY);
    }
}
