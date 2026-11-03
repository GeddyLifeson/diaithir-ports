package net.diaithir.inventoryui.inventoryessentials.config;

import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class ServerConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ALLOW_SORT;
    public static final ModConfigSpec.IntValue SORT_COOLDOWN_TICKS;
    public static final ModConfigSpec.IntValue MAX_SORTS_PER_SECOND;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> BLACKLIST_MENUS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> BLACKLIST_BLOCK_ENTITIES;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        ALLOW_SORT = b.define("allowSort", true);
        SORT_COOLDOWN_TICKS = b.defineInRange("sortCooldownTicks", 5, 0, 1200);
        MAX_SORTS_PER_SECOND = b.defineInRange("maxSortsPerSecond", 4, 1, 20);
        BLACKLIST_MENUS = b.defineListAllowEmpty("sortBlacklistMenus", List.of(), () -> "", o -> o instanceof String);
        BLACKLIST_BLOCK_ENTITIES = b.defineListAllowEmpty("sortBlacklistBlockEntities", List.of(), () -> "", o -> o instanceof String);
        SPEC = b.build();
    }

    private ServerConfig() {}
}
