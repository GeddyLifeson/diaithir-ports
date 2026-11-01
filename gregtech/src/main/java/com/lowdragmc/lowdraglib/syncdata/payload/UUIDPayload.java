package com.lowdragmc.lowdraglib.syncdata.payload;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;

import javax.annotation.Nullable;

import java.util.UUID;

public class UUIDPayload extends ObjectTypedPayload<UUID> {
    @Override
    public void writePayload(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(payload);
    }

    @Override
    public void readPayload(RegistryFriendlyByteBuf buf) {
        payload = buf.readUUID();
    }

    @Override
    public @Nullable Tag serializeNBT(HolderLookup.Provider provider) { return net.minecraft.nbt.StringTag.valueOf(payload.toString()); }

    @Override
    public void deserializeNBT(Tag tag, HolderLookup.Provider provider) {
        if (tag instanceof net.minecraft.nbt.IntArrayTag ia && ia.size() == 4) {
            payload = net.minecraft.core.UUIDUtil.uuidFromIntArray(ia.getAsIntArray());
        } else {
            payload = java.util.UUID.fromString(tag.asString().orElse("00000000-0000-0000-0000-000000000000"));
        }
    }
}
