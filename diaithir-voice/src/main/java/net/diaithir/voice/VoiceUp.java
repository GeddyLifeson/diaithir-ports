package net.diaithir.voice;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** One encoded frame from a speaking client to the server. */
public record VoiceUp(short seq, byte[] data) implements CustomPacketPayload {
    public static final int MAX_BYTES = AdpcmCodec.encodedSize(AdpcmCodec.FRAME_SAMPLES * 2);
    public static final Type<VoiceUp> TYPE = new Type<>(Identifier.fromNamespaceAndPath(DiaithirVoice.ID, "up"));
    public static final StreamCodec<RegistryFriendlyByteBuf, VoiceUp> CODEC = StreamCodec.of(
        (buf, v) -> { buf.writeShort(v.seq); buf.writeByteArray(v.data); },
        buf -> new VoiceUp(buf.readShort(), buf.readByteArray(MAX_BYTES)));

    @Override
    public Type<VoiceUp> type() {
        return TYPE;
    }
}
