package net.diaithir.crashtools.simplebackups.compression;

import net.diaithir.crashtools.simplebackups.SimpleBackups;
import net.diaithir.crashtools.simplebackups.config.CommonConfig;
import net.diaithir.crashtools.simplebackups.sbk.SbkWriteOptions;
import net.diaithir.crashtools.simplebackups.sbk.SbkWriter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class SbkCompression extends CompressionBase {

    public SbkCompression(FileStore fileStore, boolean doFullBackup, long lastSaved) {
        super(fileStore, doFullBackup, lastSaved);
    }

    @Override
    public String getExtension() {
        return BackupFormat.SBK.getExtension();
    }

    @Override
    public void makeBackup(Path levelName, Path levelPath, Path outputFile) throws IOException {
        List<Path> acceptedFiles = new ArrayList<>();
        Files.walkFileTree(levelPath, new CompressionFileVisitor(levelPath) {

            @Nonnull
            @Override
            protected FileVisitResult visitFile_(@Nonnull Path file, @Nonnull BasicFileAttributes attrs) {
                acceptedFiles.add(file);

                return FileVisitResult.CONTINUE;
            }
        });

        SbkWriteOptions options = SbkWriteOptions.builder()
                .lzmaPreset(this.lzmaPreset())
                .algorithm(CommonConfig.sbkAlgorithm())
                .build();

        SbkWriter.compress(levelPath, acceptedFiles, levelName, outputFile, options,
                (completed, total, path) -> SimpleBackups.LOGGER.debug("SBK [{}/{}] {}", completed, total, path));
    }

    private int lzmaPreset() {
        int lvl = CommonConfig.getCompressionLevel();
        // Map config level [-1, 0..9] to LZMA2 preset [0..9]
        // -1 (DEFAULT_COMPRESSION) maps to preset 3
        if (lvl < 0) {
            return SbkWriteOptions.DEFAULT_PRESET;
        }

        return Math.clamp(lvl, 0, 9);
    }
}
