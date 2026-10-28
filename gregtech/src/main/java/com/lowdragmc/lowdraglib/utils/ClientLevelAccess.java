package com.lowdragmc.lowdraglib.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Isolates client-only Minecraft references so common classes do not load client classes on a dedicated server. */
public final class ClientLevelAccess {

    private ClientLevelAccess() {}

    public static Level level() {
        return Minecraft.getInstance().level;
    }

    public static Player player() {
        return Minecraft.getInstance().player;
    }
}
