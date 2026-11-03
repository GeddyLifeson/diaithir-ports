// Ported from Simple Backups (Apache-2.0) https://github.com/ChaoticTrials/SimpleBackups
// Modifications: new helper; translatable components with built-in English fallback so vanilla clients without the lang file still see text.
package net.diaithir.admincore.simplebackups;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class Messages {
    public record Msg(String key, String fallback) {}

    private static final String P = "diaithiradmincore.simplebackups.";

    public static final Msg BACKUP_STARTED = new Msg(P + "backup_started", "Backup started...");
    public static final Msg BACKUP_FINISHED = new Msg(P + "backup_finished", "Backup completed in %s (%s | %s)");
    public static final Msg BACKUP_ERRORS = new Msg(P + "backup_errors", "%s error(s) occurred during backup");
    public static final Msg BACKUP_FAILED_DELETE = new Msg(P + "backup_failed_delete", "Backup failed (deleting unfinished file) - please check your log file for more information");
    public static final Msg BACKUP_FAILED_CONTINUE = new Msg(P + "backup_failed_continue", "Backup creation failed early, some files may not be backed up - please check your log file for more information to prevent data loss");
    public static final Msg NOT_ENOUGH_SPACE = new Msg(P + "not_enough_space", "Not enough space on disk to create backup. Aborted!");
    public static final Msg PAUSED = new Msg(P + "backups_paused", "Backups paused");
    public static final Msg UNPAUSED = new Msg(P + "backups_unpaused", "Backups resumed");
    public static final Msg ONLY_MODIFIED = new Msg(P + "commands.only_modified", "Not only modified files are being backed up, please check your configuration file");
    public static final Msg IS_MERGING = new Msg(P + "commands.is_merging", "A merge operation is already in progress");
    public static final Msg MERGE_ERROR = new Msg(P + "commands.error", "There was an error while merging backups: %s");
    public static final Msg MERGE_FINISHED = new Msg(P + "commands.finished", "Merging backups completed successfully at %s");
    public static final Msg UNKNOWN_CHAIN = new Msg(P + "commands.unknown_chain", "Unknown backup chain: %s");

    private Messages() {}

    public static MutableComponent component(Msg msg, Object... args) {
        return Component.translatableWithFallback(msg.key(), msg.fallback(), args);
    }

    public static String plain(Msg msg, Object... args) {
        try {
            return String.format(msg.fallback(), args);
        } catch (RuntimeException e) {
            return msg.fallback();
        }
    }
}
