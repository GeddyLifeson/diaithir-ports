// Ported from No Chat Reports (WTFPL) https://github.com/Aizistral-Studios/No-Chat-Reports
// Modifications: reads feature common config.
package net.diaithir.chatclient.nochatreports.mixins.server;

import io.netty.channel.ChannelFutureListener;
import net.diaithir.chatclient.nochatreports.config.NCRCommonConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class MixinServerCommonPacketListenerImpl {

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void ncr$onSend(Packet<?> packet, CallbackInfo info) {
        Object self = this;
        if (self instanceof ServerGamePacketListenerImpl listener && NCRCommonConfig.SPEC.isLoaded()
                && NCRCommonConfig.CONVERT_TO_GAME_MESSAGE.get() && packet instanceof ClientboundPlayerChatPacket chat) {
            Packet<?> converted = new ClientboundSystemChatPacket(chat.chatType().decorate(
                    chat.unsignedContent() != null ? chat.unsignedContent()
                            : Component.literal(chat.body().content())), false);
            info.cancel();
            listener.send(converted);
        }
    }

    @Inject(method = "Lnet/minecraft/server/network/ServerCommonPacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;)V",
            at = @At("HEAD"), cancellable = true)
    private void ncr$onSendListener(Packet<?> packet, @Nullable ChannelFutureListener channelFutureListener, CallbackInfo info) {
        Object self = this;
        if (self instanceof ServerGamePacketListenerImpl listener && NCRCommonConfig.SPEC.isLoaded()
                && NCRCommonConfig.CONVERT_TO_GAME_MESSAGE.get()
                && packet instanceof ClientboundPlayerChatPacket chat && channelFutureListener != null) {
            info.cancel();
            listener.send(chat);
        }
    }
}
