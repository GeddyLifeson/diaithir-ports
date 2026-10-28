package net.diaithir.bosses;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Boss profiles and the elite table. Classpath copies are read at startup (entity types need them); datapacks may replace both on reload. */
public final class Data {
    static final Logger LOG = LoggerFactory.getLogger("diaithirbosses");
    static final Gson GSON = new Gson();

    public static final class Own {
        public String kind = "zombie";
        public float scale = 1.0F;
        public double hp = 100, damage = 6, armor = 0, speed = 0.28, knockback_resistance = 1.0, follow_range = 48;
        public boolean fire_immune;
    }

    public static final class Bar { public String color = "red", overlay = "notched_10"; }

    public static final class Scaling { public double hp_per_age = 0.2, damage_per_age = 0.1, hp_per_extra_player = 0.4; public int max_age_steps = 4; }

    public static final class Arena { public double radius = 22, burn = 4; public boolean lock = true, leash = true; }

    public static final class Respawn { public String mode = "cooldown"; public int ticks = 24000; }

    public static final class Phase {
        public double at = 1, speed = 1, damage = 1, cooldown = 1;
        public String name = "I", say;
        public List<String> attacks = new ArrayList<>();
        public int invuln;
    }

    public static final class Attack {
        public String type;
        public int telegraph = 30, cooldown = 100, active = 12, count = 1, cap = 8, ticks = 100, amp = 0, duration = 200;
        public double radius = 5, damage = 5, knockback = 1.0, speed = 1.0, power = 1.0, range = 30, amount = 0.05;
        public boolean fire;
        public String effect, minion;
    }

    public static final class Loot { public String item; public int min = 1, max = 1, min_age = 1, per_age; public double chance = 1; }

    public static final class Structure { public int style, spacing, separation; public String biomes; }

    public static final class Profile {
        public String id, name, name_en, entity, creature_id, court, habitat, existing_model;
        public int age = 1, xp = 100;
        public boolean hostile = true, mythic, fallback_own;
        public double max_hit_fraction = 0.1;
        public Own own = new Own();
        public Bar bar = new Bar();
        public Scaling scaling = new Scaling();
        public Arena arena = new Arena();
        public Respawn respawn = new Respawn();
        public Map<String, Attack> attacks = new LinkedHashMap<>();
        public List<Phase> phases = new ArrayList<>();
        public List<Loot> loot = new ArrayList<>();
        public Map<String, String> equipment = new LinkedHashMap<>();
        public Structure structure;

        public String key() { return creature_id != null ? creature_id : id; }
        /** True when this profile spawns its own entity type (as opposed to attaching to another mod's). */
        public boolean ownsEntity() { return entity.startsWith(DiaithirBosses.ID + ":"); }
        public String ownPath() { return id; }
    }

    public static final class Affix {
        public Map<String, List<Object>> attributes;
        public Double reflect, leech, absorption;
        public boolean fire_immune;
        public Map<String, Object> on_hit, summon_on_hurt, blast_on_death;
    }

    public static final class EliteBase { public int weight = 1; public List<String> affixes = new ArrayList<>(); }

    public static final class Token { public String item; public double chance; public int min_age = 1; }

    public static final class Elites {
        public double[] chance_by_age = {0.03};
        public int[] affixes_by_age = {1};
        public double extra_hp_per_age = 0.05, xp_multiplier = 3;
        /** Health bonuses of other mods (Dwarfs and Giants size, Remnants level) and our Age bonus add up instead of multiplying; 0 turns the guard off. */
        public boolean additive_stacking = true;
        /** The ceiling also applies when other mods' rolls alone are above it (our modifier goes negative to bring the total down). */
        public boolean cap_other_mods = true;
        /** Ceiling for the champion's total max health over its base value, all mods together. 0 = no ceiling. */
        public double max_combined_health_multiplier = 4.0;
        public Token token;
        public Map<String, EliteBase> bases = new LinkedHashMap<>();
        public Map<String, Affix> affixes = new LinkedHashMap<>();
    }

    private static volatile Map<String, Profile> profiles = new LinkedHashMap<>();
    private static volatile Map<String, Profile> byEntity = new LinkedHashMap<>();
    private static volatile Elites elites = new Elites();

