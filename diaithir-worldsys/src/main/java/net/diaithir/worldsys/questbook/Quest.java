package net.diaithir.worldsys.questbook;

import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** One quest definition. */
public record Quest(String id, String title, Task task, String target, int amount, String requires, ItemStack reward) {
    public enum Task { KILL, MINE, COLLECT, CRAFT }

    public static final List<Quest> ALL = List.of(
            new Quest("first_wood", "Gather Wood", Task.MINE, "minecraft:oak_log", 8, null, new ItemStack(Items.BREAD, 8)),
            new Quest("workbench", "Get Crafty", Task.CRAFT, "minecraft:crafting_table", 1, "first_wood", new ItemStack(Items.STONE_PICKAXE)),
            new Quest("stone_age", "Stone Age", Task.COLLECT, "minecraft:cobblestone", 32, "workbench", new ItemStack(Items.COAL, 16)),
            new Quest("zombie_hunt", "Night Watch", Task.KILL, "minecraft:zombie", 10, "stone_age", new ItemStack(Items.IRON_SWORD)),
            new Quest("iron_miner", "Iron Miner", Task.MINE, "minecraft:iron_ore", 12, "stone_age", new ItemStack(Items.IRON_INGOT, 16)),
            new Quest("diamond_dream", "Diamond Dream", Task.COLLECT, "minecraft:diamond", 5, "iron_miner", new ItemStack(Items.GOLDEN_APPLE, 4)));

    public static Quest byId(String id) {
        return ALL.stream().filter(q -> q.id.equals(id)).findFirst().orElse(null);
    }
}
