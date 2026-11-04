package net.diaithir.portable;

import java.util.Map;

/**
 * Old registry names from the replaced mods mapped to ours. No DataFixer hook is used; this is a documented static map
 * that a datapack remap or a command can read. Keys are old "namespace:path", values our path under diaithirportable.
 */
public final class IdMigration {
    public static final Map<String, String> MAP = Map.ofEntries(
            Map.entry("sophisticatedstorage:chest", "wood_storage"),
            Map.entry("sophisticatedstorage:iron_chest", "iron_storage"),
            Map.entry("sophisticatedstorage:gold_chest", "gold_storage"),
            Map.entry("sophisticatedstorage:diamond_chest", "diamond_storage"),
            Map.entry("sophisticatedstorage:netherite_chest", "netherite_storage"),
            Map.entry("sophisticatedstorage:barrel", "wood_storage"),
            Map.entry("sophisticatedstorage:iron_barrel", "iron_storage"),
            Map.entry("sophisticatedstorage:gold_barrel", "gold_storage"),
            Map.entry("sophisticatedstorage:diamond_barrel", "diamond_storage"),
            Map.entry("sophisticatedstorage:netherite_barrel", "netherite_storage"),
            Map.entry("sophisticatedbackpacks:backpack", "wood_backpack"),
            Map.entry("sophisticatedbackpacks:iron_backpack", "iron_backpack"),
            Map.entry("sophisticatedbackpacks:gold_backpack", "gold_backpack"),
            Map.entry("sophisticatedbackpacks:diamond_backpack", "diamond_backpack"),
            Map.entry("sophisticatedbackpacks:netherite_backpack", "netherite_backpack"),
            Map.entry("sophisticatedbackpacks:stack_upgrade_tier_1", "stack_upgrade"),
            Map.entry("sophisticatedbackpacks:pickup_upgrade", "pickup_upgrade"),
            Map.entry("sophisticatedbackpacks:feeding_upgrade", "feeding_upgrade"),
            Map.entry("sophisticatedbackpacks:magnet_upgrade", "magnet_upgrade"),
            Map.entry("sophisticatedbackpacks:void_upgrade", "void_upgrade"),
            Map.entry("sophisticatedbackpacks:smelting_upgrade", "smelting_upgrade"),
            Map.entry("sophisticatedbackpacks:crafting_upgrade", "crafting_upgrade"),
            Map.entry("sophisticatedbackpacks:jukebox_upgrade", "jukebox_upgrade"),
            Map.entry("sophisticatedstorageinmotion:storage_in_motion", "wood_backpack"),
            Map.entry("deepnull:deep_null", "deep_null"),
            Map.entry("deepnull:deepnull", "deep_null"));

    private IdMigration() {
    }

    /** Returns our full id for an old id, or null when unknown. */
    public static String migrate(String oldId) {
        String path = MAP.get(oldId);
        return path == null ? null : DiaithirPortable.ID + ":" + path;
    }
}
