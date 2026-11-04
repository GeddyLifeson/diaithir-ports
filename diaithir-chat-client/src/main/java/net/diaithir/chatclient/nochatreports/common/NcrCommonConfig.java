// Ported from No Chat Reports (WTFPL) https://github.com/Aizistral-Studios/No-Chat-Reports
// Modifications: NCR-Common.json replaced with a ModConfigSpec; addQueryData dropped together with the server-list icon feature
package net.diaithir.chatclient.nochatreports.common;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class NcrCommonConfig {
    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.BooleanValue DEMAND_ON_CLIENT;
    private static final ModConfigSpec.ConfigValue<String> DEMAND_ON_CLIENT_MESSAGE;
    private static final ModConfigSpec.BooleanValue CONVERT_TO_GAME_MESSAGE;
    private static final ModConfigSpec.BooleanValue ENABLE_DEBUG_LOG;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        DEMAND_ON_CLIENT = b.comment("Kick clients that send a chat session (i.e. clients that do not suppress signing).")
                .define("demandOnClient", false);
        DEMAND_ON_CLIENT_MESSAGE = b.comment("Disconnect message used when demandOnClient is enabled.")
                .define("demandOnClientMessage",
                        "You do not have No Chat Reports, and this server is configured to require it on client!");
        CONVERT_TO_GAME_MESSAGE = b.comment("Convert all player chat messages to system messages (they cannot be reported).")
                .define("convertToGameMessage", true);
        ENABLE_DEBUG_LOG = b.comment("Enable verbose debug logging.")
                .define("enableDebugLog", false);
        SPEC = b.build();
    }

    private NcrCommonConfig() {
    }

    public static boolean demandOnClient() {
        return DEMAND_ON_CLIENT.get();
    }

    public static String demandOnClientMessage() {
        return DEMAND_ON_CLIENT_MESSAGE.get();
    }

    public static boolean convertToGameMessage() {
        return CONVERT_TO_GAME_MESSAGE.get();
    }

    public static boolean enableDebugLog() {
        return ENABLE_DEBUG_LOG.get();
    }
}
