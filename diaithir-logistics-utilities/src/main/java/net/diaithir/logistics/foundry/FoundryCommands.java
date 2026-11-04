package net.diaithir.logistics.foundry;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.List;

/** /foundrydepot selftest (owner only). */
final class FoundryCommands {
    private FoundryCommands() {}

    static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("foundrydepot")
                .then(Commands.literal("selftest").requires(Commands.hasPermission(Commands.LEVEL_OWNERS)).executes(c -> {
                    List<String> fails = FoundrySelfTest.run(c.getSource().getServer());
                    String line = fails.isEmpty() ? "Foundry depot self test PASS" : "Foundry depot self test FAIL: " + String.join("; ", fails);
                    c.getSource().sendSuccess(() -> Component.literal(line), false);
                    return fails.isEmpty() ? 1 : 0;
                })));
    }
}
