package net.diaithir.client.updater;

import com.google.gson.Gson;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Packwiz-style pack manifest (clean-room): a version string and a list of files, each with a
 * relative path, a SHA-256 and a download URL. The plan compares it with what is on disk.
 */
public record Manifest(String version, List<Entry> files) {
    public record Entry(String path, String sha256, String url) {}

    public enum Action { ADD, REPLACE }

    public record Step(Action action, Entry entry) {}

    /** Only these top-level folders may be touched by an update. */
    private static final Set<String> ALLOWED_ROOTS = Set.of("mods", "config", "resourcepacks", "shaderpacks");

    public static Manifest parse(String json) {
        Manifest m = new Gson().fromJson(json, Manifest.class);
        if (m == null || m.version == null || m.files == null) throw new IllegalArgumentException("not a pack manifest");
        return m;
    }

    /** A path is safe when it is relative, has no "..", and starts in an allowed folder. */
    public static boolean safe(String rel) {
        if (rel == null || rel.isEmpty() || rel.startsWith("/") || rel.contains("\\") || rel.contains(":")) return false;
        String[] parts = rel.split("/");
        for (String p : parts) if (p.isEmpty() || p.equals("..") || p.equals(".")) return false;
        return parts.length > 1 && ALLOWED_ROOTS.contains(parts[0]);
    }

    /** What must be downloaded, given a map of existing relative path to its SHA-256. Unsafe entries are skipped. */
    public List<Step> plan(Map<String, String> localHashes) {
        List<Step> out = new ArrayList<>();
        for (Entry e : files) {
            if (!safe(e.path) || e.sha256 == null || e.url == null || !e.url.startsWith("https://")) continue;
            String have = localHashes.get(e.path);
            if (have == null) out.add(new Step(Action.ADD, e));
            else if (!have.equalsIgnoreCase(e.sha256)) out.add(new Step(Action.REPLACE, e));
        }
        return out;
    }

    /** Paths on disk (under the managed roots) that the manifest does not list; reported, never deleted. */
    public List<String> unlisted(Set<String> localPaths) {
        Set<String> listed = files.stream().map(Entry::path).collect(Collectors.toSet());
        return localPaths.stream().filter(p -> !listed.contains(p)).sorted().toList();
    }

    static Path resolve(Path root, String rel) {
        Path p = root.resolve(rel).normalize();
        if (!p.startsWith(root.normalize())) throw new IllegalArgumentException("path escapes root: " + rel);
        return p;
    }
}
