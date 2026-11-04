package net.diaithir.crashtools.crashassistant.crash;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class CrashParser {
    public record Parsed(String description, String exception, String minecraftVersion, String neoVersion, List<String> frames) {}

    private CrashParser() {}

    public static Parsed parse(Path file) {
        String desc = "", exc = "unknown", mc = "?", neo = "?";
        List<String> frames = new ArrayList<>();
        boolean inTrace = false;
        try (BufferedReader r = Files.newBufferedReader(file)) {
            String line;
            int n = 0;
            while ((line = r.readLine()) != null && n++ < 20000) {
                if (line.startsWith("Description:")) {
                    desc = line.substring(12).strip();
                    inTrace = true;
                    continue;
                }
                if (inTrace && exc.equals("unknown") && !line.isBlank()) {
                    exc = line.strip();
                    continue;
                }
                if (inTrace && line.startsWith("\tat ")) {
                    frames.add(line.strip().substring(3));
                    continue;
                }
                if (line.startsWith("-- ") && !frames.isEmpty()) inTrace = false;
                String t = line.strip();
                if (t.startsWith("Minecraft Version:")) mc = t.substring(18).strip();
                else if (t.startsWith("NeoForge:") || t.startsWith("NeoForge Version:")) neo = t.substring(t.indexOf(':') + 1).strip();
                if (t.startsWith("Caused by:") && inTrace) exc = t;
            }
        } catch (IOException ignored) {
        }
        return new Parsed(desc, exc, mc, neo, frames);
    }
}
