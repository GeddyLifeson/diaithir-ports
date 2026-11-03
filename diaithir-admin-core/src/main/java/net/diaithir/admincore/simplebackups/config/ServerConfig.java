// Ported from Simple Backups (Apache-2.0) https://github.com/ChaoticTrials/SimpleBackups
// Modifications: static final ModConfigSpec values.
package net.diaithir.admincore.simplebackups.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ServerConfig {
    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.BooleanValue COMMANDS_CHEATS_DISABLED;
    private static final ModConfigSpec.BooleanValue MESSAGES_FOR_EVERYONE;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        COMMANDS_CHEATS_DISABLED = b.comment("Should commands work without cheats enabled? Mainly recommended for single player, otherwise all users on servers can trigger commands.")
                .define("commandsCheatsDisabled", false);
        MESSAGES_FOR_EVERYONE = b.comment("Should all users receive the backup message? Disable to only have users with permission level 2 and higher receive them.")
                .define("messagesForEveryone", true);
        SPEC = b.build();
    }

    private ServerConfig() {}

    public static boolean commandsCheatsDisabled() {
        return COMMANDS_CHEATS_DISABLED.get();
    }

    public static boolean messagesForEveryone() {
        return MESSAGES_FOR_EVERYONE.get();
    }
}
