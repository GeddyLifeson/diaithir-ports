package net.diaithir.druidry.ars.common.network;

import net.diaithir.druidry.ars.ArsNouveau;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class PacketGenericClientMessage extends AbstractPacket{
    public static final Type<PacketGenericClientMessage> TYPE = new Type<>(ArsNouveau.prefix("generic_client_message"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketGenericClientMessage> CODEC = StreamCodec.ofMember(PacketGenericClientMessage::toBytes, PacketGenericClientMessage::new);
    public enum Action{
        JUMP_RING
    }

    Action action;

    public PacketGenericClientMessage(Action action){
        this.action = action;
    }
    //Decoder
    public PacketGenericClientMessage(RegistryFriendlyByteBuf buf) {
        this.action = Action.valueOf(buf.readUtf());
    }

    //Encoder
    public void toBytes(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(action.name());
    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, ServerPlayer player) {
        // Port note: JUMP_RING handling removed with the Curios-based JumpingRing.
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
