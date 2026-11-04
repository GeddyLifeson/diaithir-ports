// Ported from No Chat Reports (WTFPL) https://github.com/Aizistral-Studios/No-Chat-Reports
// Modifications: renamed class (no '$' in file name); targets the same anonymous inner class; updates SafetyState.
package net.diaithir.chatclient.nochatreports.mixins.client;

import java.net.InetSocketAddress;
import net.diaithir.chatclient.nochatreports.client.SafetyState;
import net.diaithir.chatclient.nochatreports.core.ServerDataExtension;
import net.diaithir.chatclient.nochatreports.core.ServerStatusCache;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerStatusPinger;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.network.EventLoopGroupHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/client/multiplayer/ServerStatusPinger$1")
public abstract class MixinServerStatusPingerAnon1 {

    @Unique
    private ServerDataExtension ncr$serverData;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void ncr$captureServerData(ServerStatusPinger pinger, Connection connection, ServerData serverData,
            Runnable r1, Runnable r2, InetSocketAddress address, ServerAddress serverAddress,
            EventLoopGroupHolder holder, CallbackInfo ci) {
        this.ncr$serverData = (ServerDataExtension) serverData;
    }

    @Inject(method = "handleStatusResponse(Lnet/minecraft/network/protocol/status/ClientboundStatusResponsePacket;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/status/ServerStatus;"
                    + "description()Lnet/minecraft/network/chat/Component;"))
    private void ncr$getNoChatReports(ClientboundStatusResponsePacket packet, CallbackInfo info) {
        ServerStatus status = packet.status();
        boolean preventsReports = ServerStatusCache.doesPreventReports();
        if (status.version().isPresent()) {
            ServerStatus.Version version = status.version().get();
            if (version.protocol() < 759 && version.protocol() > 0) {
                preventsReports = true;
            }
        }
        if (this.ncr$serverData == null) {
            return;
        }
        this.ncr$serverData.setPreventsChatReports(preventsReports);
        SafetyState.setServerPreventsReports(preventsReports);
    }
}
