// Ported from Simple Backups (Apache-2.0) https://github.com/ChaoticTrials/SimpleBackups
// Modifications: merged CompressionBase+ZipCompression (ZIP only, no zstd/sbk/commons-compress); errors now propagate to BackupThread; regex precompiled.
package net.diaithir.admincore.simplebackups;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileStore;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class BackupCompressor {

    /**
     * Settings for a compression run.
     *
     * @param compressionLevel Deflater level 0-9 (-1 for default)
     * @param excludePatterns  regex patterns matched against the relative path (forward slashes)
     */
    public record Options(int compressionLevel, List<String> excludePatterns) {
        public Options {
            excludePatterns = excludePatterns == null ? List.of() : List.copyOf(excludePatterns);
        }

        public static Options defaults() {
            return new Options(Deflater.DEFAULT_COMPRESSION, List.of());
        }
    }

    private BackupCompressor() {
    }

    /**
     * Compresses the given source directory into a ZIP file at {@code target}.
     * Errors are propagated to the caller (BackupThread).
     */
    public static void compress(Path sourceDir, Path target, Options options) throws IOException {
        if (!Files.isDirectory(sourceDir)) {
            throw new FileNotFoundException("Backup source is not a directory: " + sourceDir);
        }

        List<Pattern> excludes = compilePatterns(options.excludePatterns());

        Path absTarget = target.toAbsolutePath().normalize();
        Path parent = absTarget.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
            checkFreeSpace(sourceDir, parent);
        }

        // Write to a temp file first so a failed backup never leaves a corrupt archive.
        Path tmp = absTarget.resolveSibling(absTarget.getFileName() + ".tmp");
        try {
            writeZip(sourceDir, tmp, absTarget, options.compressionLevel(), excludes);
            Files.move(tmp, absTarget, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException | RuntimeException e) {
            try {
                Files.deleteIfExists(tmp);
            } catch (IOException ignored) {
                // best effort cleanup
            }
            throw e;
        }
    }

    private static void writeZip(Path sourceDir, Path tmp, Path finalTarget, int level, List<Pattern> excludes)
            throws IOException {
        final Path root = sourceDir.toAbsolutePath().normalize();

        try (ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(tmp)))) {
            zip.setLevel(level < -1 || level > 9 ? Deflater.DEFAULT_COMPRESSION : level);

            Files.walkFileTree(root, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (dir.equals(root)) {
                        return FileVisitResult.CONTINUE;
                    }
                    String rel = relativize(root, dir);
                    if (isExcluded(rel, excludes) || isSelf(dir)) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    ZipEntry entry = new ZipEntry(rel + "/");
                    entry.setTime(attrs.lastModifiedTime().toMillis());
                    zip.putNextEntry(entry);
                    zip.closeEntry();
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (!attrs.isRegularFile() || isSelf(file)) {
                        return FileVisitResult.CONTINUE;
                    }
                    String rel = relativize(root, file);
                    if (isExcluded(rel, excludes)) {
                        return FileVisitResult.CONTINUE;
                    }
                    // Never archive the output file itself.
                    if (file.toAbsolutePath().normalize().equals(finalTarget)) {
                        return FileVisitResult.CONTINUE;
                    }

                    ZipEntry entry = new ZipEntry(rel);
                    entry.setTime(attrs.lastModifiedTime().toMillis());
                    try (InputStream in = new BufferedInputStream(Files.newInputStream(file))) {
                        zip.putNextEntry(entry);
                        in.transferTo(zip);
                        zip.closeEntry();
                    } catch (NoSuchFileException e) {
                        // File vanished mid-backup (e.g. temp file); skip it.
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    if (exc instanceof NoSuchFileException) {
                        return FileVisitResult.CONTINUE;
                    }
                    throw exc;
                }

                private boolean isSelf(Path p) {
                    Path abs = p.toAbsolutePath().normalize();
                    return abs.equals(tmp.toAbsolutePath().normalize()) || abs.equals(finalTarget);
                }
            });
        }
    }

    private static void checkFreeSpace(Path sourceDir, Path targetParent) throws IOException {
        long size = directorySize(sourceDir);
        FileStore store = Files.getFileStore(targetParent);
        long usable = store.getUsableSpace();
        // ZIP is rarely larger than the source; require at least the raw size as headroom.
        if (usable < size) {
            throw new IOException("Not enough free space for backup: need ~" + size + " bytes, have " + usable);
        }
    }

    private static long directorySize(Path dir) throws IOException {
        final long[] total = {0L};
        Files.walkFileTree(dir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                total[0] += attrs.size();
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                return FileVisitResult.CONTINUE;
            }
        });
        return total[0];
    }

    private static List<Pattern> compilePatterns(List<String> raw) {
        List<Pattern> out = new ArrayList<>(raw.size());
        for (String s : raw) {
            if (s == null || s.isBlank()) {
                continue;
            }
            try {
                out.add(Pattern.compile(s));
            } catch (PatternSyntaxException e) {
                throw new IllegalArgumentException("Invalid backup exclude pattern: " + s, e);
            }
        }
        return out;
    }

    private static boolean isExcluded(String relPath, List<Pattern> excludes) {
        for (Pattern p : excludes) {
            if (p.matcher(relPath).matches()) {
                return true;
            }
        }
        return false;
    }

    private static String relativize(Path root, Path p) {
        return root.relativize(p.toAbsolutePath().normalize()).toString().replace('\\', '/');
    }

    /**
     * Deletes old backups in {@code dir} so that at most {@code keep} ZIP files remain
     * (oldest removed first). A value &lt;= 0 disables pruning.
     */
    public static void prune(Path dir, int keep) throws IOException {
        if (keep <= 0 || !Files.isDirectory(dir)) {
            return;
        }
        List<Path> zips;
        try (var stream = Files.list(dir)) {
            zips = new ArrayList<>(stream
                    .filter(p -> p.getFileName().toString().endsWith(".zip"))
                    .toList());
        }
        zips.sort(Comparator.comparingLong((Path p) -> {
            try {
                return Files.getLastModifiedTime(p).toMillis();
            } catch (IOException e) {
                return 0L;
            }
        }));
        int excess = zips.size() - keep;
        for (int i = 0; i < excess; i++) {
            Files.deleteIfExists(zips.get(i));
        }
    }

    /** Convenience for resolving a path string relative to the current working directory. */
    public static Path resolve(String path) {
        return Paths.get(path).toAbsolutePath().normalize();
    }
}
