package com.gregtechceu.gtceu.compat;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.entity.player.Player;

/** 26.1 replaced numeric permission levels with permission sets. */
public final class PermCompat {

    private PermCompat() {}

    public static boolean hasPermission(CommandSourceStack source, int level) {
        return source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(level)));
    }

    public static boolean hasPermission(CommandSourceStack source, net.minecraft.server.permissions.PermissionCheck check) {
        return check.check(source.permissions());
    }

    public static boolean hasPermissions(Player player, int level) {
        return player.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(level)));
    }

    public static boolean hasPermission(Player player, int level) {
        return hasPermissions(player, level);
    }
}
