package net.diaithir.wiretech.circuit;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/** /circuit shows the circuit block you look at, /circuit set &lt;program&gt; programs it. Programs are described in {@link Logic}. */
public final class CircuitCommands {
    private CircuitCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(net.minecraft.commands.Commands.literal("circuit")
                .executes(c -> with(c.getSource(), null))
                .then(net.minecraft.commands.Commands.literal("set").then(net.minecraft.commands.Commands.argument("program", StringArgumentType.greedyString())
                        .executes(c -> with(c.getSource(), StringArgumentType.getString(c, "program"))))));
    }

    private static int with(CommandSourceStack src, String program) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player = src.getPlayerOrException();
        HitResult hit = player.pick(6.0, 0.0F, false);
        if (!(hit instanceof BlockHitResult bh) || !(player.level().getBlockEntity(bh.getBlockPos()) instanceof CircuitNodeBE be)) {
            src.sendFailure(Component.literal("Look at a circuit block."));
            return 0;
        }
        if (program != null) {
            if (!be.takesPrograms()) {
                src.sendFailure(Component.literal("The " + be.kind.display + " has no program."));
                return 0;
            }
            try {
                be.setProgram(program);
            } catch (IllegalArgumentException e) {
                src.sendFailure(Component.literal(e.getMessage()));
                return 0;
            }
        }
        src.sendSuccess(() -> Component.literal(be.describe()), false);
        return 1;
    }
}
