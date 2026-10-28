package com.github.alexmodguy.alexscaves.client.gui.book;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 26.1 clients no longer hold the recipe manager, so the guide book reads the mod's own recipe json
 * straight from the mod jar (the same file the server loads) to draw crafting and smelting grids.
 */
public class BookRecipes {

    public record Display(boolean smelting, int width, int height, List<List<ItemStack>> cells, ItemStack result) {
    }

    private static final Map<String, Optional<Display>> CACHE = new HashMap<>();

    public static Display get(String recipeId) {
        return CACHE.computeIfAbsent(recipeId, BookRecipes::load).orElse(null);
    }

    private static Optional<Display> load(String recipeId) {
        Identifier id = Identifier.parse(recipeId);
        String path = "/data/" + id.getNamespace() + "/recipe/" + id.getPath() + ".json";
        try (var in = BookRecipes.class.getResourceAsStream(path)) {
            if (in == null) {
                return Optional.empty();
            }
            try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                String type = json.get("type").getAsString();
                ItemStack result = parseResult(json.get("result"));
                if (type.endsWith("crafting_shaped")) {
                    JsonArray pattern = json.getAsJsonArray("pattern");
                    JsonObject key = json.getAsJsonObject("key");
                    int h = pattern.size();
                    int w = 0;
                    for (JsonElement row : pattern) {
                        w = Math.max(w, row.getAsString().length());
                    }
                    List<List<ItemStack>> cells = new ArrayList<>();
                    for (int y = 0; y < h; y++) {
                        String row = pattern.get(y).getAsString();
                        for (int x = 0; x < w; x++) {
                            char c = x < row.length() ? row.charAt(x) : ' ';
                            cells.add(c == ' ' || !key.has(String.valueOf(c)) ? List.of() : parseIngredient(key.get(String.valueOf(c))));
                        }
                    }
                    return Optional.of(new Display(false, w, h, cells, result));
                } else if (type.endsWith("crafting_shapeless")) {
                    List<List<ItemStack>> cells = new ArrayList<>();
                    for (JsonElement e : json.getAsJsonArray("ingredients")) {
                        cells.add(parseIngredient(e));
                    }
                    return Optional.of(new Display(false, 3, 3, cells, result));
                } else if (json.has("ingredient")) {
                    return Optional.of(new Display(true, 1, 1, List.of(parseIngredient(json.get("ingredient"))), result));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    private static ItemStack parseResult(JsonElement e) {
        if (e == null) {
            return ItemStack.EMPTY;
        }
        if (e.isJsonPrimitive()) {
            return stackOf(e.getAsString(), 1);
        }
        JsonObject o = e.getAsJsonObject();
        String item = o.has("id") ? o.get("id").getAsString() : o.get("item").getAsString();
        return stackOf(item, o.has("count") ? o.get("count").getAsInt() : 1);
    }

    private static ItemStack stackOf(String id, int count) {
        Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(id));
        return new ItemStack(item, count);
    }

    private static List<ItemStack> parseIngredient(JsonElement e) {
        List<ItemStack> out = new ArrayList<>();
        if (e.isJsonArray()) {
            for (JsonElement inner : e.getAsJsonArray()) {
                out.addAll(parseIngredient(inner));
            }
        } else if (e.isJsonPrimitive()) {
            addRef(out, e.getAsString());
        } else if (e.isJsonObject()) {
            JsonObject o = e.getAsJsonObject();
            if (o.has("item")) {
                addRef(out, o.get("item").getAsString());
            } else if (o.has("tag")) {
                addRef(out, "#" + o.get("tag").getAsString());
            }
        }
        return out;
    }

    private static void addRef(List<ItemStack> out, String ref) {
        if (ref.startsWith("#")) {
            TagKey<Item> tag = TagKey.create(Registries.ITEM, Identifier.parse(ref.substring(1)));
            for (var holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
                out.add(new ItemStack(holder.value()));
            }
        } else {
            out.add(stackOf(ref, 1));
        }
    }
}
