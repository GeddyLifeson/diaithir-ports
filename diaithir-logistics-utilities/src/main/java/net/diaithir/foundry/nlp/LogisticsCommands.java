// SPDX-License-Identifier: LGPL-3.0-only
// Design derived from Not Logistics Pipes (c) Contributors to Not Logistics Pipes <https://nlp.rs485.network>, reimplemented in Java for NeoForge 26.1.2 (Diaithir).
package net.diaithir.foundry.nlp;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.List;

/** /diaithirlogistics selftest (owners only). */
final class LogisticsCommands {
    private LogisticsCommands() {}

    static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("diaithirlogistics")
                .then(Commands.literal("selftest").requires(Commands.hasPermission(Commands.LEVEL_OWNERS)).executes(c -> {
                    List<String> fails = LogisticsSelfTest.run(c.getSource().getServer());
                    String line = fails.isEmpty() ? "Logistics pipes self test PASS" : "Logistics pipes self test FAIL: " + String.join("; ", fails);
                    c.getSource().sendSuccess(() -> Component.literal(line), false);
                    return fails.isEmpty() ? 1 : 0;
                })));
    }
}
