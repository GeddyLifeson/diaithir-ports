package net.diaithir.worldsys.questbook;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Reads quest JSON from every datapack on server start and after /reload. Bad files are logged and skipped. */
public final class QuestLoader {
    private static final Logger LOG = LoggerFactory.getLogger("diaithir-questbook");
    private static volatile Map<String, Quest> quests = new LinkedHashMap<>();

    private QuestLoader() {}

    public static void init() {
        NeoForge.EVENT_BUS.addListener((ServerStartedEvent e) -> load(e.getServer()));
        NeoForge.EVENT_BUS.addListener((OnDatapackSyncEvent e) -> {
            if (e.getPlayer() == null) load(e.getPlayerList().getServer());
        });
    }

    public static List<Quest> all() {
        return new ArrayList<>(quests.values());
    }

    public static Quest byId(String id) {
        return quests.get(id);
    }

    static void load(MinecraftServer server) {
        Map<String, Quest> out = new LinkedHashMap<>();
        Map<Identifier, Resource> found = server.getResourceManager().listResources("questbook", id -> id.getPath().endsWith(".json"));
        List<Identifier> ids = new ArrayList<>(found.keySet());
        ids.sort(Identifier::compareTo);
        for (Identifier file : ids) {
            try (Reader r = found.get(file).openAsReader()) {
                Quest q = parse(file, JsonParser.parseReader(r).getAsJsonObject());
                if (out.putIfAbsent(q.id(), q) != null) LOG.warn("Duplicate quest id {} in {}", q.id(), file);
            } catch (Exception ex) {
                LOG.warn("Skipping quest file {}: {}", file, ex.toString());
            }
        }
        quests = out;
        LOG.info("Loaded {} quests", out.size());
    }

    static Quest parse(Identifier file, JsonObject j) {
        String path = file.getPath();
        String id = j.has("id") ? j.get("id").getAsString() : path.substring(path.lastIndexOf('/') + 1, path.length() - 5);
        List<Quest.Objective> obj = new ArrayList<>();
        for (JsonElement e : j.getAsJsonArray("objectives")) {
            JsonObject o = e.getAsJsonObject();
            obj.add(new Quest.Objective(Quest.Task.valueOf(o.get("type").getAsString().toUpperCase()), o.get("target").getAsString(),
                o.has("amount") ? Math.max(1, o.get("amount").getAsInt()) : 1));
        }
        if (obj.isEmpty()) throw new IllegalArgumentException("no objectives");
        List<Quest.Reward> rew = new ArrayList<>();
        if (j.has("rewards")) {
            for (JsonElement e : j.getAsJsonArray("rewards")) {
                JsonObject o = e.getAsJsonObject();
                rew.add(new Quest.Reward(o.get("type").getAsString(), o.has("value") ? o.get("value").getAsString() : "", o.has("amount") ? o.get("amount").getAsInt() : 1));
            }
        }
        List<String> req = new ArrayList<>();
        if (j.has("requires")) {
            JsonElement r = j.get("requires");
            if (r.isJsonArray()) for (JsonElement e : (JsonArray) r) req.add(e.getAsString());
            else req.add(r.getAsString());
        }
        return new Quest(id, j.has("title") ? j.get("title").getAsString() : id, j.has("category") ? j.get("category").getAsString() : "general",
            j.has("description") ? j.get("description").getAsString() : "", req, j.has("min_points") ? j.get("min_points").getAsInt() : 0,
            obj, rew, j.has("manual") && j.get("manual").getAsBoolean(), j.has("repeatable") && j.get("repeatable").getAsBoolean(),
            j.has("cooldown_seconds") ? j.get("cooldown_seconds").getAsLong() : 0L);
    }
}
