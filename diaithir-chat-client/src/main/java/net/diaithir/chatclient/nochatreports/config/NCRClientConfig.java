// Ported from No Chat Reports (WTFPL) https://github.com/Aizistral-Studios/No-Chat-Reports
// Modifications: JSON config replaced by NeoForge ModConfigSpec (CLIENT); UI-only options (icons, buttons) removed.
package net.diaithir.chatclient.nochatreports.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class NCRClientConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLE_MOD;
    public static final ModConfigSpec.EnumValue<SigningMode> DEFAULT_SIGNING_MODE;
    public static final ModConfigSpec.BooleanValue HIDE_INSECURE_INDICATORS;
    public static final ModConfigSpec.BooleanValue HIDE_MODIFIED_INDICATORS;
    public static final ModConfigSpec.BooleanValue HIDE_SYSTEM_INDICATORS;
    public static final ModConfigSpec.BooleanValue HIDE_WARNING_TOAST;
    public static final ModConfigSpec.BooleanValue ALWAYS_HIDE_REPORT_BUTTON;
    public static final ModConfigSpec.BooleanValue DISABLE_TELEMETRY;
    public static final ModConfigSpec.BooleanValue DEMAND_ON_SERVER;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        ENABLE_MOD = b.comment("Enable the networking-related signing suppression. Requires reconnect if toggled mid-session")
                .define("enableMod", true);
        DEFAULT_SIGNING_MODE = b.comment("NEVER = never sign; ALWAYS = sign when possible; PROMPT/ON_DEMAND = sign only when server demands (both enable signing automatically here)")
                .defineEnum("defaultSigningMode", SigningMode.PROMPT);
        HIDE_INSECURE_INDICATORS = b.comment("Hide the 'unverified message' trust indicator")
                .define("hideInsecureMessageIndicators", true);
        HIDE_MODIFIED_INDICATORS = b.comment("Hide the 'modified message' trust indicator")
                .define("hideModifiedMessageIndicators", true);
        HIDE_SYSTEM_INDICATORS = b.comment("Hide the system message indicator bar")
                .define("hideSystemMessageIndicators", true);
        HIDE_WARNING_TOAST = b.comment("Hide the 'chat messages can't be verified' toast")
                .define("hideWarningToast", true);
        ALWAYS_HIDE_REPORT_BUTTON = b.comment("Always hide the report button in the social interactions screen")
                .define("alwaysHideReportButton", false);
        DISABLE_TELEMETRY = b.comment("Disable Mojang telemetry")
                .define("disableTelemetry", true);
        DEMAND_ON_SERVER = b.comment("Refuse to join servers that are not known to be secure")
                .define("demandOnServer", false);
        SPEC = b.build();
    }

    public enum SigningMode { NEVER, ALWAYS, PROMPT, ON_DEMAND }

    private NCRClientConfig() {}
}
