package net.diaithir.diaithirfarmkitchen;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

/** /diaithirfarmkitchen reapertest (owners): builds a ripe wheat field and a chest, runs one reaper pass and reports PASS or what failed. */
final class FarmkitchenCommands {
    private FarmkitchenCommands() {}

    static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("diaithirfarmkitchen").requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                .then(Commands.literal("reapertest").executes(c -> {
                    String err = Reaper.test(c.getSource().getLevel(), BlockPos.containing(c.getSource().getPosition()));
                    c.getSource().sendSuccess(() -> Component.literal(err == null ? "Reaper test PASS" : "Reaper test FAIL: " + err), false);
                    return err == null ? 1 : 0;
                })));
    }
}
