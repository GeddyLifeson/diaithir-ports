package com.gregtechceu.gtceu.common.capability;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class WorldIDSaveData extends SavedData {

    private static WorldIDSaveData instance;
    private static final String DATA_NAME = "gtceu_world_id";

    private String worldID;

    @SuppressWarnings("unused")
    public WorldIDSaveData(ServerLevel level) {
        worldID = level.getServer().getWorldData().getLevelName() + "_" + UUID.randomUUID();
        this.setDirty();
    }

    public WorldIDSaveData(CompoundTag tag) {
        this.worldID = tag.getStringOr("id", "");
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) { return null; /* ported stub */ }

    public static void init(ServerLevel level) {  /* ported stub */ }

    public static String getWorldID() {
        return instance.worldID;
    }
}
