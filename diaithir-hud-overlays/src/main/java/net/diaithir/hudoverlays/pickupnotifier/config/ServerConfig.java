// Ported from Pick Up Notifier (MPL-2.0) https://github.com/Fuzss/pickupnotifier
// Modifications: Puzzles Lib config replaced by ModConfigSpec.
package net.diaithir.hudoverlays.pickupnotifier.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ServerConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue PARTIAL_PICK_UPS;
    public static final ModConfigSpec.BooleanValue BACKPACK_COMPAT;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        PARTIAL_PICK_UPS = b.comment(
                "Collect partial pick-up entries (when there isn't enough room in your inventory) in the log.",
                "Might accidentally log items that have not been picked up, therefore it can be disabled.")
                .define("partialPickUps", true);
        BACKPACK_COMPAT = b.comment(
                "Show entries for items picked up that don't go to the player's inventory. This will enable compatibility with some backpack mods, but might also falsely log items the player never actually receives; depending on the backpack implementation.")
                .define("backpack_integration", true);
        SPEC = b.build();
    }

    private ServerConfig() {}
}
