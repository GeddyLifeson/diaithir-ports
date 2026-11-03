package net.diaithir.bosses;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;

/**
 * The Library's rites (diaithir-druidry data/diaithir_druidry/rites/&lt;universe&gt;.json, format rite.v1): one file per universe, read on
 * first use and kept. A rite id is the Library entry id (AR.alaenneirae.E1.powers.000127); the universe is its second part. Each
 * rite becomes a {@link Ritual.Def} the Ascendant altar runs with the same step code as a boss rite. The datapack resource manager
 * is tried first (the shipped way), then the class path (dev runs and the self test).
 */
public final class RiteLibrary {
    private static final Gson GSON = new Gson();
    private static final String NS = "diaithir_druidry";
    private static final Map<String, int[]> HOURS = Map.of("midnight", new int[]{17000, 19000}, "dawn", new int[]{23000, 1000},
        "dusk", new int[]{12000, 13500}, "noon", new int[]{5500, 6500});
    private static final String[] RIDDLE = {"the pale one that wears the first white", "red berries set against the witch", "the one that bleeds when it is cut",
        "the weeper by the water", "kin of the tree that holds up the world", "the gate the Fair Folk keep", "the door-wood, the strong one",
        "the winter king, green when all is grey", "the nut of the one who knows"};
    private static final List<String> WAVE = List.of("minecraft:zombie", "minecraft:skeleton", "minecraft:spider");
    private static final Map<String, Map<String, Rite>> FILES = new HashMap<>();
    /** Set by the server on start so the shipped path can find datapack files. */
    static volatile MinecraftServer server;

    public static final class RStep {
        public String step, item, entity, window, tag, solo;
        public int count = 1, seconds = 60, fighters = 2, radius = 48;
        public List<String> runes = new ArrayList<>();
    }

    public static final class Rite {
        public String id, name, age, school, era, spine, text, planet, dimension;
        public int tier, gate_age;
        public List<RStep> steps = new ArrayList<>();
    }

    private static String universe(String id) {
        String[] p = id.split("\\.");
        return p.length < 2 ? null : p[1].toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]", "_");
    }

    private static InputStream open(String file) {
        String path = "rites/" + file + ".json";
        String dir = System.getProperty("diaithir.rites");
        if (dir != null) {
            try { return java.nio.file.Files.newInputStream(java.nio.file.Path.of(dir, file + ".json")); } catch (java.io.IOException ignored) { }
        }
        MinecraftServer s = server;
        if (s != null) {
            var r = s.getResourceManager().getResource(Identifier.fromNamespaceAndPath(NS, path));
            if (r.isPresent()) {
                try { return r.get().open(); } catch (java.io.IOException ignored) { }
            }
        }
        return RiteLibrary.class.getResourceAsStream("/data/" + NS + "/" + path);
    }

    /** Every rite of one universe file, by id; empty if the file is missing. */
    public static synchronized Map<String, Rite> universeFile(String file) {
        return FILES.computeIfAbsent(file, f -> {
            Map<String, Rite> out = new HashMap<>();
            try (InputStream in = open(f)) {
                if (in == null) return out;
                JsonObject o = GSON.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), JsonObject.class);
                for (var e : o.getAsJsonArray("rites")) {
                    Rite r = GSON.fromJson(e, Rite.class);
                    out.put(r.id, r);
                }
            } catch (Exception ignored) { }
            return out;
        });
    }

    public static Rite get(String id) {
        String u = universe(id);
        return u == null ? null : universeFile(u).get(id);
    }

    public static List<String> universes() {
        List<String> out = new ArrayList<>();
        try (InputStream in = open("_index")) {
            if (in == null) return out;
            JsonObject o = GSON.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), JsonObject.class);
            out.addAll(o.getAsJsonObject("files").keySet());
        } catch (Exception ignored) { }
        return out;
    }

    /** The altar's view of a rite: the step types of boss rites with the entry's own text as the opening line. */
    public static Ritual.Def def(String id) {
        Rite r = get(id);
        if (r == null) return null;
        Ritual.Def d = new Ritual.Def();
        d.boss = "rite:" + id;
        d.name = r.name;
        d.text = r.text;
        for (RStep s : r.steps) {
            Ritual.Step t = new Ritual.Step();
            switch (s.step) {
                case "offer" -> { t.type = "offer"; t.item = s.item; t.count = s.count; t.clue = "Lay " + s.count + " of " + s.item.replace("minecraft:", "").replace('_', ' ') + " upon the stone."; }
                case "feast" -> { t.type = "offer"; t.item = "#" + s.tag; t.count = 1; t.clue = "The altar eats before it fights. Set one feast upon the stone, a dish worth a guest-gift."; }
                case "hunt" -> { t.type = "hunt"; t.mob = s.entity; t.count = s.count; t.clue = "Let " + s.count + " of the " + s.entity.replace("minecraft:", "").replace('_', ' ') + " fall in sight of the altar."; }
                case "stones" -> {
                    t.type = "stones";
                    List<String> riddles = new ArrayList<>();
                    for (String rune : s.runes) {
                        int i = indexOfTree(rune);
                        if (i < 0) return null;
                        t.sequence.add(i);
                        riddles.add(RIDDLE[i]);
                    }
                    t.clue = "Trees stand in the stone's mind. Seal them in this order, no other: " + String.join("; then ", riddles) + ".";
                }
                case "hour" -> {
                    int[] w = HOURS.get(s.window);
                    if (w == null) return null;
                    t.type = "hour"; t.from = w[0]; t.to = w[1];
                    t.clue = "The stone will answer only at the " + s.window + " hour. Use the altar then.";
                }
                case "coop" -> { t.type = "coop"; t.players = s.fighters; t.clue = s.fighters + " fighters must stand at the altar. Alone, a champion token calls a rite spirit for each who is absent."; }
                case "defend" -> { t.type = "defend"; t.seconds = s.seconds; t.mobs = WAVE; t.clue = "Hold the altar for " + s.seconds + " seconds. Leave it alone and the rite starts again."; }
                default -> { return null; }
            }
            d.steps.add(t);
        }
        return d;
    }

    private static int indexOfTree(String name) {
        for (int i = 0; i < Ritual.TREES.length; i++) if (Ritual.TREES[i].equalsIgnoreCase(name)) return i;
        return -1;
    }

    private RiteLibrary() {}
}
