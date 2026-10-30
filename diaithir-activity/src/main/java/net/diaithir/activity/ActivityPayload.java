package net.diaithir.activity;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Server to client: what one player is doing. An empty id means nothing to show. */
public record ActivityPayload(int entityId, String activity) implements CustomPacketPayload {
    public static final Type<ActivityPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(DiaithirActivity.ID, "state"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ActivityPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, ActivityPayload::entityId,
        ByteBufCodecs.stringUtf8(64), ActivityPayload::activity,
        ActivityPayload::new);

    @Override
    public Type<ActivityPayload> type() {
        return TYPE;
    }
}
