package net.diaithir.steamrails;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/** /diaithirsteamrails selftest (owners): builds a boiler yard high above spawn, burns fuel and checks pressure, steam flow, the tender, the registries and the train hooks. */
final class SteamCommands {
    private SteamCommands() {}

    static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("diaithirsteamrails")
                .then(Commands.literal("selftest").requires(Commands.hasPermission(Commands.LEVEL_OWNERS)).executes(c -> {
                    String err = SelfTest.start(c.getSource());
                    c.getSource().sendSuccess(() -> Component.literal(err == null ? "Steamrails self test started (about 15 seconds)" : "Steamrails self test not started: " + err), false);
                    return err == null ? 1 : 0;
                })));
    }
}
