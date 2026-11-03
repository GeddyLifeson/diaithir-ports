package net.diaithir.claims;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** The 21 by 21 chunks around a chunk: 0 free, 1 yours, 2 someone else's. */
public record MapPayload(int cx, int cz, byte[] cells) implements CustomPacketPayload {
    public static final int SIZE = 21;
    public static final Type<MapPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(DiaithirClaims.ID, "map"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MapPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, MapPayload::cx, ByteBufCodecs.VAR_INT, MapPayload::cz, ByteBufCodecs.BYTE_ARRAY, MapPayload::cells, MapPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
