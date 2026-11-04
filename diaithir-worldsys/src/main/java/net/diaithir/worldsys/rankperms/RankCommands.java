package net.diaithir.worldsys.rankperms;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.function.UnaryOperator;

/** The /rank command tree. */
final class RankCommands {
    private RankCommands() {
    }

    static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("rank").requires(s -> s.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))
                .then(Commands.literal("create").then(Commands.argument("name", StringArgumentType.word())
                        .then(Commands.argument("priority", IntegerArgumentType.integer()).executes(c -> {
                            String n = StringArgumentType.getString(c, "name");
                            RankData data = data(c);
                            data.put(new Rank(n, IntegerArgumentType.getInteger(c, "priority"), n, "white", new ArrayList<>()));
                            return msg(c, "Rank created: " + n);
                        }))))
                .then(Commands.literal("delete").then(Commands.argument("name", StringArgumentType.word()).executes(c -> {
                    data(c).remove(StringArgumentType.getString(c, "name"));
                    refreshAll(c);
                    return msg(c, "Rank deleted.");
                })))
                .then(Commands.literal("prefix").then(Commands.argument("name", StringArgumentType.word())
                        .then(Commands.argument("text", StringArgumentType.greedyString()).executes(c -> edit(c, r -> r.withPrefix(StringArgumentType.getString(c, "text")))))))
                .then(Commands.literal("color").then(Commands.argument("name", StringArgumentType.word())
                        .then(Commands.argument("colour", StringArgumentType.word()).executes(c -> edit(c, r -> r.withColor(StringArgumentType.getString(c, "colour")))))))
                .then(Commands.literal("grant").then(Commands.argument("name", StringArgumentType.word())
                        .then(Commands.argument("node", StringArgumentType.word()).executes(c -> edit(c, r -> {
                            ArrayList<String> n = new ArrayList<>(r.nodes());
                            String node = StringArgumentType.getString(c, "node");
                            if (!n.contains(node)) n.add(node);
                            return r.withNodes(n);
                        })))))
                .then(Commands.literal("revoke").then(Commands.argument("name", StringArgumentType.word())
                        .then(Commands.argument("node", StringArgumentType.word()).executes(c -> edit(c, r -> {
                            ArrayList<String> n = new ArrayList<>(r.nodes());
                            n.remove(StringArgumentType.getString(c, "node"));
                            return r.withNodes(n);
                        })))))
                .then(Commands.literal("set").then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("name", StringArgumentType.word()).executes(c -> {
                            String n = StringArgumentType.getString(c, "name");
                            if (!data(c).ranks().containsKey(n)) return msg(c, "No such rank.");
                            ServerPlayer p = EntityArgument.getPlayer(c, "player");
                            data(c).assign(p.getUUID(), n);
                            p.refreshDisplayName();
                            p.refreshTabListName();
                            return msg(c, "Rank set.");
                        }))))
                .then(Commands.literal("clear").then(Commands.argument("player", EntityArgument.player()).executes(c -> {
                    ServerPlayer p = EntityArgument.getPlayer(c, "player");
                    data(c).assign(p.getUUID(), null);
                    p.refreshDisplayName();
                    p.refreshTabListName();
                    return msg(c, "Rank cleared.");
                })))
                .then(Commands.literal("list").executes(c -> {
                    StringBuilder sb = new StringBuilder("Ranks:");
                    data(c).ranks().values().forEach(r -> sb.append(' ').append(r.name()).append('(').append(r.priority()).append(')'));
                    return msg(c, sb.toString());
                })));
    }

    private static RankData data(CommandContext<CommandSourceStack> c) {
        return RankData.get(c.getSource().getServer().overworld());
    }

    private static int msg(CommandContext<CommandSourceStack> c, String text) {
        c.getSource().sendSuccess(() -> Component.literal(text), false);
        return 1;
    }

    private static int edit(CommandContext<CommandSourceStack> c, UnaryOperator<Rank> f) {
        RankData data = data(c);
        Rank r = data.ranks().get(StringArgumentType.getString(c, "name"));
        if (r == null) return msg(c, "No such rank.");
        data.put(f.apply(r));
        refreshAll(c);
        return msg(c, "Rank updated.");
    }

    private static void refreshAll(CommandContext<CommandSourceStack> c) {
        for (ServerPlayer p : c.getSource().getServer().getPlayerList().getPlayers()) {
            p.refreshDisplayName();
            p.refreshTabListName();
        }
    }
}
