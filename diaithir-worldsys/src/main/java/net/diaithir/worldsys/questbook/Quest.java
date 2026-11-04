package net.diaithir.worldsys.questbook;

import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * One quest definition, loaded from data/&lt;namespace&gt;/questbook/*.json (see {@link QuestLoader}).
 * Clean-room quest engine: behaviour modelled on what questing mods do (several objectives, prerequisites, categories,
 * accept and abort, rewards that are items, experience, points or commands, cooldowns for repeatables); no code taken.
 */
public record Quest(String id, String title, String category, String description, List<String> requires, int minPoints,
        List<Objective> objectives, List<Reward> rewards, boolean manual, boolean repeatable, long cooldownSeconds) {

    public enum Task { KILL, MINE, COLLECT, CRAFT }

    /** One thing to do. Target is an id like minecraft:zombie, or #namespace:tag for an item, block or entity tag. */
    public record Objective(Task task, String target, int amount) {}

    /** What completing a quest gives. type is item, xp, points or command. */
    public record Reward(String type, String value, int amount) {
        public ItemStack stack() {
            if (!type.equals("item")) return ItemStack.EMPTY;
            return BuiltInRegistries.ITEM.get(Identifier.parse(value)).map(h -> new ItemStack(h.value(), amount)).orElse(ItemStack.EMPTY);
        }
    }

    /** Progress key of objective i: the quest id for the first (keeps older saves valid), id#i for the rest. */
    public String key(int i) {
        return i == 0 ? id : id + "#" + i;
    }
}
