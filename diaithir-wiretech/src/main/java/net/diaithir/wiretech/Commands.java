package net.diaithir.wiretech;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import net.minecraft.core.BlockPos;

import java.util.List;

/** /wiretech selftest (owners): places machines high above spawn, powers them and checks their output. */
final class Commands {
    private Commands() {}

    static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(net.minecraft.commands.Commands.literal("wiretech")
                .then(net.minecraft.commands.Commands.literal("selftest")
                        .requires(net.minecraft.commands.Commands.hasPermission(net.minecraft.commands.Commands.LEVEL_OWNERS)).executes(c -> {
                            List<String> fails = SelfTest.run(c.getSource().getServer());
                            String line = fails.isEmpty() ? "Wiretech self test PASS" : "Wiretech self test FAIL: " + String.join("; ", fails);
                            c.getSource().sendSuccess(() -> Component.literal(line), false);
                            return fails.isEmpty() ? 1 : 0;
                        }))
                .then(net.minecraft.commands.Commands.literal("gui")
                        .requires(net.minecraft.commands.Commands.hasPermission(net.minecraft.commands.Commands.LEVEL_OWNERS))
                        .then(net.minecraft.commands.Commands.argument("pos", net.minecraft.commands.arguments.coordinates.BlockPosArgument.blockPos()).executes(c -> {
                            var player = c.getSource().getPlayerOrException();
                            BlockPos p = net.minecraft.commands.arguments.coordinates.BlockPosArgument.getBlockPos(c, "pos");
                            if (player.level().getBlockEntity(p) instanceof MachineBlockEntity be) {
                                player.openMenu(be, buf -> buf.writeVarInt(be.kind.ordinal()));
                                return 1;
                            }
                            return 0;
                        })))
                .then(net.minecraft.commands.Commands.literal("demo")
                        .requires(net.minecraft.commands.Commands.hasPermission(net.minecraft.commands.Commands.LEVEL_OWNERS)).executes(c -> {
                            BlockPos o = Demo.build(c.getSource().getServer());
                            c.getSource().sendSuccess(() -> Component.literal("Wiretech demo origin " + o.getX() + " " + o.getY() + " " + o.getZ()), false);
                            return 1;
                        })));
    }
}
