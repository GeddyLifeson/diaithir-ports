// Ported from Simple Backups (Apache-2.0) https://github.com/ChaoticTrials/SimpleBackups
// Modifications: ZIP-only (format field dropped), reader closed properly, child removal helper.
package net.diaithir.admincore.simplebackups;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import net.diaithir.admincore.simplebackups.config.BackupType;

public class BackupChain {

    public static final String METADATA_FILE = "metadata.json";
    private final Path parentFolder;
    private final Path fullBackup;
    private final List<Path> children;
    private final BackupType backupType;
    private long lastUpdated;

    public BackupChain(Path parentFolder, Path fullBackup, BackupType backupType) {
        this(parentFolder, fullBackup, new ArrayList<>(), backupType, System.currentTimeMillis());
    }

    public BackupChain(Path parentFolder, Path fullBackup, List<Path> children, BackupType backupType, long lastUpdated) {
        this.parentFolder = parentFolder;
        this.fullBackup = fullBackup;
        this.children = children;
        this.backupType = backupType;
        this.lastUpdated = lastUpdated;
    }

    public Path getParentFolder() {
        return this.parentFolder;
    }

    public Path getFullBackup() {
        return this.parentFolder.resolve(this.fullBackup);
    }

    public List<Path> getChildren() {
        return this.children.stream().map(this.parentFolder::resolve).toList();
    }

    public BackupType getBackupType() {
        return this.backupType;
    }

    public Path createChild() {
        int index = this.children.size() + 1;
        Path child = Path.of("child-" + String.format("%04d", index) + SimpleBackups.EXTENSION);
        this.children.add(child);
        this.lastUpdated = System.currentTimeMillis();
        this.writeMetadata();
        return this.parentFolder.resolve(child);
    }

    public void removeChild(Path absoluteFile) {
        if (this.children.removeIf(c -> this.parentFolder.resolve(c).equals(absoluteFile))) {
            this.writeMetadata();
        }
    }

    public long getLastUpdated() {
        return this.lastUpdated;
    }

    public long getFileSize() {
        return StorageSize.getFolderSize(this.parentFolder);
    }

    public void deleteFiles() {
        if (!Files.exists(this.parentFolder)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(this.parentFolder)) {
            walk.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    SimpleBackups.LOGGER.warn("Failed to delete \"{}\"", path, e);
                }
            });
        } catch (IOException e) {
            SimpleBackups.LOGGER.warn("Failed to delete directory tree \"{}\"", this.parentFolder, e);
        }
    }

    public int getSize() {
        return this.children.size() + 1;
    }

    public void writeMetadata() {
        Path meta = this.parentFolder.resolve(METADATA_FILE);
        JsonObject json = new JsonObject();
        json.addProperty("backupType", this.backupType.name());
        json.addProperty("fullBackup", this.fullBackup.toString());
        JsonArray array = new JsonArray();
        for (Path child : this.children) {
            array.add(child.toString());
        }
        json.add("children", array);
        json.addProperty("lastUpdated", this.lastUpdated);
        try {
            Files.writeString(meta, json.toString());
        } catch (IOException e) {
            SimpleBackups.LOGGER.warn("Failed to write metadata to {}", meta, e);
        }
    }

    public static BackupChain readMetadata(Path chainDir) {
        Path meta = chainDir.resolve(METADATA_FILE);
        try (Reader reader = Files.newBufferedReader(meta)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            Path fullBackup = Path.of(json.get("fullBackup").getAsString());
            List<Path> children = new ArrayList<>();
            for (JsonElement child : json.getAsJsonArray("children")) {
                children.add(Path.of(child.getAsString()));
            }
            BackupType type = BackupType.valueOf(json.get("backupType").getAsString());
            long lastUpdated = json.get("lastUpdated").getAsLong();
            return new BackupChain(chainDir, fullBackup, children, type, lastUpdated);
        } catch (IOException | RuntimeException e) {
            SimpleBackups.LOGGER.warn("Failed to read metadata from {}", meta, e);
            return null;
        }
    }
}
