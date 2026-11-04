package net.diaithir.worldsys.questbook;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Applies progress to quests and handles acceptance, completion, rewards and repeat cooldowns. All times are real seconds. */
public final class QuestTracker {
    private QuestTracker() {}

    public static boolean unlocked(QuestData data, String owner, Quest q) {
        for (String r : q.requires()) {
            Quest pre = QuestLoader.byId(r);
            if (pre != null && !complete(data, owner, pre)) return false;
        }
        return data.points(owner) >= q.minPoints();
    }

    public static boolean complete(QuestData data, String owner, Quest q) {
        for (int i = 0; i < q.objectives().size(); i++) {
            if (data.get(owner, q.key(i)) < q.objectives().get(i).amount()) return false;
        }
        return true;
    }

    /** Whether progress counts now: unlocked, and either automatic or accepted. */
    public static boolean active(QuestData data, String owner, Quest q) {
        return unlocked(data, owner, q) && (!q.manual() || data.isAccepted(owner, q.id())) && !complete(data, owner, q);
    }

    /** Seconds until a repeatable quest can be taken again, 0 when it can. */
    public static long cooldownLeft(QuestData data, String owner, Quest q) {
        long at = data.doneAt(owner, q.id());
        if (at == 0 || !q.repeatable()) return 0;
        return Math.max(0, at + q.cooldownSeconds() - System.currentTimeMillis() / 1000);
    }

    /** target is "ns:id" or "#ns:tag"; tagTest is asked with the tag id when the target is a tag. */
    public static boolean matches(String target, Identifier id, Predicate<Identifier> tagTest) {
        if (target.startsWith("#")) return tagTest.test(Identifier.parse(target.substring(1)));
        return target.equals(id.toString());
    }

    public static void add(ServerPlayer player, Quest.Task task, Identifier id, Predicate<Identifier> tagTest, int amount) {
        if (amount <= 0) return;
        QuestData data = QuestData.get(player.level().getServer());
        String owner = data.ownerOf(player.getUUID());
        for (Quest q : QuestLoader.all()) {
            if (!active(data, owner, q)) continue;
            boolean changed = false;
            for (int i = 0; i < q.objectives().size(); i++) {
                Quest.Objective o = q.objectives().get(i);
                if (o.task() != task || !matches(o.target(), id, tagTest)) continue;
                int cur = data.get(owner, q.key(i));
                if (cur >= o.amount()) continue;
                data.set(owner, q.key(i), Math.min(o.amount(), cur + amount));
                changed = true;
            }
            if (changed && complete(data, owner, q)) finish(player, data, owner, q);
        }
    }

    private static void tell(ServerPlayer player, QuestData data, Component msg) {
        String team = data.teamOf(player.getUUID());
        if (team == null) {
            player.sendSystemMessage(msg);
            return;
        }
        for (UUID id : data.members(team)) {
            ServerPlayer m = player.level().getServer().getPlayerList().getPlayer(id);
            if (m != null) m.sendSystemMessage(msg);
        }
    }

    private static void finish(ServerPlayer player, QuestData data, String owner, Quest q) {
        tell(player, data, Component.literal("Quest complete: " + q.title()));
        data.markDone(owner, q.id(), System.currentTimeMillis() / 1000);
        data.setAccepted(owner, q.id(), false);
        for (Quest.Reward r : q.rewards()) {
            switch (r.type()) {
                case "item" -> {
                    ItemStack s = r.stack();
                    if (!s.isEmpty() && !player.getInventory().add(s)) player.drop(s, false);
                }
                case "xp" -> player.giveExperiencePoints(r.amount());
                case "points" -> data.addPoints(owner, r.amount());
                case "command" -> player.level().getServer().getCommands().performPrefixedCommand(
                    player.createCommandSourceStack().withSuppressedOutput().withPermission(net.minecraft.server.permissions.PermissionSet.ALL_PERMISSIONS),
                    r.value().replace("@p", player.getGameProfile().name()));
                default -> { }
            }
        }
        data.setDirty();
    }

    /** Player asks to take a quest; returns the reason it cannot be taken, or null on success. */
    public static String take(ServerPlayer player, Quest q) {
        QuestData data = QuestData.get(player.level().getServer());
        String owner = data.ownerOf(player.getUUID());
        if (!unlocked(data, owner, q)) return "That quest is locked.";
        if (data.isAccepted(owner, q.id())) return "Already taken.";
        if (complete(data, owner, q)) {
            if (!q.repeatable()) return "Already done.";
            long left = cooldownLeft(data, owner, q);
            if (left > 0) return "Wait " + left + " s before repeating it.";
            data.reset(owner, q.id(), q.objectives().size());
        }
        data.setAccepted(owner, q.id(), true);
        return null;
    }

    public static void abort(ServerPlayer player, Quest q) {
        QuestData data = QuestData.get(player.level().getServer());
        data.reset(data.ownerOf(player.getUUID()), q.id(), q.objectives().size());
    }

    public static List<Quest> available() {
        return QuestLoader.all();
    }
}
