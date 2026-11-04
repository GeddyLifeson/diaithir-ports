package net.diaithir.claims;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Client asks who holds a chunk. */
public record ClaimQuery(int cx, int cz) implements CustomPacketPayload {
    public static final Type<ClaimQuery> TYPE = new Type<>(Identifier.fromNamespaceAndPath(DiaithirClaims.ID, "query"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClaimQuery> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, ClaimQuery::cx, ByteBufCodecs.VAR_INT, ClaimQuery::cz, ClaimQuery::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
