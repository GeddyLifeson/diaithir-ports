package org.valkyrienskies.mod.common.command

import net.minecraft.commands.CommandSourceStack
import net.minecraft.server.permissions.Permission
import net.minecraft.server.permissions.PermissionLevel
import net.minecraft.world.entity.player.Player

/**
 * 26.1 replaced numeric permission levels (`hasPermission(int)`) with [Permission] objects.
 * These helpers keep VS2's numeric config values (0-4) working.
 */
fun commandLevelPermission(level: Int): Permission = Permission.HasCommandLevel(PermissionLevel.byId(level))

fun CommandSourceStack.hasPermission(level: Int): Boolean =
    permissions().hasPermission(commandLevelPermission(level))

fun Player.hasPermissions(level: Int): Boolean =
    permissions().hasPermission(commandLevelPermission(level))
