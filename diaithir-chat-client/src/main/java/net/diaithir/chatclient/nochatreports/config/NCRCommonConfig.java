// Ported from No Chat Reports (WTFPL) https://github.com/Aizistral-Studios/No-Chat-Reports
// Modifications: JSON config replaced by NeoForge ModConfigSpec (COMMON); same options.
package net.diaithir.chatclient.nochatreports.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class NCRCommonConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue DEMAND_ON_CLIENT;
    public static final ModConfigSpec.ConfigValue<String> DEMAND_ON_CLIENT_MESSAGE;
    public static final ModConfigSpec.BooleanValue CONVERT_TO_GAME_MESSAGE;
    public static final ModConfigSpec.BooleanValue ADD_QUERY_DATA;
    public static final ModConfigSpec.BooleanValue ENABLE_DEBUG_LOG;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        DEMAND_ON_CLIENT = b.comment("Kick clients that do not have No Chat Reports installed")
                .define("demandOnClient", false);
        DEMAND_ON_CLIENT_MESSAGE = b.comment("Kick message shown when demandOnClient is true")
                .define("demandOnClientMessage",
                        "You do not have No Chat Reports, and this server is configured to require it on client!");
        CONVERT_TO_GAME_MESSAGE = b.comment("Convert player chat messages to unsigned system messages")
                .define("convertToGameMessage", true);
        ADD_QUERY_DATA = b.comment("Add 'preventsChatReports' to server status pings")
                .define("addQueryData", true);
        ENABLE_DEBUG_LOG = b.comment("Log extra debug information about chat and signatures")
                .define("enableDebugLog", false);
        SPEC = b.build();
    }

    private NCRCommonConfig() {}
}
