// Ported from Simple Backups (Apache-2.0) https://github.com/ChaoticTrials/SimpleBackups
// Modifications: tracks wall-clock times separately from the scheduling clock (fixes tick-counter mode); merge flag moved out of saved data.
package net.diaithir.admincore.simplebackups;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.diaithir.admincore.simplebackups.config.CommonConfig;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class BackupData extends SavedData {

    private long lastSaved;
    private long lastSavedWall;
    private long lastFullBackup;
    private boolean paused;
    private boolean usesTickCounter;
    private int backupsSinceLastPlayerJoined;

    public static final Codec<BackupData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.LONG.fieldOf("lastSaved").forGetter(BackupData::getLastSaved),
                    Codec.LONG.fieldOf("lastSavedWall").forGetter(BackupData::getLastSavedWall),
                    Codec.LONG.fieldOf("lastFullBackup").forGetter(BackupData::getLastFullBackup),
                    Codec.BOOL.fieldOf("paused").forGetter(BackupData::isPaused),
                    Codec.BOOL.fieldOf("usesTickCounter").forGetter(BackupData::usesTickCounter),
                    Codec.INT.fieldOf("backupsSinceLastPlayerJoined").forGetter(BackupData::backupsSinceLastPlayerJoined)
            )
            .apply(instance, BackupData::new));

    private static final SavedDataType<BackupData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("diaithiradmincore", "simplebackups"),
            ctx -> new BackupData(0, 0, 0, false, CommonConfig.useTickCounter(), 0),
            ctx -> CODEC);

    private BackupData(long lastSaved, long lastSavedWall, long lastFullBackup, boolean paused, boolean usesTickCounter, int backupsSinceLastPlayerJoined) {
        this.lastSaved = lastSaved;
        this.lastSavedWall = lastSavedWall;
        this.lastFullBackup = lastFullBackup;
        this.paused = paused;
        this.usesTickCounter = usesTickCounter;
        this.backupsSinceLastPlayerJoined = backupsSinceLastPlayerJoined;
    }

    public static BackupData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
        this.setDirty();
    }

    public boolean isPaused() {
        return this.paused;
    }

    /** Time of the last backup on the scheduling clock (ticks if tick counter is used, otherwise millis). */
    public long getLastSaved() {
        return this.lastSaved;
    }

    public void setLastSaved(long lastSaved) {
        this.lastSaved = lastSaved;
        this.setDirty();
    }

    /** Wall-clock millis of the last backup (used for file modification filtering). */
    public long getLastSavedWall() {
        return this.lastSavedWall;
    }

    /** Wall-clock millis of the last full backup. */
    public long getLastFullBackup() {
        return this.lastFullBackup;
    }

    public void markBackup(long schedulingTime, long wallTime, boolean fullBackup) {
        this.lastSaved = schedulingTime;
        this.lastSavedWall = wallTime;
        if (fullBackup) {
            this.lastFullBackup = wallTime;
        }
        this.setDirty();
    }

    public int backupsSinceLastPlayerJoined() {
        return this.backupsSinceLastPlayerJoined;
    }

    public void incrementBackupsSinceLastPlayerJoined() {
        this.backupsSinceLastPlayerJoined++;
        this.setDirty();
    }

    public void resetBackupsSinceLastPlayerJoined() {
        if (this.backupsSinceLastPlayerJoined == 0) {
            return;
        }
        this.backupsSinceLastPlayerJoined = 0;
        this.setDirty();
    }

    public boolean usesTickCounter() {
        return this.usesTickCounter;
    }

    public void setUsesTickCounter(boolean usesTickCounter) {
        this.usesTickCounter = usesTickCounter;
        this.setDirty();
    }
}
