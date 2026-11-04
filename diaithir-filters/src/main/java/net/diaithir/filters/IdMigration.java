package net.diaithir.filters;

import java.util.Map;

/**
 * Old FTB Filter System registry names mapped to ours. No Remap hook is wired here, so this is a documented static map:
 * a world migrator or datafixer in another module should read it as {@code old -> new}.
 */
public final class IdMigration {
    public static final Map<String, String> ITEMS = Map.of(
            "ftbfiltersystem:smart_filter", "diaithirfilters:filter",
            "ftbfiltersystem:filter", "diaithirfilters:filter",
            "ftbfiltersystem:filter_card", "diaithirfilters:filter_card");
    public static final Map<String, String> MENUS = Map.of(
            "ftbfiltersystem:smart_filter", "diaithirfilters:filter");
    public static final Map<String, String> COMPONENTS = Map.of(
            "ftbfiltersystem:filter", "diaithirfilters:filter");

    private IdMigration() {
    }

    public static String item(String old) {
        return ITEMS.getOrDefault(old, old);
    }
}
