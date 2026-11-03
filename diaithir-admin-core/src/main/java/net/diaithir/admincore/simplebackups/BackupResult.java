// Ported from Simple Backups (Apache-2.0) https://github.com/ChaoticTrials/SimpleBackups
// Modifications: none of substance.
package net.diaithir.admincore.simplebackups;

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
