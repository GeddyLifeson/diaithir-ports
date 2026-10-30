package com.github.alexthe666.citadel.server.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;

/** Citadel keeps a synced tag on living entities through a mixin; here it lives in NeoForge persistent data. */
public class CitadelEntityData {
    private static final String KEY = "CitadelTag";

    public static CompoundTag getOrCreateCitadelTag(LivingEntity entity) {
        CompoundTag data = entity.getPersistentData();
        if (!data.contains(KEY)) {
            data.put(KEY, new CompoundTag());
        }
        return data.getCompoundOrEmpty(KEY);
    }

    public static CompoundTag getCitadelTag(LivingEntity entity) {
        return entity.getPersistentData().getCompoundOrEmpty(KEY);
    }

    public static void setCitadelTag(LivingEntity entity, CompoundTag tag) {
        entity.getPersistentData().put(KEY, tag);
    }
}
