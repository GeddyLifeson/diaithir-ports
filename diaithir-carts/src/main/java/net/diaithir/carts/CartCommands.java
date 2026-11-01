package net.diaithir.carts;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/** /diaithircarts selftest (owners): builds a test yard high above spawn and checks the speed caps, powered rails, coupling, cutters and the station. */
final class CartCommands {
    private CartCommands() {}

    static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("diaithircarts")
                .then(Commands.literal("selftest").requires(Commands.hasPermission(Commands.LEVEL_OWNERS)).executes(c -> {
                    String err = CartSelfTest.start(c.getSource());
                    c.getSource().sendSuccess(() -> Component.literal(err == null ? "Carts self test started (about 10 seconds)" : "Carts self test not started: " + err), false);
                    return err == null ? 1 : 0;
                })));
    }
}
