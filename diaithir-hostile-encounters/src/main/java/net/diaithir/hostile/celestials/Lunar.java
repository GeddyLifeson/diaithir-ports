package net.diaithir.hostile.celestials;

import com.google.gson.Gson;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lunar event definitions, read from data/&lt;ns&gt;/lunar_events/*.json (datapacks replace them on reload).
 * Derived from the lunar event model of Enhanced Celestials (LGPL-3.0, Corgitaco), reduced to plain JSON.
 */
public final class Lunar {
    static final Logger LOG = LoggerFactory.getLogger("diaithirhostile");
    static final Gson GSON = new Gson();
    public static final String DEFAULT_ID = "diaithirhostile:moon";

    public static final class Effect {
        /** any, monster or player. */
        public String target = "any";
        public String effect;
        public int duration_in_ticks = 1210, amplifier;
        public boolean ambient = true, visible, show_icon;
    }

    public static final class Horde {
        /** Extra hostile mobs kept near each player; 0 = none. */
        public int cap;
        public int interval_ticks = 40;
        /** biome (the biome's own monster list) or a list of entity ids. */
        public boolean biome_monsters = true;
        public List<String> pool = new ArrayList<>();
        public int min_distance = 24, max_distance = 48;
    }

    public static final class Meteor {
        public int interval_ticks;
        public int radius = 3, min_distance = 28, max_distance = 56;
    }

    /** Blood Moon (KS) style pressure: some hostile mobs are boosted and marked red, and drop Moon Essence. */
    public static final class Boost {
        public double chance;
        /** Effect ids; one is picked per boosted mob. */
        public List<String> effects = new ArrayList<>();
        public int amplifier;
        public double essence_chance = 0.5;
        /** Creeper blasts also put these effects on players within 6 blocks. */
        public List<String> creeper_effects = new ArrayList<>();
        /** Extra phantoms kept above each player; 0 = none. */
        public int phantoms;
    }

    public static final class Event {
        public String id;
        public String color = "ffffff";
        public String chat_color = "white";
        public double chance;
        public int min_nights_between = 4;
        public List<Integer> valid_moon_phases = new ArrayList<>();
        public boolean super_moon;
        public String rise, set;
        public boolean block_sleeping;
        public double xp = 1.0, anvil_cost = 1.0, drop_multiplier = 1.0;
        public String drop_tag;
        public Horde horde = new Horde();
        public List<Effect> effects = new ArrayList<>();
        public String quest;
        public double beacon_radius = 1.0, enchant_cost = 1.0;
        /** Meteors fall near players while this event is up; interval 0 = none. */
        public Meteor meteor = new Meteor();
        public Boost boost = new Boost();

        public boolean schedulable() { return chance > 0 && !valid_moon_phases.isEmpty(); }
    }

    /** Fixed world cadence, matching the Enhanced Celestials overworld defaults. */
    public static final long DAY = 24000L, YEAR_DAYS = 100L, MIN_BETWEEN = 2L, MAX_BETWEEN = 98L;

    private static volatile Map<String, Event> events = new LinkedHashMap<>();

    public static Map<String, Event> events() { return events; }
    public static Event get(String id) { return events.getOrDefault(id, events.get(DEFAULT_ID)); }
    public static Event none() { return get(DEFAULT_ID); }

    public static void reload(ResourceManager manager) {
        Map<String, Event> out = new LinkedHashMap<>();
        for (Map.Entry<Identifier, Resource> en : manager.listResources("lunar_events", i -> i.getPath().endsWith(".json")).entrySet()) {
            try (Reader r = en.getValue().openAsReader()) {
                Event e = GSON.fromJson(r, Event.class);
                String path = en.getKey().getPath();
                e.id = en.getKey().getNamespace() + ":" + path.substring("lunar_events/".length(), path.length() - 5);
                out.put(e.id, e);
            } catch (Exception ex) {
                LOG.error("bad lunar event {}", en.getKey(), ex);
            }
        }
        if (!out.containsKey(DEFAULT_ID)) {
            Event d = new Event();
            d.id = DEFAULT_ID;
            out.put(d.id, d);
        }
        events = out;
        LOG.info("lunar events: {}", out.keySet());
    }

    private Lunar() {}
}
