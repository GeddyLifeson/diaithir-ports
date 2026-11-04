package net.diaithir.unrest;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.List;

/** /diaithirunrest selftest (owners). */
final class UnrestCommands {
    private UnrestCommands() {}

    static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(net.minecraft.commands.Commands.literal("diaithirunrest")
                .then(net.minecraft.commands.Commands.literal("selftest").requires(net.minecraft.commands.Commands.hasPermission(net.minecraft.commands.Commands.LEVEL_OWNERS)).executes(c -> {
                    List<String> fails = SelfTest.run(c.getSource().getServer());
                    String line = fails.isEmpty() ? "Unrest self test PASS" : "Unrest self test FAIL: " + String.join("; ", fails);
                    c.getSource().sendSuccess(() -> Component.literal(line), false);
                    return fails.isEmpty() ? 1 : 0;
                })));
    }
}
