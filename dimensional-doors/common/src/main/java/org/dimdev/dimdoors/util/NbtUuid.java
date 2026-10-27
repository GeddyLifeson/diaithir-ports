package org.dimdev.dimdoors.util;

import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

/** Replacements for the removed CompoundTag#putUUID / getUUID. */
public final class NbtUuid {
    private NbtUuid() {
    }

    public static void put(CompoundTag tag, String key, UUID uuid) {
        tag.store(key, UUIDUtil.CODEC, uuid);
    }

    public static UUID get(CompoundTag tag, String key) {
        return tag.read(key, UUIDUtil.CODEC).orElseThrow();
    }
}
