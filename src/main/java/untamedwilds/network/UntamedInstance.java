package untamedwilds.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import untamedwilds.UntamedWilds;

public class UntamedInstance {

    public static void registerMessages(RegisterPayloadHandlersEvent event) {
        UntamedWilds.LOGGER.info("Registering Packets!");
        final PayloadRegistrar registrar = event.registrar(UntamedWilds.MOD_ID);
        
        registrar.playToClient(
            SyncTextureData.TYPE,
            SyncTextureData.STREAM_CODEC,
            SyncTextureData::handleClient
        );
        registrar.playToClient(
            AnimationMessage.TYPE,
            AnimationMessage.STREAM_CODEC,
            AnimationMessage::handleClient
        );
    }

    public static void sendToClient(net.minecraft.network.protocol.common.custom.CustomPacketPayload packet, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, packet);
    }
}
