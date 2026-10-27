package net.diaithir.brewing;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads data/NAMESPACE/diaithirbrewing/NAME.json from every datapack (KubeJS can write these too):
 * {"station":"keg"|"fermenting_barrel","base":"item","base_returns":"item"?,"ingredients":{"item":count},
 *  "ticks":n,"drink":"item","servings":n,"container":"item"?}
 */
public final class BrewRecipes implements ResourceManagerReloadListener {
    private static final Logger LOG = LogUtils.getLogger();
    private static final Gson GSON = new Gson();
    private static volatile List<BrewRecipe> all = List.of();

    public static List<BrewRecipe> all() {
        return all;
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        List<BrewRecipe> loaded = new ArrayList<>();
        for (Map.Entry<Identifier, Resource> e : manager.listResources(DiaithirBrewing.ID, id -> id.getPath().endsWith(".json")).entrySet()) {
            try (Reader in = e.getValue().openAsReader()) {
                loaded.add(parse(GSON.fromJson(in, JsonObject.class)));
            } catch (Exception ex) {
                LOG.warn("Skipping brewing recipe {}: {}", e.getKey(), ex.toString());
            }
        }
        all = List.copyOf(loaded);
        LOG.info("Loaded {} brewing recipes", loaded.size());
    }

    private static BrewRecipe parse(JsonObject j) {
        Map<Item, Integer> ingredients = new HashMap<>();
        j.getAsJsonObject("ingredients").entrySet().forEach(en -> ingredients.merge(item(en.getKey()), en.getValue().getAsInt(), Integer::sum));
        String station = j.get("station").getAsString();
        if (!station.equals("keg") && !station.equals("fermenting_barrel")) {
            throw new IllegalArgumentException("unknown station " + station);
        }
        Item returns = j.has("base_returns") ? item(j.get("base_returns").getAsString()) : Items.AIR;
        Item container = j.has("container") ? item(j.get("container").getAsString()) : Items.GLASS_BOTTLE;
                return new BrewRecipe(station, item(j.get("base").getAsString()), returns, Map.copyOf(ingredients), j.get("ticks").getAsInt(),
            item(j.get("drink").getAsString()), j.has("servings") ? j.get("servings").getAsInt() : 4, container);
    }

    private static Item item(String id) {
        return BuiltInRegistries.ITEM.getOptional(Identifier.parse(id)).orElseThrow(() -> new IllegalArgumentException("unknown item " + id));
    }

    static int largest(String station) {
        return all.stream().filter(r -> r.station().equals(station))
            .mapToInt(r -> r.ingredients().values().stream().mapToInt(Integer::intValue).sum()).max().orElse(0);
    }

    static boolean usesBase(String station, Item item) {
        return all.stream().anyMatch(r -> r.station().equals(station) && r.base() == item);
    }

    static boolean usesIngredient(String station, Item item) {
        return all.stream().anyMatch(r -> r.station().equals(station) && r.ingredients().containsKey(item));
    }
}
