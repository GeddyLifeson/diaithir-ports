package org.valkyrienskies.mod.common.util

import net.minecraft.commands.CommandSourceStack
import net.minecraft.server.permissions.PermissionSet
import net.minecraft.server.permissions.Permissions
import net.minecraft.world.entity.player.Player

/**
 * 26.2 removed the integer permission-level API (`CommandSourceStack.hasPermission(int)` and
 * `Player.hasPermissions(int)`) in favour of named [net.minecraft.server.permissions.Permission]
 * objects checked against a [PermissionSet]. These bridges preserve VS2's existing int-level call
 * sites: level 0 means "everyone", levels 1-4 map to the vanilla command permission tiers.
 */
private fun PermissionSet.hasCommandLevel(level: Int): Boolean = when {
    level <= 0 -> true
    level == 1 -> hasPermission(Permissions.COMMANDS_MODERATOR)
    level == 2 -> hasPermission(Permissions.COMMANDS_GAMEMASTER)
    level == 3 -> hasPermission(Permissions.COMMANDS_ADMIN)
    else -> hasPermission(Permissions.COMMANDS_OWNER)
}

fun CommandSourceStack.hasPermission(level: Int): Boolean = permissions().hasCommandLevel(level)

fun Player.hasPermissions(level: Int): Boolean = permissions().hasCommandLevel(level)
