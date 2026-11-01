package net.diaithir.activity;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Players who asked not to be shown. A small JSON list of UUIDs next to the world data. */
public final class OptOut {
    private final Path file;
    private final Set<UUID> hidden = new HashSet<>();

    public OptOut(Path file) {
        this.file = file;
        if (Files.exists(file)) {
            try {
                for (JsonElement e : JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonArray()) {
                    hidden.add(UUID.fromString(e.getAsString()));
                }
            } catch (IOException | RuntimeException ignored) {
                // a damaged file just means nobody has opted out yet
            }
        }
    }

    public boolean isHidden(UUID id) {
        return hidden.contains(id);
    }

    /** @return true when the state changed */
    public boolean set(UUID id, boolean hide) {
        boolean changed = hide ? hidden.add(id) : hidden.remove(id);
        if (changed) save();
        return changed;
    }

    private void save() {
        JsonArray arr = new JsonArray();
        hidden.forEach(u -> arr.add(u.toString()));
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, arr.toString(), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
            // the choice still holds until restart
        }
    }
}
