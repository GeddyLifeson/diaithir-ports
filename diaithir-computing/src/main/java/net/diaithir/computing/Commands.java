package net.diaithir.computing;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import java.util.List;

/** /diaithircomputing selftest (owners). */
final class Commands {
    static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(net.minecraft.commands.Commands.literal(DiaithirComputing.ID)
                .then(net.minecraft.commands.Commands.literal("selftest").requires(net.minecraft.commands.Commands.hasPermission(net.minecraft.commands.Commands.LEVEL_OWNERS)).executes(c -> {
                    List<String> fails = SelfTest.run(c.getSource().getServer());
                    String line = fails.isEmpty() ? "Computing self test PASS" : "Computing self test FAIL: " + String.join("; ", fails);
                    c.getSource().sendSuccess(() -> Component.literal(line), false);
                    return fails.isEmpty() ? 1 : 0;
                })));
    }

    private Commands() {}
}
