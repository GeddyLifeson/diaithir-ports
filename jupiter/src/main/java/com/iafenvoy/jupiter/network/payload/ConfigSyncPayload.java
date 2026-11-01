package com.iafenvoy.jupiter.network.payload;

//? >=1.20.5 {

import com.iafenvoy.jupiter.Jupiter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public record ConfigSyncPayload(Identifier id, boolean allow,
                                CompoundTag compound) implements CustomPacketPayload {
    public static final Type<ConfigSyncPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Jupiter.MOD_ID, "config_sync"));
    public static final StreamCodec<FriendlyByteBuf, ConfigSyncPayload> CODEC = StreamCodec.of((buf, value) -> {
        buf.writeIdentifier(value.id);
        buf.writeBoolean(value.allow);
        buf.writeNbt(value.compound);
    }, buf -> new ConfigSyncPayload(buf.readIdentifier(), buf.readBoolean(), buf.readNbt()));

    public ConfigSyncPayload(Identifier id, CompoundTag compound) {
        this(id, true, compound);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
