package net.diaithir.crashtools.crashassistant.crash;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;
import net.neoforged.fml.loading.FMLPaths;

public final class CrashReportLocator {
    private CrashReportLocator() {}

    public static Path crashDir() {
        return FMLPaths.GAMEDIR.get().resolve("crash-reports");
    }

    public static Path stateDir() {
        return FMLPaths.GAMEDIR.get().resolve("crashtools");
    }

    private static Path seenFile() {
        return stateDir().resolve("last_seen.txt");
    }

    private static long lastSeen() {
        try {
            return Long.parseLong(Files.readString(seenFile()).strip());
        } catch (Exception e) {
            return 0L;
        }
    }

    private static void markSeen(long t) {
        try {
            Files.createDirectories(stateDir());
            Files.writeString(seenFile(), Long.toString(t));
        } catch (IOException ignored) {
        }
    }

    public static Optional<Path> findLatest() {
        Path dir = crashDir();
        if (!Files.isDirectory(dir)) return Optional.empty();
        try (Stream<Path> s = Files.list(dir)) {
            return s.filter(p -> p.getFileName().toString().endsWith(".txt"))
                    .max(Comparator.comparingLong(CrashReportLocator::mtime));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public static Optional<Path> findUnseen() {
        Optional<Path> latest = findLatest();
        if (latest.isEmpty()) return latest;
        long m = mtime(latest.get());
        long seen = lastSeen();
        if (seen == 0L && !Files.exists(seenFile())) {
            long now = System.currentTimeMillis();
            if (now - m > 24L * 3600_000L) {
                markSeen(m);
                return Optional.empty();
            }
        }
        if (m <= seen) return Optional.empty();
        markSeen(m);
        return latest;
    }

    private static long mtime(Path p) {
        try {
            return Files.getLastModifiedTime(p).toMillis();
        } catch (IOException e) {
            return 0L;
        }
    }
}
