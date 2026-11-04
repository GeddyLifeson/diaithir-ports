package net.diaithir.crashtools.crashassistant.crash;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.List;
import java.util.stream.Collectors;
import net.diaithir.crashtools.crashassistant.config.CrashToolsConfig;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;

public final class ReportFormatter {
    private ReportFormatter() {}

    public static String summary(CrashParser.Parsed p, List<SuspectAnalyzer.Suspect> suspects) {
        StringBuilder sb = new StringBuilder();
        sb.append("**Exception:** ").append(p.exception()).append('\n');
        if (!p.description().isEmpty()) sb.append("**Description:** ").append(p.description()).append('\n');
        sb.append("**Minecraft:** ").append(p.minecraftVersion()).append(" | **NeoForge:** ").append(p.neoVersion()).append('\n');
        if (!suspects.isEmpty()) {
            sb.append("**Suspects:** ").append(suspects.stream()
                    .map(s -> s.modName() + " (" + s.modId() + ")").collect(Collectors.joining(", "))).append('\n');
        }
        sb.append("```\n");
        p.frames().stream().limit(10).forEach(f -> sb.append("at ").append(f).append('\n'));
        sb.append("```\n");
        return redact(sb.toString());
    }

    public static String fullMarkdown(Path report, CrashParser.Parsed p, List<SuspectAnalyzer.Suspect> suspects) {
        StringBuilder sb = new StringBuilder(summary(p, suspects));
        if (CrashToolsConfig.INCLUDE_MOD_LIST.get()) {
            sb.append("\n<details><summary>Mod list</summary>\n\n");
            try {
                ModList.get().getMods().forEach(m ->
                        sb.append("- ").append(m.getModId()).append(' ').append(m.getVersion()).append('\n'));
            } catch (Throwable ignored) {
            }
            sb.append("\n</details>\n");
        }
        sb.append("\n**JVM args:** `").append(String.join(" ",
                java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments())).append("`\n");
        sb.append("\n<details><summary>Crash report</summary>\n\n```\n");
        try {
            sb.append(String.join("\n", tail(report, CrashToolsConfig.MAX_LOG_LINES.get())));
        } catch (IOException e) {
            sb.append("(unreadable)");
        }
        sb.append("\n```\n</details>\n");
        Path latestLog = FMLPaths.GAMEDIR.get().resolve("logs/latest.log");
        if (Files.isRegularFile(latestLog)) {
            sb.append("\n<details><summary>latest.log (tail)</summary>\n\n```\n");
            try {
                sb.append(String.join("\n", tail(latestLog, CrashToolsConfig.MAX_LOG_LINES.get())));
            } catch (IOException e) {
                sb.append("(unreadable)");
            }
            sb.append("\n```\n</details>\n");
        }
        return redact(sb.toString());
    }

    public static List<String> tail(Path file, int maxLines) throws IOException {
        ArrayDeque<String> lines = new ArrayDeque<>();
        try (RandomAccessFile raf = new RandomAccessFile(file.toFile(), "r")) {
            long pos = raf.length();
            long start = pos;
            int found = 0;
            byte[] buf = new byte[8192];
            while (start > 0 && found <= maxLines) {
                int read = (int) Math.min(buf.length, start);
                start -= read;
                raf.seek(start);
                raf.readFully(buf, 0, read);
                for (int i = read - 1; i >= 0; i--) if (buf[i] == '\n') found++;
                if (start < pos - (long) maxLines * 4096L) break;
            }
            raf.seek(start);
            byte[] data = new byte[(int) (pos - start)];
            raf.readFully(data);
            String text = new String(data, StandardCharsets.UTF_8);
            for (String l : text.split("\r?\n")) {
                lines.addLast(l);
                if (lines.size() > maxLines) lines.removeFirst();
            }
        }
        return List.copyOf(lines);
    }

    public static String redact(String s) {
        if (!CrashToolsConfig.REDACT_PATHS.get()) return s;
        String home = System.getProperty("user.home");
        String user = System.getProperty("user.name");
        if (home != null && !home.isEmpty()) {
            s = s.replace(home, "~").replace(home.replace('\\', '/'), "~");
        }
        if (user != null && user.length() > 2) {
            s = s.replace("\\" + user + "\\", "\\~\\").replace("/" + user + "/", "/~/");
        }
        return s;
    }
}
