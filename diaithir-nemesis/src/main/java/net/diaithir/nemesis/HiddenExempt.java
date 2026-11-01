package net.diaithir.nemesis;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Test accounts listed in config/diaithirnemesis_hidden.json ({"exempt":["name or uuid"]}) are never Shaken. The file is optional and has no default. */
public final class HiddenExempt {
    private static final Path FILE = Path.of("config", "diaithirnemesis_hidden.json");

    private HiddenExempt() {}

    public static boolean exempt(String uuid, String name) {
        if (!Files.isRegularFile(FILE)) return false;
        try (var in = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
            Set<String> ids = new HashSet<>();
            for (JsonElement e : JsonParser.parseReader(in).getAsJsonObject().getAsJsonArray("exempt")) {
                String s = e.getAsString();
                ids.add(s.toLowerCase());
                ids.add(UUID.nameUUIDFromBytes(("OfflinePlayer:" + s).getBytes(StandardCharsets.UTF_8)).toString());
            }
            return ids.contains(uuid.toLowerCase()) || ids.contains(name.toLowerCase());
        } catch (Exception e) {
            return false;
        }
    }
}
