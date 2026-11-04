package me.desht.pneumaticcraft.client;

import me.desht.pneumaticcraft.api.lib.Names;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

import java.util.Map;

/** Key mapping categories; 26.1 requires registered Category objects rather than translation key strings. */
public class PncKeyCategories {
    public static final KeyMapping.Category MAIN = new KeyMapping.Category(Identifier.fromNamespaceAndPath(Names.MOD_ID, "main"));
    public static final KeyMapping.Category UPGRADE_TOGGLES = new KeyMapping.Category(Identifier.fromNamespaceAndPath(Names.MOD_ID, "upgrade_toggles"));
    public static final KeyMapping.Category BLOCK_TRACKER = new KeyMapping.Category(Identifier.fromNamespaceAndPath(Names.MOD_ID, "block_tracker"));

    private static final Map<String, KeyMapping.Category> BY_NAME = Map.of(
            Names.PNEUMATIC_KEYBINDING_CATEGORY_MAIN, MAIN,
            Names.PNEUMATIC_KEYBINDING_CATEGORY_UPGRADE_TOGGLES, UPGRADE_TOGGLES,
            Names.PNEUMATIC_KEYBINDING_CATEGORY_BLOCK_TRACKER, BLOCK_TRACKER
    );

    public static KeyMapping.Category byName(String name) {
        return BY_NAME.getOrDefault(name, MAIN);
    }

    public static void register(RegisterKeyMappingsEvent event) {
        event.registerCategory(MAIN);
        event.registerCategory(UPGRADE_TOGGLES);
        event.registerCategory(BLOCK_TRACKER);
    }
}
