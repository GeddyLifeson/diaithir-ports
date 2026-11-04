package io.github.tr100000.researcher.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.github.tr100000.researcher.Research;
import io.github.tr100000.researcher.screen.ResearchScreen;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NullMarked;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

@NullMarked
public final class ResearcherClientCommand {
    private ResearcherClientCommand() {}

    public static final SuggestionProvider<CommandSourceStack> SUGGESTION_PROVIDER = (context, builder) ->
            SharedSuggestionProvider.suggestResource(Minecraft.getInstance().getConnection().researcher$getClientTracker().listAllIds(), builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(literal("researcher_client")
                .then(literal("show")
                        .then(argument("research", IdentifierArgument.id())
                                .suggests(SUGGESTION_PROVIDER)
                                .executes(ResearcherClientCommand::showResearch)
                        )
                )
        );
    }

    private static int showResearch(CommandContext<CommandSourceStack> context) {
        Minecraft client = Minecraft.getInstance();
        Identifier id = context.getArgument("research", Identifier.class);
        Research research = client.getConnection().researcher$getClientTracker().get(id);
        if (research != null) {
            ResearchScreen.setSelected(research);
            client.setScreen(new ResearchScreen(client.screen));
            return 1;
        }
        else {
            context.getSource().sendFailure(Component.literal("Unknown research id"));
            return 0;
        }
    }
}
