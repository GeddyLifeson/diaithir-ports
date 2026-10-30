package com.iafenvoy.jupiter.network.payload;

//? >=1.20.5 {

import com.iafenvoy.jupiter.Jupiter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public record ConfigRequestPayload(Identifier id) implements CustomPacketPayload {
    public static final Type<ConfigRequestPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Jupiter.MOD_ID, "config_request"));
    public static final StreamCodec<FriendlyByteBuf, ConfigRequestPayload> CODEC = StreamCodec.of((buf, value) -> buf.writeIdentifier(value.id), buf -> new ConfigRequestPayload(buf.readIdentifier()));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
