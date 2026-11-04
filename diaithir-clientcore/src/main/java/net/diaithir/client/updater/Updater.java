package net.diaithir.client.updater;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Clean-room pack updater. It checks a manifest and downloads changed files into
 * {@code <game>/diaithir-update/staged/}, verifying each SHA-256 before it is kept. It does not
 * overwrite live mods (a running JVM cannot swap its own jars); the launcher or the user moves
 * the staged tree over the game folder, which {@link #stagedCount} reports.
 */
public final class Updater {
    private static final String[] ROOTS = {"mods", "config", "resourcepacks", "shaderpacks"};

    private final Path gameDir;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).followRedirects(HttpClient.Redirect.NORMAL).build();

    public Updater(Path gameDir) {
        this.gameDir = gameDir;
    }

    public Manifest fetch(String manifestUrl) throws IOException, InterruptedException {
        if (!manifestUrl.startsWith("https://")) throw new IOException("manifest url must be https");
        HttpResponse<String> r = http.send(HttpRequest.newBuilder(URI.create(manifestUrl)).timeout(Duration.ofSeconds(20)).build(),
                HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() != 200) throw new IOException("manifest HTTP " + r.statusCode());
        return Manifest.parse(r.body());
    }

    public Map<String, String> localHashes() throws IOException {
        Map<String, String> out = new HashMap<>();
        for (String root : ROOTS) {
            Path dir = gameDir.resolve(root);
            if (!Files.isDirectory(dir)) continue;
            try (Stream<Path> s = Files.walk(dir)) {
                for (Path p : (Iterable<Path>) s.filter(Files::isRegularFile)::iterator) {
                    out.put(gameDir.relativize(p).toString().replace('\\', '/'), sha256(p));
                }
            }
        }
        return out;
    }

    /** Downloads the plan's files to the staging folder. Returns how many were kept after hash verification. */
    public int stage(List<Manifest.Step> plan) throws IOException, InterruptedException {
        Path root = gameDir.resolve("diaithir-update").resolve("staged");
        int kept = 0;
        for (Manifest.Step step : plan) {
            Path target = Manifest.resolve(root, step.entry().path());
            Files.createDirectories(target.getParent());
            Path tmp = target.resolveSibling(target.getFileName() + ".part");
            HttpResponse<InputStream> r = http.send(HttpRequest.newBuilder(URI.create(step.entry().url())).timeout(Duration.ofMinutes(5)).build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (r.statusCode() != 200) {
                r.body().close();
                continue;
            }
            try (InputStream in = r.body()) {
                Files.copy(in, tmp, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            if (sha256(tmp).equalsIgnoreCase(step.entry().sha256())) {
                Files.move(tmp, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                kept++;
            } else {
                Files.deleteIfExists(tmp);
            }
        }
        return kept;
    }

    public long stagedCount() throws IOException {
        Path root = gameDir.resolve("diaithir-update").resolve("staged");
        if (!Files.isDirectory(root)) return 0;
        try (Stream<Path> s = Files.walk(root)) {
            return s.filter(Files::isRegularFile).count();
        }
    }

    static String sha256(Path p) throws IOException {
        try (InputStream in = Files.newInputStream(p)) {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buf = new byte[65536];
            int n;
            while ((n = in.read(buf)) > 0) md.update(buf, 0, n);
            return HexFormat.of().formatHex(md.digest());
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
