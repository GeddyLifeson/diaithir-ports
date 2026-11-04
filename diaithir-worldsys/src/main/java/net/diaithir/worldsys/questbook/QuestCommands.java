package net.diaithir.worldsys.questbook;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** /quests list, progress and team management. */
public final class QuestCommands {
    private QuestCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("quests")
                .then(Commands.literal("list").executes(c -> show(c.getSource().getPlayerOrException(), false)))
                .then(Commands.literal("progress").executes(c -> show(c.getSource().getPlayerOrException(), true)))
                .then(Commands.literal("take").then(Commands.argument("quest", StringArgumentType.word()).executes(c -> {
                    Quest q = QuestLoader.byId(StringArgumentType.getString(c, "quest"));
                    if (q == null) return msg(c.getSource(), "No such quest.");
                    String why = QuestTracker.take(c.getSource().getPlayerOrException(), q);
                    return msg(c.getSource(), why == null ? "Taken: " + q.title() : why);
                })))
                .then(Commands.literal("abort").then(Commands.argument("quest", StringArgumentType.word()).executes(c -> {
                    Quest q = QuestLoader.byId(StringArgumentType.getString(c, "quest"));
                    if (q == null) return msg(c.getSource(), "No such quest.");
                    QuestTracker.abort(c.getSource().getPlayerOrException(), q);
                    return msg(c.getSource(), "Dropped: " + q.title());
                })))
                .then(Commands.literal("info").then(Commands.argument("quest", StringArgumentType.word()).executes(c -> {
                    Quest q = QuestLoader.byId(StringArgumentType.getString(c, "quest"));
                    if (q == null) return msg(c.getSource(), "No such quest.");
                    return info(c.getSource().getPlayerOrException(), q);
                })))
                .then(Commands.literal("points").executes(c -> {
                    ServerPlayer p = c.getSource().getPlayerOrException();
                    QuestData data = QuestData.get(c.getSource().getServer());
                    return msg(c.getSource(), "Quest points: " + data.points(data.ownerOf(p.getUUID())));
                }))
                .then(Commands.literal("team")
                        .then(Commands.literal("create").then(Commands.argument("name", StringArgumentType.word()).executes(c -> {
                            ServerPlayer p = c.getSource().getPlayerOrException();
                            QuestData data = QuestData.get(c.getSource().getServer());
                            String name = StringArgumentType.getString(c, "name");
                            if (data.teamExists(name)) return msg(c.getSource(), "That team already exists.");
                            data.setTeam(p.getUUID(), name);
                            return msg(c.getSource(), "Team " + name + " created.");
                        })))
                        .then(Commands.literal("join").then(Commands.argument("name", StringArgumentType.word()).executes(c -> {
                            ServerPlayer p = c.getSource().getPlayerOrException();
                            QuestData data = QuestData.get(c.getSource().getServer());
                            String name = StringArgumentType.getString(c, "name");
                            if (!data.teamExists(name)) return msg(c.getSource(), "No such team.");
                            data.setTeam(p.getUUID(), name);
                            return msg(c.getSource(), "Joined team " + name + ".");
                        })))
                        .then(Commands.literal("leave").executes(c -> {
                            ServerPlayer p = c.getSource().getPlayerOrException();
                            QuestData.get(c.getSource().getServer()).setTeam(p.getUUID(), null);
                            return msg(c.getSource(), "You left your team.");
                        }))));
    }

    private static int msg(CommandSourceStack src, String text) {
        src.sendSuccess(() -> Component.literal(text), false);
        return 1;
    }

    private static int show(ServerPlayer p, boolean onlyActive) {
        QuestData data = QuestData.get(p.level().getServer());
        String owner = data.ownerOf(p.getUUID());
        for (Quest q : QuestLoader.all()) {
            boolean open = QuestTracker.unlocked(data, owner, q);
            boolean done = QuestTracker.complete(data, owner, q);
            boolean on = QuestTracker.active(data, owner, q);
            if (onlyActive && !on) continue;
            String state = done ? "done" : !open ? "locked" : q.manual() && !on ? "available (/quests take " + q.id() + ")" : progressText(data, owner, q);
            p.sendSystemMessage(Component.literal("[" + q.category() + "] " + q.title() + " (" + q.id() + "): " + state));
        }
        return 1;
    }

    private static String progressText(QuestData data, String owner, Quest q) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < q.objectives().size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(data.get(owner, q.key(i))).append('/').append(q.objectives().get(i).amount());
        }
        return sb.toString();
    }

    private static int info(ServerPlayer p, Quest q) {
        QuestData data = QuestData.get(p.level().getServer());
        String owner = data.ownerOf(p.getUUID());
        p.sendSystemMessage(Component.literal(q.title() + (q.description().isEmpty() ? "" : ": " + q.description())));
        for (int i = 0; i < q.objectives().size(); i++) {
            Quest.Objective o = q.objectives().get(i);
            p.sendSystemMessage(Component.literal("  " + o.task().name().toLowerCase() + " " + o.target() + " " + data.get(owner, q.key(i)) + "/" + o.amount()));
        }
        for (Quest.Reward r : q.rewards()) p.sendSystemMessage(Component.literal("  reward: " + r.type() + " " + r.value() + (r.amount() > 1 ? " x" + r.amount() : "")));
        return 1;
    }
}
