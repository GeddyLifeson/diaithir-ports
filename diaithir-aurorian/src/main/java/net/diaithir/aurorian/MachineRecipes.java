package net.diaithir.aurorian;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Scrapper and moonlight forge recipes: data/theaurorian/machine_recipes/{scrapper,moonlight_forge}/*.json, reloadable by datapacks. */
public final class MachineRecipes {
    public record Ing(String spec) {
        boolean test(ItemStack s) {
            if (s.isEmpty()) {
                return false;
            }
            if (spec.startsWith("#")) {
                return s.is(TagKey.create(Registries.ITEM, Identifier.parse(spec.substring(1))));
            }
            return BuiltInRegistries.ITEM.getKey(s.getItem()).equals(Identifier.parse(spec));
        }
    }

    public record Recipe(Ing input, Ing catalyst, Identifier output, int count) {
        public ItemStack result() {
            Item item = BuiltInRegistries.ITEM.getOptional(output).orElse(null);
            return item == null ? ItemStack.EMPTY : new ItemStack(item, count);
        }
    }

    private static volatile Map<String, List<Recipe>> all = Map.of();

    public static Recipe find(String kind, ItemStack input, ItemStack catalyst) {
        for (Recipe r : all.getOrDefault(kind, List.of())) {
            if (r.input().test(input) && (r.catalyst() == null || r.catalyst().test(catalyst))) {
                return r;
            }
        }
        return null;
    }

    public static SimplePreparableReloadListener<Map<String, List<Recipe>>> listener() {
        return new SimplePreparableReloadListener<>() {
            @Override
            protected Map<String, List<Recipe>> prepare(ResourceManager rm, ProfilerFiller p) {
                Map<String, List<Recipe>> out = new java.util.HashMap<>();
                for (String kind : new String[] {"scrapper", "moonlight_forge"}) {
                    List<Recipe> list = new ArrayList<>();
                    for (Map.Entry<Identifier, Resource> e : rm.listResources("machine_recipes/" + kind, id -> id.getPath().endsWith(".json")).entrySet()) {
                        try (Reader r = e.getValue().openAsReader()) {
                            JsonObject o = JsonParser.parseReader(r).getAsJsonObject();
                            JsonObject res = o.getAsJsonObject("output");
                            list.add(new Recipe(new Ing(o.get("input").getAsString()), o.has("catalyst") ? new Ing(o.get("catalyst").getAsString()) : null,
                                Identifier.parse(res.get("id").getAsString()), res.has("count") ? res.get("count").getAsInt() : 1));
                        } catch (Exception ex) {
                            DiaithirAurorian.LOGGER.error("bad machine recipe {}", e.getKey(), ex);
                        }
                    }
                    out.put(kind, list);
                }
                return out;
            }

            @Override
            protected void apply(Map<String, List<Recipe>> prepared, ResourceManager rm, ProfilerFiller p) {
                all = prepared;
            }
        };
    }

    private MachineRecipes() {}
}
