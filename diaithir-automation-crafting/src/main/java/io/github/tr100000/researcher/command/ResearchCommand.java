package io.github.tr100000.researcher.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.github.tr100000.researcher.Research;
import io.github.tr100000.researcher.ResearchManager;
import io.github.tr100000.researcher.Researcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.Commands.CommandSelection;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class ResearchCommand {
    private ResearchCommand() {}

    private static final DynamicCommandExceptionType EXCEPTION = new DynamicCommandExceptionType(Message.class::cast);
    public static final SuggestionProvider<CommandSourceStack> SUGGESTION_PROVIDER = (context, builder)
            -> SharedSuggestionProvider.suggestResource(context.getSource().getServer().researcher$getServerManager().listAllIds(), builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, CommandSelection environment) {
        dispatcher.register(literal(ResearchManager.PATH)
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(literal("grant")
                        .then(argument("targets", EntityArgument.players())
                                .then(literal("only")
                                        .then(argument("research", IdentifierArgument.id())
                                                .suggests(SUGGESTION_PROVIDER)
                                                .executes(context -> execute(context, Operation.GRANT, Selection.ONLY))
                                        )
                                )
                                .then(literal("from")
                                        .then(argument("research", IdentifierArgument.id())
                                                .suggests(SUGGESTION_PROVIDER)
                                                .executes(context -> execute(context, Operation.GRANT, Selection.FROM))
                                        )
                                )
                                .then(literal("until")
                                        .then(argument("research", IdentifierArgument.id())
                                                .suggests(SUGGESTION_PROVIDER)
                                                .executes(context -> execute(context, Operation.GRANT, Selection.UNTIL))
                                        )
                                )
                                .then(literal("through")
                                        .then(argument("research", IdentifierArgument.id())
                                                .suggests(SUGGESTION_PROVIDER)
                                                .executes(context -> execute(context, Operation.GRANT, Selection.THROUGH))
                                        )
                                )
                                .then(literal("everything")
                                        .executes(context -> execute(context, Operation.GRANT, Selection.EVERYTHING))
                                )
                        )
                )
                .then(literal("revoke")
                        .then(argument("targets", EntityArgument.players())
                                .then(literal("only")
                                        .then(argument("research", IdentifierArgument.id())
                                                .suggests(SUGGESTION_PROVIDER)
                                                .executes(context -> execute(context, Operation.REVOKE, Selection.ONLY))
                                        )
                                )
                                .then(literal("from")
                                        .then(argument("research", IdentifierArgument.id())
                                                .suggests(SUGGESTION_PROVIDER)
                                                .executes(context -> execute(context, Operation.REVOKE, Selection.FROM))
                                        )
                                )
                                .then(literal("until")
                                        .then(argument("research", IdentifierArgument.id())
                                                .suggests(SUGGESTION_PROVIDER)
                                                .executes(context -> execute(context, Operation.REVOKE, Selection.UNTIL))
                                        )
                                )
                                .then(literal("through")
                                        .then(argument("research", IdentifierArgument.id())
                                                .suggests(SUGGESTION_PROVIDER)
                                                .executes(context -> execute(context, Operation.REVOKE, Selection.THROUGH))
                                        )
                                )
                                .then(literal("everything")
                                        .executes(context -> execute(context, Operation.REVOKE, Selection.EVERYTHING))
                                )
                        )
                )
                .then(literal("increment")
                        .then(argument("targets", EntityArgument.players())
                                .then(argument("research", IdentifierArgument.id())
                                        .suggests(SUGGESTION_PROVIDER)
                                        .then(argument("count", IntegerArgumentType.integer())
                                            .executes(ResearchCommand::increment)
                                        )
                                )
                        )
                )
                .then(literal("list")
                        .requires(CommandSourceStack::isPlayer)
                        .executes(context -> execute(context, Operation.LIST, Selection.EVERYTHING))
                        .then(literal("from")
                                .then(argument("research", IdentifierArgument.id())
                                        .suggests(SUGGESTION_PROVIDER)
                                        .executes(context -> execute(context, Operation.LIST, Selection.FROM))
                                )
                        )
                        .then(literal("until")
                                .then(argument("research", IdentifierArgument.id())
                                        .suggests(SUGGESTION_PROVIDER)
                                        .executes(context -> execute(context, Operation.LIST, Selection.UNTIL))
                                )
                        )
                        .then(literal("through")
                                .then(argument("research", IdentifierArgument.id())
                                        .suggests(SUGGESTION_PROVIDER)
                                        .executes(context -> execute(context, Operation.LIST, Selection.THROUGH))
                                )
                        )
                        .then(literal("everything")
                                .executes(context -> execute(context, Operation.LIST, Selection.EVERYTHING))
                        )
                        .then(literal("root")
                                .executes(context -> execute(context, Operation.LIST, Selection.ROOT))
                        )
                )
        );
    }

    private static int execute(CommandContext<CommandSourceStack> context, Operation operation, Selection selection) throws CommandSyntaxException {
        if (operation == Operation.LIST) {
            return executeList(context, selection);
        }

        Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "targets");
        Set<Research> selected = select(context, selection);

        int i = 0;
        try {
            for (ServerPlayer player : targets) {
                i += operation.processAll(player, selected);
            }
        }
        catch (Exception e) {
            Researcher.LOGGER.info("Unknown error!", e);
            return -1;
        }

        ResearchManager researchManager = context.getSource().getServer().researcher$getServerManager();
        if (i == 0) {
            if (selected.size() == 1) {
                if (targets.size() == 1) {
                    throw EXCEPTION.create(
                            Component.translatable(
                                    operation.getCommandPrefix() + ".one.to.one.failure",
                                    selected.iterator().next().getTitle(researchManager),
                                    targets.iterator().next().getDisplayName()
                            )
                    );
                } else {
                    throw EXCEPTION.create(
                            Component.translatable(
                                    operation.getCommandPrefix() + ".one.to.many.failure", selected.iterator().next().getTitle(researchManager), targets.size()
                            )
                    );
                }
            } else if (targets.size() == 1) {
                throw EXCEPTION.create(
                        Component.translatable(
                                operation.getCommandPrefix() + ".many.to.one.failure", selected.size(), targets.iterator().next().getDisplayName()
                        )
                );
            } else {
                throw EXCEPTION.create(Component.translatable(operation.getCommandPrefix() + ".many.to.many.failure", selected.size(), targets.size()));
            }
        }
        else {
            if (selected.size() == 1) {
                if (targets.size() == 1) {
                    context.getSource().sendSuccess(
                            () -> Component.translatable(
                                    operation.getCommandPrefix() + ".one.to.one.success",
                                    selected.iterator().next().getTitle(researchManager),
                                    targets.iterator().next().getDisplayName()
                            ),
                            true
                    );
                } else {
                    context.getSource().sendSuccess(
                            () -> Component.translatable(
                                    operation.getCommandPrefix() + ".one.to.many.success", selected.iterator().next().getTitle(researchManager), targets.size()
                            ),
                            true
                    );
                }
            } else if (targets.size() == 1) {
                context.getSource().sendSuccess(
                        () -> Component.translatable(
                                operation.getCommandPrefix() + ".many.to.one.success", selected.size(), targets.iterator().next().getDisplayName()
                        ),
                        true
                );
            } else {
                context.getSource().sendSuccess(() -> Component.translatable(operation.getCommandPrefix() + ".many.to.many.success", selected.size(), targets.size()), true);
            }

            return i;
        }
    }

    private static int executeList(CommandContext<CommandSourceStack> context, Selection selection) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Set<Research> selected = select(context, selection);
        return Operation.LIST.processAll(player, selected);
    }

    private static int increment(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ResearchManager researchManager = context.getSource().getServer().researcher$getServerManager();
        Research research = researchManager.get(IdentifierArgument.getId(context, "research"));
        if (research != null) {
            int count = IntegerArgumentType.getInteger(context, "count");
            Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "targets");
            targets.forEach(player -> player.researcher$getPlayerTracker().incrementCriterion(research, count));
            return 0;
        }
        return -1;
    }

    public static Set<Research> select(CommandContext<CommandSourceStack> context, Selection selection) {
        ResearchManager researchManager = context.getSource().getServer().researcher$getServerManager();
        switch (selection) {
            case Selection.EVERYTHING -> {
                return researchManager.listAllAsSet();
            }
            case Selection.ROOT -> {
                return researchManager.getRootNodes();
            }
            default -> {
                Identifier id = IdentifierArgument.getId(context, "research");
                Research research = researchManager.get(id);
                Set<Research> nodes = new HashSet<>();
                if (selection.before) {
                    nodes.addAll(researchManager.allPredecessorsOf(research));
                }
                nodes.add(research);
                if (selection.after) {
                    nodes.addAll(researchManager.allSuccessorsOf(research));
                }
                return nodes;
            }
        }
    }

    public enum Operation {
        GRANT("grant") {
            @Override
            protected boolean processEach(ServerPlayer player, Research research) {
                research.prerequisites(player.level().getServer().researcher$getServerManager()).forEach(research1 -> {
                    processEach(player, research1);
                });
                return player.researcher$getPlayerTracker().grantCriterion(research);
            }
        },
        REVOKE("revoke") {
            @Override
            protected boolean processEach(ServerPlayer player, Research research) {
                return player.researcher$getPlayerTracker().revokeCriterion(research);
            }
        },
        LIST("list") {
            @Override
            protected boolean processEach(ServerPlayer player, Research research) {
                player.sendSystemMessage(Component.literal(player.level().getServer().researcher$getServerManager().getIdOrEmpty(research).toString()));
                return true;
            }
        };

        private final String commandPrefix;

        Operation(final String name) {
            this.commandPrefix = "commands." + Researcher.MODID + "." + name;
        }

        public int processAll(ServerPlayer player, Iterable<Research> researchList) {
            int i = 0;
            for (Research research : researchList) {
                if (this.processEach(player, research)) {
                    i++;
                }
            }
            return i;
        }

        protected abstract boolean processEach(ServerPlayer player, Research research);

        public String getCommandPrefix() {
            return this.commandPrefix;
        }
    }

    public enum Selection {
        ONLY(false, false),
        THROUGH(true, true),
        FROM(false, true),
        UNTIL(true, false),
        EVERYTHING(true, true),
        ROOT(false, false);

        final boolean before;
        final boolean after;

        Selection(final boolean before, final boolean after) {
            this.before = before;
            this.after = after;
        }
    }
}
