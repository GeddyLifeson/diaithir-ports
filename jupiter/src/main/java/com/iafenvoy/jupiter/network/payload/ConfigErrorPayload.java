package com.iafenvoy.jupiter.network.payload;

//? >=1.20.5 {

import com.iafenvoy.jupiter.Jupiter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public record ConfigErrorPayload() implements CustomPacketPayload {
    public static final Type<ConfigErrorPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Jupiter.MOD_ID, "config_error"));
    public static final StreamCodec<FriendlyByteBuf, ConfigErrorPayload> CODEC = StreamCodec.unit(new ConfigErrorPayload());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
