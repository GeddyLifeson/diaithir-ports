package net.diaithir.crashtools.crashassistant.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class CrashToolsConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.BooleanValue SHOW_ON_NEXT_LAUNCH;
    public static final ModConfigSpec.IntValue MAX_LOG_LINES;
    public static final ModConfigSpec.BooleanValue INCLUDE_MOD_LIST;
    public static final ModConfigSpec.BooleanValue REDACT_PATHS;
    public static final ModConfigSpec.BooleanValue ENABLE_UPLOAD;
    public static final ModConfigSpec.ConfigValue<String> UPLOAD_URL;
    public static final ModConfigSpec.IntValue SUSPECT_FRAME_DEPTH;
    public static final ModConfigSpec.BooleanValue AUTO_OPEN_FOLDER;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        ENABLED = b.define("enabled", true);
        SHOW_ON_NEXT_LAUNCH = b.define("showOnNextLaunch", true);
        MAX_LOG_LINES = b.defineInRange("maxLogLines", 500, 10, 20000);
        INCLUDE_MOD_LIST = b.define("includeModList", true);
        REDACT_PATHS = b.define("redactPaths", true);
        ENABLE_UPLOAD = b.define("enableUpload", false);
        UPLOAD_URL = b.define("uploadUrl", "");
        SUSPECT_FRAME_DEPTH = b.defineInRange("suspectFrameDepth", 30, 1, 500);
        AUTO_OPEN_FOLDER = b.define("autoOpenFolderOnCrash", false);
        SPEC = b.build();
    }

    private CrashToolsConfig() {}
}
