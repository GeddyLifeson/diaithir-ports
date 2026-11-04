package net.diaithir.crashtools.simplebackups;

import java.nio.file.Path;
import java.util.List;

public record BackupResult(Path outputFile, long fileSize, List<Path> errors) {

    public BackupResult(Path outputFile, long fileSize) {
        this(outputFile, fileSize, List.of());
    }

    public boolean hasErrors() {
        return !this.errors.isEmpty();
    }
}
