package io.github.tr100000.researcher.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import io.github.tr100000.researcher.Researcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import static net.minecraft.commands.Commands.literal;

public final class ResearcherCommand {
    private ResearcherCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
        dispatcher.register(literal(Researcher.MODID)
                .executes(ResearcherCommand::printInfo)
                .then(literal("version")
                        .executes(ResearcherCommand::printInfo)
                )
        );
    }

    private static int printInfo(CommandContext<CommandSourceStack> context) {
        Component message = Component.literal("Researcher ").append(Component.literal("v" + Researcher.getVersion()).withStyle(ChatFormatting.GOLD));
        context.getSource().sendSystemMessage(message);
        return 0;
    }
}
