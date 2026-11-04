package net.diaithir.claims;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Server answers with the holder's name, empty when the chunk is free. */
public record ClaimInfo(int cx, int cz, String owner) implements CustomPacketPayload {
    public static final Type<ClaimInfo> TYPE = new Type<>(Identifier.fromNamespaceAndPath(DiaithirClaims.ID, "info"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClaimInfo> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, ClaimInfo::cx, ByteBufCodecs.VAR_INT, ClaimInfo::cz, ByteBufCodecs.STRING_UTF8, ClaimInfo::owner, ClaimInfo::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
