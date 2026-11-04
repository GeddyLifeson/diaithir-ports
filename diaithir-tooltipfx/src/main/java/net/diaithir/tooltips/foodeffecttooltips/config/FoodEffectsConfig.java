// Ported from Food Effect Tooltips (LGPL-3.0-only) https://github.com/DaFuqs/FoodEffectTooltips
// Modifications: Static ModConfigSpec values instead of Pair/configure instance; same option names and defaults.
package net.diaithir.tooltips.foodeffecttooltips.config;

import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class FoodEffectsConfig {
    public static final ModConfigSpec CONFIG_SPEC;
    public static final ModConfigSpec.BooleanValue USE_AS_WHITELIST_INSTEAD;
    public static final ModConfigSpec.BooleanValue SHOW_SUSPICIOUS_STEW_TOOLTIPS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> BLACKLISTED_ITEM_IDENTIFIERS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> BLACKLISTED_MOD_IDS;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        USE_AS_WHITELIST_INSTEAD = b.define("use_as_whitelists_instead", false);
        SHOW_SUSPICIOUS_STEW_TOOLTIPS = b.define("show_suspicious_stew_tooltips", false);
        BLACKLISTED_ITEM_IDENTIFIERS = b.defineList("blacklisted_item_identifiers", List.of(), () -> "mymod:myid", o -> o instanceof String);
        BLACKLISTED_MOD_IDS = b.defineList("blacklisted_mod_ids",
                List.of("spectrum", "vinery", "farmersdelight", "createfood", "expandeddelight", "frightsdelight", "moredelight", "oceansdelight", "silentsdelight", "ubesdelight"),
                () -> "mymod", o -> o instanceof String);
        CONFIG_SPEC = b.build();
    }

    private FoodEffectsConfig() {}
}
