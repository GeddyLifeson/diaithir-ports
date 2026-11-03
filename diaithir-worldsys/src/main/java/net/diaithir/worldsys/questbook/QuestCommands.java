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
        for (Quest q : Quest.ALL) {
            boolean open = QuestTracker.unlocked(data, owner, q);
            int cur = data.get(owner, q.id());
            boolean done = cur >= q.amount();
            if (onlyActive && (!open || done)) continue;
            String state = done ? "done" : open ? cur + "/" + q.amount() : "locked";
            p.sendSystemMessage(Component.literal(q.title() + ": " + state));
        }
        return 1;
    }
}
