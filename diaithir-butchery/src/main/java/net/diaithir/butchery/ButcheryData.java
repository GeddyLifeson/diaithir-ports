package net.diaithir.butchery;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.AgeableWaterCreature;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Body classes and the entities that use them, from data/&lt;ns&gt;/butchery/*.json. A file holds {@code "classes"}
 * (id to {skin, gut, cut} yield maps of item id to [min, max]) and {@code "entities"} (entity id or #tag to class id).
 * An animal with no entry gets a class from its size, so modded creatures are butchered without any data.
 */
public final class ButcheryData {
    private static final Logger LOG = LogUtils.getLogger();
    static final String[] STAGES = {"skin", "gut", "cut"};

    record Yield(String item, int min, int max) {
    }

    private static volatile Map<String, Map<String, List<Yield>>> classes = Map.of();
    private static volatile Map<String, String> exact = Map.of();
    private static volatile List<Map.Entry<TagKey<EntityType<?>>, String>> tagged = List.of();

    private ButcheryData() {
    }

    static final class Loader extends SimpleJsonResourceReloadListener<JsonElement> {
        Loader() {
            super(ExtraCodecs.JSON, FileToIdConverter.json("butchery"));
        }

        @Override
        protected void apply(Map<Identifier, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
            Map<String, Map<String, List<Yield>>> cls = new LinkedHashMap<>();
            Map<String, String> ex = new LinkedHashMap<>();
            List<Map.Entry<TagKey<EntityType<?>>, String>> tags = new ArrayList<>();
            for (var file : files.entrySet()) {
                if (!(file.getValue() instanceof JsonObject root)) continue;
                if (root.get("classes") instanceof JsonObject c) {
                    for (var e : c.entrySet()) cls.put(e.getKey(), parseClass(e.getValue().getAsJsonObject()));
                }
                if (root.get("entities") instanceof JsonObject m) {
                    for (var e : m.entrySet()) {
                        String key = e.getKey(), value = e.getValue().getAsString();
                        if (key.startsWith("#")) {
                            Identifier id = Identifier.tryParse(key.substring(1));
                            if (id != null) tags.add(Map.entry(TagKey.create(Registries.ENTITY_TYPE, id), value));
                        } else {
                            ex.put(key, value);
                        }
                    }
                }
            }
            classes = cls;
            exact = ex;
            tagged = tags;
            LOG.info("Loaded {} butchery body classes, {} entities, {} entity tags", cls.size(), ex.size(), tags.size());
        }

        private static Map<String, List<Yield>> parseClass(JsonObject c) {
            Map<String, List<Yield>> out = new LinkedHashMap<>();
            for (String stage : STAGES) {
                List<Yield> list = new ArrayList<>();
                if (c.get(stage) instanceof JsonObject y) {
                    for (var e : y.entrySet()) {
                        JsonArray a = e.getValue().getAsJsonArray();
                        list.add(new Yield(Butchery.qualify(e.getKey()), a.get(0).getAsInt(), a.get(1).getAsInt()));
                    }
                }
                out.put(stage, list);
            }
            return out;
        }
    }

    /** The body class of this entity, or null if it is not butchered (people, players, undead, bosses and unmapped monsters). */
    static String bodyOf(LivingEntity e) {
        if (e instanceof Player || e instanceof Npc) return null;
        EntityType<?> type = e.getType();
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
        String mapped = exact.get(id);
        if (mapped != null) return "none".equals(mapped) ? null : mapped;
        for (var t : tagged) {
            if (type.builtInRegistryHolder().is(t.getKey())) return t.getValue();
        }
        boolean animal = e instanceof Animal || e instanceof AgeableWaterCreature || e instanceof AbstractFish;
        if (!animal || e instanceof Enemy) return null;
        double size = e.getBbWidth() * e.getBbWidth() * e.getBbHeight();
        boolean water = e instanceof AgeableWaterCreature || e instanceof AbstractFish;
        String prefix = water ? "fish_" : "land_";
        String bucket = size < 0.12 ? "tiny" : size < 0.5 ? "small" : size < 2.0 ? "medium" : size < 8.0 ? "large" : "huge";
        String candidate = prefix + bucket;
        return classes.containsKey(candidate) ? candidate : classes.containsKey("land_medium") ? "land_medium" : null;
    }

    static List<Yield> yield(String body, int stage) {
        Map<String, List<Yield>> c = classes.get(body);
        return c == null ? List.of() : c.getOrDefault(STAGES[stage], List.of());
    }

    static boolean known(String body) {
        return classes.containsKey(body);
    }

    static int classCount() {
        return classes.size();
    }

    static int entityCount() {
        return exact.size() + tagged.size();
    }
}
