package com.github.alexthe666.citadel.server.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;

/** Citadel keeps a synced tag on living entities through a mixin; here it lives in NeoForge persistent data. */
public class CitadelEntityData {
    private static final String KEY = "CitadelTag";

    /** Players use the respawn-persistent sub-tag so the data survives death and cloning; other entities use plain persistent data. */
    private static CompoundTag root(LivingEntity entity) {
        CompoundTag data = entity.getPersistentData();
        if (entity instanceof net.minecraft.world.entity.player.Player) {
            if (!data.contains(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG)) {
                data.put(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG, new CompoundTag());
            }
            return data.getCompoundOrEmpty(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG);
        }
        return data;
    }

    public static CompoundTag getOrCreateCitadelTag(LivingEntity entity) {
        CompoundTag data = root(entity);
        if (!data.contains(KEY)) {
            data.put(KEY, new CompoundTag());
        }
        return data.getCompoundOrEmpty(KEY);
    }

    public static CompoundTag getCitadelTag(LivingEntity entity) {
        return root(entity).getCompoundOrEmpty(KEY);
    }

    public static void setCitadelTag(LivingEntity entity, CompoundTag tag) {
        root(entity).put(KEY, tag);
    }
}
