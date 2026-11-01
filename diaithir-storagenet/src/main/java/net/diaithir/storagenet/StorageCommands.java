package net.diaithir.storagenet;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

/** /diaithirstoragenet: search the terminal you have open, status of the network you stand in, and an owner-only selftest. */
final class StorageCommands {
    private StorageCommands() {}

    static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("diaithirstoragenet")
                .then(Commands.literal("search").then(Commands.argument("text", StringArgumentType.greedyString()).executes(c -> {
                    ServerPlayer p = c.getSource().getPlayerOrException();
                    TerminalMenu.setSearch(p, StringArgumentType.getString(c, "text"));
                    if (p.containerMenu instanceof TerminalMenu m) {
                        m.refresh(p);
                        m.sendAllDataToRemote();
                    }
                    c.getSource().sendSuccess(() -> Component.literal("Search set"), false);
                    return 1;
                })).executes(c -> {
                    ServerPlayer p = c.getSource().getPlayerOrException();
                    TerminalMenu.clearSearch(p);
                    if (p.containerMenu instanceof TerminalMenu m) {
                        m.refresh(p);
                        m.sendAllDataToRemote();
                    }
                    c.getSource().sendSuccess(() -> Component.literal("Search cleared"), false);
                    return 1;
                }))
                .then(Commands.literal("selftest").requires(Commands.hasPermission(Commands.LEVEL_OWNERS)).executes(c -> {
                    List<String> fails = StorageSelfTest.run(c.getSource().getServer());
                    String line = fails.isEmpty() ? "Storagenet self test PASS" : "Storagenet self test FAIL: " + String.join("; ", fails);
                    c.getSource().sendSuccess(() -> Component.literal(line), false);
                    return fails.isEmpty() ? 1 : 0;
                })));
    }
}
