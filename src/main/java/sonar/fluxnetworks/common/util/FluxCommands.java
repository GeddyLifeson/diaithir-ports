package sonar.fluxnetworks.common.util;

import net.minecraft.server.permissions.Permissions;
import net.minecraft.server.players.NameAndId;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import sonar.fluxnetworks.FluxConfig;
import sonar.fluxnetworks.FluxNetworks;
import sonar.fluxnetworks.common.data.FluxPlayerData;
import sonar.fluxnetworks.register.Messages;

import javax.annotation.Nonnull;
import java.util.Collection;

public class FluxCommands {

    public static void register(@Nonnull CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(FluxNetworks.MODID)
                .then(Commands.literal("superadmin")
                        .requires(Commands.hasPermission(Commands.LEVEL_MODERATORS))
                        .then(Commands.argument("targets", GameProfileArgument.gameProfile())
                                .then(Commands.argument("enable", BoolArgumentType.bool())
                                        .executes(s -> superAdmin(s.getSource(),
                                                GameProfileArgument.getGameProfiles(s, "targets"),
                                                BoolArgumentType.getBool(s, "enable"))
                                        )
                                )
                        )
                )
        );
    }

    private static int superAdmin(@Nonnull CommandSourceStack source,
                                  @Nonnull Collection<NameAndId> profiles, boolean enable) {
        PlayerList playerList = source.getServer().getPlayerList();
        int success = 0;

        for (NameAndId profile : profiles) {
            ServerPlayer player = playerList.getPlayer(profile.id());
            if (player != null) {
                final FluxPlayerData fp = FluxUtils.getPlayerData(player);
                if (fp != null &&
                        (((fp.isSuperAdmin() || FluxConfig.enableSuperAdmin) && source.permissions().hasPermission(Permissions.COMMANDS_ADMIN)) ||
                                (player == source.getEntity() && (fp.isSuperAdmin() || FluxPlayerData.canActivateSuperAdmin(player)))) &&
                        fp.setSuperAdmin(enable)) {
                    Messages.syncCapability(player);
                    player.sendSystemMessage(Component.translatable(enable ?
                            "gui.fluxnetworks.superadmin.on" : "gui.fluxnetworks.superadmin.off"));
                    success++;
                }
            }
        }

        return success;
    }
}
