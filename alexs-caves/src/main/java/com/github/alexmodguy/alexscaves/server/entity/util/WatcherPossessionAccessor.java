package com.github.alexmodguy.alexscaves.server.entity.util;

import net.minecraft.world.entity.Entity;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Possession flag on entities. The original added this interface to Player with a mixin; here it is a UUID set. */
public final class WatcherPossessionAccessor {
    private static final Set<UUID> POSSESSED = ConcurrentHashMap.newKeySet();

    private WatcherPossessionAccessor() {
    }

    public static void setPossessedByWatcher(Entity entity, boolean possessed) {
        if (possessed) POSSESSED.add(entity.getUUID());
        else POSSESSED.remove(entity.getUUID());
    }

    public static boolean isPossessedByWatcher(Entity entity) {
        return POSSESSED.contains(entity.getUUID());
    }
}
