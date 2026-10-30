package com.github.alexmodguy.alexscaves.server.misc;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * 26.1 split hurt/lookup APIs into server-only calls; 1.21 code freely called them from code that also runs on the
 * client. These helpers keep the call sites side-safe.
 */
public final class ACSide {
    private ACSide() {
    }

    /** hurtServer when on the logical server, false on the client. */
    public static boolean hurt(Entity target, DamageSource source, float amount) {
        if (target.level() instanceof ServerLevel sl) {
            return target.hurtServer(sl, source, amount);
        }
        return false;
    }

    /** Entity lookup by UUID on either side. */
    @Nullable
    public static Entity entity(Level level, @Nullable UUID id) {
        if (id == null) return null;
        if (level instanceof ServerLevel sl) return sl.getEntity(id);
        return clientEntity(level, id);
    }

    @Nullable
    private static Entity clientEntity(Level level, UUID id) {
        if (net.neoforged.fml.loading.FMLEnvironment.getDist().isClient() && level instanceof ClientLevel cl) {
            for (Entity e : cl.entitiesForRendering()) {
                if (e.getUUID().equals(id)) return e;
            }
        }
        return null;
    }
}
