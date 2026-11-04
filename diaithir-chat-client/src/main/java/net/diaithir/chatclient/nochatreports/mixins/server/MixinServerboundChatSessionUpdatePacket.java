// Ported from No Chat Reports (WTFPL) https://github.com/Aizistral-Studios/No-Chat-Reports
// Modifications: reads feature common config.
package net.diaithir.chatclient.nochatreports.mixins.server;

import net.diaithir.chatclient.nochatreports.config.NCRCommonConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundChatSessionUpdatePacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerboundChatSessionUpdatePacket.class)
public class MixinServerboundChatSessionUpdatePacket {

    @Inject(method = "handle", at = @At("HEAD"), cancellable = true)
    private void ncr$onHandle(ServerGamePacketListener listener, CallbackInfo info) {
        ServerGamePacketListenerImpl impl = (ServerGamePacketListenerImpl) listener;
        if (!impl.getPlayer().level().getServer().isSingleplayerOwner(impl.getPlayer().nameAndId())
                && NCRCommonConfig.SPEC.isLoaded() && NCRCommonConfig.DEMAND_ON_CLIENT.get()) {
            impl.disconnect(Component.literal(NCRCommonConfig.DEMAND_ON_CLIENT_MESSAGE.get()));
        }
        info.cancel();
    }
}
