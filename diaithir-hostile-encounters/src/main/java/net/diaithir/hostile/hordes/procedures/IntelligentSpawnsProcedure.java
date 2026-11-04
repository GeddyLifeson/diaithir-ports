package net.diaithir.hostile.hordes.procedures;

import net.diaithir.hostile.hordes.HordeHoardConstants;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;

/**
 * Source event (1.20.1 Forge): EntityJoinLevelEvent.
 * Wired in 26.1.2 from per-loader handlers.
 *
 * Joins horde_hoard:intelligent_teams members to the "intelligentmobs" scoreboard team
 * by running a /team join command as the entity itself with op level 4.
 */
public class IntelligentSpawnsProcedure {

    public static void execute(Entity entity) {
        if (entity == null)
            return;
        if (entity.getType().builtInRegistryHolder().is(TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(HordeHoardConstants.MOD_ID, "intelligent_teams")))) {
            {
                Entity _ent = entity;
                if (!_ent.level().isClientSide() && _ent.level().getServer() != null) {
                    _ent.level().getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, _ent.position(), _ent.getRotationVector(), _ent.level() instanceof ServerLevel ? (ServerLevel) _ent.level() : null, PermissionSet.ALL_PERMISSIONS,
                            _ent.getName().getString(), _ent.getDisplayName(), _ent.level().getServer(), _ent), "team join intelligentmobs");
                }
            }
        }
    }
}