    public static Map<String, Profile> profiles() { return profiles; }
    public static Profile byEntity(String entityId) { return byEntity.get(entityId); }
    public static Profile byId(String id) { return profiles.get(id); }
    public static Elites elites() { return elites; }

    /** A mythic kit whose creature is spawned by diaithir_myth attaches to that entity; otherwise it spawns its own. */
    private static void normalise(Profile p) {
        if (p.fallback_own && !ModList.get().isLoaded("diaithir_myth")) p.entity = DiaithirBosses.ID + ":" + p.id;
        if (p.entity == null) p.entity = DiaithirBosses.ID + ":" + p.id;
        if (p.name == null) p.name = p.id;
    }

    private static void install(Map<String, Profile> list, Elites e) {
        Map<String, Profile> bye = new LinkedHashMap<>();
        for (Profile p : list.values()) bye.put(p.entity, p);
        profiles = list;
        byEntity = bye;
        if (e != null) elites = e;
    }

    /** Startup read from the mod jar. */
    public static void loadClasspath() {
        Map<String, Profile> out = new LinkedHashMap<>();
        try (Reader r = new InputStreamReader(Data.class.getResourceAsStream("/data/" + DiaithirBosses.ID + "/boss_index.json"), StandardCharsets.UTF_8)) {
            List<String> ids = GSON.fromJson(r, new TypeToken<List<String>>() {}.getType());
            for (String id : ids) {
                try (Reader pr = new InputStreamReader(Data.class.getResourceAsStream("/data/" + DiaithirBosses.ID + "/boss/" + id + ".json"), StandardCharsets.UTF_8)) {
                    Profile p = GSON.fromJson(pr, Profile.class);
                    normalise(p);
                    out.put(p.id, p);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("boss profiles missing", e);
        }
        Elites el;
        try (Reader r = new InputStreamReader(Data.class.getResourceAsStream("/data/" + DiaithirBosses.ID + "/elites.json"), StandardCharsets.UTF_8)) {
            el = GSON.fromJson(r, Elites.class);
        } catch (Exception e) {
            throw new IllegalStateException("elites.json missing", e);
        }
        install(out, el);
        LOG.info("Loaded {} boss profiles and {} elite bases", out.size(), el.bases.size());
    }

    /** Datapack reload: profiles from every data/<ns>/boss/*.json, elites from data/diaithirbosses/elites.json. */
    public static void reload(ResourceManager rm) {
        Map<String, Profile> out = new LinkedHashMap<>();
        for (Map.Entry<Identifier, Resource> en : rm.listResources("boss", id -> id.getPath().endsWith(".json")).entrySet()) {
            try (Reader r = en.getValue().openAsReader()) {
                Profile p = GSON.fromJson(r, Profile.class);
                if (p == null || p.phases == null || p.phases.isEmpty()) continue;
                if (p.id == null) {
                    String path = en.getKey().getPath();
                    p.id = path.substring(path.lastIndexOf('/') + 1, path.length() - 5);
                }
                normalise(p);
                out.put(p.id, p);
            } catch (Exception e) {
                LOG.warn("Bad boss profile {}: {}", en.getKey(), e.toString());
            }
        }
        Elites el = null;
        try {
            var res = rm.getResource(Identifier.fromNamespaceAndPath(DiaithirBosses.ID, "elites.json"));
            if (res.isPresent()) try (Reader r = res.get().openAsReader()) { el = GSON.fromJson(r, Elites.class); }
        } catch (Exception e) {
            LOG.warn("Bad elites.json: {}", e.toString());
        }
        if (!out.isEmpty()) install(out, el); else if (el != null) elites = el;
        LOG.info("Reloaded {} boss profiles", profiles.size());
    }

    /** Test hook: temporarily registers a profile for another mod's (or vanilla's) entity. */
    static void attach(Profile p) {
        Map<String, Profile> m = new LinkedHashMap<>(profiles);
        m.put(p.id, p);
        install(m, null);
    }

    static void detach(String id) {
        Map<String, Profile> m = new LinkedHashMap<>(profiles);
        m.remove(id);
        install(m, null);
    }

    private Data() {}
}
