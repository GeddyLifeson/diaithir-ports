package net.diaithir.voice;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.UUID;

/** One encoded frame relayed to a listener, with the speaker's position and the gain the server worked out for the distance. */
public record VoiceDown(UUID speaker, short seq, int gain, float x, float y, float z, byte[] data) implements CustomPacketPayload {
    public static final Type<VoiceDown> TYPE = new Type<>(Identifier.fromNamespaceAndPath(DiaithirVoice.ID, "down"));
    public static final StreamCodec<RegistryFriendlyByteBuf, VoiceDown> CODEC = StreamCodec.of(
        (buf, v) -> {
            buf.writeUUID(v.speaker); buf.writeShort(v.seq); buf.writeByte(v.gain);
            buf.writeFloat(v.x); buf.writeFloat(v.y); buf.writeFloat(v.z); buf.writeByteArray(v.data);
        },
        buf -> new VoiceDown(buf.readUUID(), buf.readShort(), buf.readUnsignedByte(),
            buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readByteArray(VoiceUp.MAX_BYTES)));

    @Override
    public Type<VoiceDown> type() {
        return TYPE;
    }
}
