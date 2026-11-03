// Ported from Simple Backups (Apache-2.0) https://github.com/ChaoticTrials/SimpleBackups
// Modifications: static final ModConfigSpec values; ZSTD/SBK/mod-compat options removed; timer helpers fixed to return minutes.
package net.diaithir.admincore.simplebackups.config;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class CommonConfig {
    private static final String JVM_PROP_DISABLE_BACKUPS = "simplebackups.disableBackups";
    private static final String DEFAULT_DISK_SIZE = "25 GB";

    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.BooleanValue ENABLED;
    private static final ModConfigSpec.EnumValue<BackupType> BACKUP_TYPE;
    private static final ModConfigSpec.BooleanValue SAVE_ALL;
    private static final ModConfigSpec.BooleanValue CAPTURE_LATEST_LOG;
    private static final ModConfigSpec.IntValue FULL_BACKUP_TIMER;
    private static final ModConfigSpec.IntValue BACKUP_CHAINS_TO_KEEP;
    private static final ModConfigSpec.IntValue TIMER;
    private static final ModConfigSpec.IntValue COMPRESSION_LEVEL;
    private static final ModConfigSpec.BooleanValue SEND_MESSAGES;
    private static final ModConfigSpec.ConfigValue<String> MAX_DISK_SIZE;
    private static final ModConfigSpec.ConfigValue<String> OUTPUT_PATH;
    private static final ModConfigSpec.BooleanValue PRE_COPY;
    private static final ModConfigSpec.BooleanValue NO_PLAYER_BACKUPS;
    private static final ModConfigSpec.IntValue NO_PLAYER_BACKUP_COUNT;
    private static final ModConfigSpec.IntValue NO_PLAYER_BACKUP_TIMER;
    private static final ModConfigSpec.BooleanValue CREATE_SUB_DIRS;
    private static final ModConfigSpec.BooleanValue USE_TICK_COUNTER;
    private static final ModConfigSpec.BooleanValue COLLECT_ERRORS;
    private static final ModConfigSpec.BooleanValue DELETE_UNFINISHED_BACKUP;
    private static final ModConfigSpec.BooleanValue IGNORE_TEMP_FILES;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> IGNORED_PATHS;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> IGNORED_FILES;
    private static final ModConfigSpec.ConfigValue<String> IGNORED_FILES_REGEX;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        ENABLED = b.comment("If set false, no backups are being made.")
                .define("enabled", true);
        BACKUP_TYPE = b.comment("Defines the backup type.",
                        "- FULL_BACKUPS - every backup is a full backup.",
                        "- INCREMENTAL  - backup only files changed since the last backup",
                        "- DIFFERENTIAL - backup only files changed since the last full backup")
                .defineEnum("backupType", BackupType.INCREMENTAL);
        SAVE_ALL = b.comment("Should a save-all be forced before backup?")
                .define("saveAll", true);
        CAPTURE_LATEST_LOG = b.comment("Should log lines written to logs/latest.log during backup be saved as a .log file alongside the backup archive?")
                .define("captureLatestLog", true);
        FULL_BACKUP_TIMER = b.comment("How often should a full backup be created if only modified files should be saved? This creates a full backup when x minutes are over and the next backup needs to be done. Once a year is default.")
                .defineInRange("fullBackupTimer", 525960, 1, 5259600);
        BACKUP_CHAINS_TO_KEEP = b.comment("The max amount of backup chains to keep.")
                .defineInRange("backupChainsToKeep", 10, 1, Short.MAX_VALUE);
        TIMER = b.comment("The time between two backups in minutes", "5 = each 5 minutes", "60 = each hour", "1440 = each day")
                .defineInRange("timer", 120, 1, Short.MAX_VALUE);
        COMPRESSION_LEVEL = b.comment("Compression level:",
                        "  0  = no compression (low CPU usage, larger files)",
                        "  9  = maximum compression (high CPU usage, smaller files)",
                        "  -1 = default; balances speed and compression (recommended)")
                .defineInRange("compressionLevel", -1, -1, 9);
        SEND_MESSAGES = b.comment("Should message be sent when backup is in the making?")
                .define("sendMessages", true);
        MAX_DISK_SIZE = b.comment("The max size of storage the backup folder. If it takes more storage, old files will be deleted.",
                        "Needs to be written as <number><space><storage type>",
                        "Valid storage types: B, KB, MB, GB, TB")
                .define("maxDiskSize", DEFAULT_DISK_SIZE, o -> o instanceof String s && parseSize(s) >= 0);
        OUTPUT_PATH = b.comment("Used to define the output path.")
                .define("outputPath", "simplebackups");
        PRE_COPY = b.comment("Copy the world to a temporary directory before zipping.",
                        "Disabling this saves temporary disk space (~world size during backup) and reduces",
                        "SSD write cycles, but risks inconsistent backups on large worlds.")
                .define("preCopy", true);
        NO_PLAYER_BACKUPS = b.comment("Create backups, even if nobody is online")
                .define("noPlayerBackups", false);
        NO_PLAYER_BACKUP_COUNT = b.comment("How many backups should be created after the last player left? Set to 0 to disable.")
                .defineInRange("noPlayerBackupCount", 1, 0, Integer.MAX_VALUE);
        NO_PLAYER_BACKUP_TIMER = b.comment("The time between two backups when no player is online. 0 means the same as the \"timer\" setting.")
                .defineInRange("noPlayerBackupTimer", 0, 0, Integer.MAX_VALUE);
        CREATE_SUB_DIRS = b.comment("Should sub-directories be generated for each world?",
                        "Keep in mind that all configs above, including backupChainsToKeep and maxDiskSize, will be calculated for each sub directory.")
                .define("createSubDirs", true);
        USE_TICK_COUNTER = b.comment("Use an internal tick counter instead of the real world time. The value of the timer will be converted to ticks. When the timer is over, the backup will be created.",
                        "Keep in mind that lagging servers will result in larger gaps between two backups, e.g. 10 TPS in average will result in double the time set between backups.")
                .define("useTickCounter", false);

        b.comment("WARNING This configuration should stay as default if backups are not monitored properly.")
                .push("error_handling");
        COLLECT_ERRORS = b.comment("I/O Errors will be collected and reported. The mod tries to complete the backup with as many files as possible. Otherwise, it will abort after the first error.")
                .define("collectErrors", true);
        DELETE_UNFINISHED_BACKUP = b.comment("If collectErrors is 'false', backups with errors will be deleted immediately to prevent deleting valid backups.")
                .define("deleteUnfinishedBackup", true);
        b.pop();

        b.comment("WARNING Please check your configuration before using permanently.",
                        "The backup system will ignore these paths and files.")
                .push("to_ignore");
        IGNORE_TEMP_FILES = b.comment("Automatically ignore temp files created by NeoForge, found with that pattern: \".+\\d{10}\\.neoforge-tmp$\"")
                .define("ignore_temp_files", true);
        IGNORED_PATHS = b.comment("All directories that should be excluded from backups",
                        "Format: Enter paths relative to the world directory (e.g., 'logs', 'data/cache')",
                        "All files within these directories will also be excluded")
                .defineListAllowEmpty("ignored_paths", List.of(), () -> "", obj -> obj instanceof String);
        IGNORED_FILES = b.comment("Specific files that should be excluded from backups",
                        "Format: Enter complete file paths relative to the world directory (e.g., 'level.dat_old', 'stats/player.json')",
                        "Use this for individual files rather than entire directories")
                .defineListAllowEmpty("ignored_files", List.of(), () -> "", obj -> obj instanceof String);
        IGNORED_FILES_REGEX = b.comment("Regular expression pattern to exclude matching files from backups",
                        "All files with paths matching this pattern will be skipped",
                        "Example: '.*\\.temp$' excludes all files ending with .temp",
                        "Leave empty to disable regex-based file exclusion")
                .define("ignored_files_regex", "");
        b.pop();

        SPEC = b.build();
    }

    private CommonConfig() {}

    /** Parses strings like "25 GB" into bytes; returns -1 if invalid. */
    private static long parseSize(String text) {
        if (text == null) {
            return -1;
        }
        String s = text.trim().toUpperCase(Locale.ROOT);
        String[] units = {"TB", "GB", "MB", "KB", "B"};
        long[] mult = {1L << 40, 1L << 30, 1L << 20, 1L << 10, 1L};
        for (int i = 0; i < units.length; i++) {
            if (s.endsWith(units[i])) {
                String num = s.substring(0, s.length() - units[i].length()).trim();
                try {
                    double v = Double.parseDouble(num);
                    if (v < 0 || Double.isNaN(v) || Double.isInfinite(v)) {
                        return -1;
                    }
                    return (long) (v * mult[i]);
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
        }
        return -1;
    }

    public static boolean isEnabled() {
        return ENABLED.get();
    }

    public static boolean backupsDisabledByJvmArg() {
        return parseBoolean(System.getProperty(JVM_PROP_DISABLE_BACKUPS), false);
    }

    public static int getBackupsToKeep() {
        return BACKUP_CHAINS_TO_KEEP.get();
    }

    /** Timer in minutes. */
    public static long getTimerMinutes(boolean arePlayersOnline) {
        int offline = NO_PLAYER_BACKUP_TIMER.get();
        if (offline == 0) {
            offline = TIMER.get();
        }
        return arePlayersOnline ? TIMER.get() : offline;
    }

    public static long getFullBackupTimerMs() {
        return (long) FULL_BACKUP_TIMER.get() * 60 * 1000;
    }

    public static int getCompressionLevel() {
        return COMPRESSION_LEVEL.get();
    }

    public static long getMaxDiskSize() {
        long bytes = parseSize(MAX_DISK_SIZE.get());
        return bytes < 0 ? parseSize(DEFAULT_DISK_SIZE) : bytes;
    }

    /** Base output directory without any per-world sub directory. */
    public static Path getBaseOutputPath() {
        Path base = Paths.get(OUTPUT_PATH.get());
        try {
            return base.toRealPath();
        } catch (IOException e) {
            return base;
        }
    }

    public static Path getOutputPath(String levelId) {
        Path base = Paths.get(OUTPUT_PATH.get());
        boolean withSubDir = levelId != null && !levelId.isEmpty() && CREATE_SUB_DIRS.get();
        try {
            return withSubDir ? base.toRealPath().resolve(levelId) : base.toRealPath();
        } catch (IOException e) {
            return withSubDir ? base.resolve(levelId) : base;
        }
    }

    public static boolean preCopy() {
        return PRE_COPY.get();
    }

    public static boolean doNoPlayerBackups() {
        return NO_PLAYER_BACKUPS.get();
    }

    public static int noPlayerBackupCount() {
        return NO_PLAYER_BACKUP_COUNT.get();
    }

    public static BackupType backupType() {
        return BACKUP_TYPE.get();
    }

    public static boolean saveAll() {
        return SAVE_ALL.get();
    }

    public static boolean captureLatestLog() {
        return CAPTURE_LATEST_LOG.get();
    }

    public static boolean sendMessages() {
        return SEND_MESSAGES.get();
    }

    public static boolean collectErrors() {
        return COLLECT_ERRORS.get();
    }

    public static boolean deleteUnfinishedBackup() {
        return DELETE_UNFINISHED_BACKUP.get();
    }

    public static boolean ignoreTempFiles() {
        return IGNORE_TEMP_FILES.get();
    }

    public static List<Path> getIgnoredPaths() {
        List<Path> paths = new ArrayList<>();
        for (String path : IGNORED_PATHS.get()) {
            if (!path.isEmpty()) {
                paths.add(Path.of(path));
            }
        }
        return paths;
    }

    public static List<Path> getIgnoredFiles() {
        List<Path> paths = new ArrayList<>();
        for (String path : IGNORED_FILES.get()) {
            if (!path.isEmpty()) {
                paths.add(Path.of(path));
            }
        }
        return paths;
    }

    public static String getIgnoredFilesRegex() {
        return IGNORED_FILES_REGEX.get();
    }

    public static boolean useTickCounter() {
        return USE_TICK_COUNTER.get();
    }

    private static boolean parseBoolean(String value, boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "true", "1", "yes", "y", "on" -> true;
            case "false", "0", "no", "n", "off" -> false;
            default -> defaultValue;
        };
    }
}
