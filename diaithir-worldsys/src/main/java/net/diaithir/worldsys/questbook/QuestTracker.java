package net.diaithir.worldsys.questbook;

import java.util.UUID;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Applies progress to quests and handles completion. */
public final class QuestTracker {
    private QuestTracker() {}

    public static boolean unlocked(QuestData data, String owner, Quest q) {
        if (q.requires() == null) return true;
        Quest pre = Quest.byId(q.requires());
        return pre == null || data.get(owner, pre.id()) >= pre.amount();
    }

    public static void add(ServerPlayer player, Quest.Task task, String target, int amount) {
        if (amount <= 0) return;
        QuestData data = QuestData.get(player.level().getServer());
        String owner = data.ownerOf(player.getUUID());
        for (Quest q : Quest.ALL) {
            if (q.task() != task || !q.target().equals(target)) continue;
            int cur = data.get(owner, q.id());
            if (cur >= q.amount() || !unlocked(data, owner, q)) continue;
            int next = Math.min(q.amount(), cur + amount);
            data.set(owner, q.id(), next);
            if (next >= q.amount()) complete(player, data, q);
        }
    }

    private static void complete(ServerPlayer player, QuestData data, Quest q) {
        Component msg = Component.literal("Quest complete: " + q.title());
        String team = data.teamOf(player.getUUID());
        if (team == null) {
            player.sendSystemMessage(msg);
        } else {
            for (UUID id : data.members(team)) {
                ServerPlayer m = player.level().getServer().getPlayerList().getPlayer(id);
                if (m != null) m.sendSystemMessage(msg);
            }
        }
        ItemStack reward = q.reward().copy();
        if (!player.getInventory().add(reward)) player.drop(reward, false);
    }
}
