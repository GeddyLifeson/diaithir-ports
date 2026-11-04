package net.diaithir.carts;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

/** /diaithircarts selftest (owners): builds a test yard high above spawn and checks the speed caps, powered rails, coupling, cutters and the station. */
final class CartCommands {
    private CartCommands() {}

    static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("diaithircarts")
                .then(Commands.literal("routingtest").requires(Commands.hasPermission(Commands.LEVEL_OWNERS)).executes(c -> {
                    String err = RoutingTest.start(c.getSource());
                    c.getSource().sendSuccess(() -> Component.literal(err == null ? "Routing test started (about 9 seconds)" : "Routing test not started: " + err), false);
                    return err == null ? 1 : 0;
                }))
                .then(Commands.literal("scheduletest").requires(Commands.hasPermission(Commands.LEVEL_OWNERS)).executes(c -> {
                    String err = ScheduleTest.start(c.getSource());
                    c.getSource().sendSuccess(() -> Component.literal(err == null ? "Schedule test started (about 26 seconds)" : "Schedule test not started: " + err), false);
                    return err == null ? 1 : 0;
                }))
                .then(Commands.literal("signaltest").requires(Commands.hasPermission(Commands.LEVEL_OWNERS)).executes(c -> {
                    String err = SignalTest.start(c.getSource());
                    c.getSource().sendSuccess(() -> Component.literal(err == null ? "Signal test started (about 4 seconds)" : "Signal test not started: " + err), false);
                    return err == null ? 1 : 0;
                }))
                .then(Commands.literal("selftest").requires(Commands.hasPermission(Commands.LEVEL_OWNERS)).executes(c -> {
                    String err = CartSelfTest.start(c.getSource());
                    c.getSource().sendSuccess(() -> Component.literal(err == null ? "Carts self test started (about 10 seconds)" : "Carts self test not started: " + err), false);
                    return err == null ? 1 : 0;
                }))
                .then(Commands.literal("reapertest").requires(Commands.hasPermission(Commands.LEVEL_OWNERS)).executes(c -> {
                    String err = Reaper.test(c.getSource().getLevel(), BlockPos.containing(c.getSource().getPosition()));
                    c.getSource().sendSuccess(() -> Component.literal(err == null ? "Reaper test PASS" : "Reaper test FAIL: " + err), false);
                    return err == null ? 1 : 0;
                }))
                .then(Commands.literal("freighttest").requires(Commands.hasPermission(Commands.LEVEL_OWNERS)).executes(c -> {
                    String err = net.diaithir.freight.FreightTest.run(c.getSource().getServer());
                    c.getSource().sendSuccess(() -> Component.literal(err == null ? "Freight test PASS" : "Freight test FAIL: " + err), false);
                    return err == null ? 1 : 0;
                }))
                .then(Commands.literal("roadtest").requires(Commands.hasPermission(Commands.LEVEL_OWNERS)).executes(c -> {
                    String err = RoadTest.run(c.getSource().getServer());
                    c.getSource().sendSuccess(() -> Component.literal(err == null ? "Road test PASS" : "Road test FAIL: " + err), false);
                    return err == null ? 1 : 0;
                })));
    }
}
