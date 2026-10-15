package net.diaithir.compat;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/// Rewrites the 1.21.1 shapes of item references to their 26.1 forms:
/// ingredients `{"item": x}` → `"x"` and `{"tag": t}` → `"#t"`; outputs `{"item": x, ...}` → `{"id": x, ...}`;
/// fluid stacks `{"name": f, "amount": n}` → `{"fluid": f, ...}` (inputs) or `{"id": f, ...}` (outputs);
/// custom ingredients `{"type": t, ...}` → `{"neoforge:ingredient_type": t, ...}`; an array used as one ingredient
/// that mixes tags or custom ingredients → a `neoforge:compound` of its entries; renamed vanilla item ids → their new ids.
/// Callers decide whether the result is kept (see DiaithirCompat), so a wrong guess here costs nothing.
final class LegacyRecipeJson {
    private static final Set<String> OUTPUT_KEYS = Set.of(
        "result", "results", "output", "outputs", "main_output", "secondary_output", "item_output", "item_outputs", "byproducts");
    private static final Set<String> LIST_KEYS = Set.of("children", "pattern");
    private static final String TYPE_KEY = "neoforge:ingredient_type";
    /// Vanilla items renamed since 1.21.1
    private static final Map<String, String> RENAMED = Map.of("minecraft:chain", "minecraft:iron_chain");

    private LegacyRecipeJson() {}

    /// Upgrades `json` in place; returns whether anything changed.
    static boolean upgrade(JsonObject json) {
        return upgrade(json, LegacyRecipeJson::isIngredientType);
    }

    static boolean upgrade(JsonObject json, Predicate<String> ingredientType) {
        boolean changed = false;
        for (var entry : json.entrySet()) {
            String key = entry.getKey();
            // plural keys ("ingredients", "inputs"), compound children and shaped patterns are lists; any other array is one ingredient's alternatives
            boolean alternatives = !key.endsWith("s") && !LIST_KEYS.contains(key);
            JsonElement replacement = upgrade(entry.getValue(), OUTPUT_KEYS.contains(key), alternatives, ingredientType);
            if (replacement != null) {
                entry.setValue(replacement);
                changed = true;
            }
        }
        return changed;
    }

    /// The replacement for `element`, or null when it (and everything under it) is already fine.
    private static JsonElement upgrade(JsonElement element, boolean output, boolean alternatives, Predicate<String> ingredientType) {
        if (element instanceof JsonArray array) {
            boolean changed = false;
            for (int i = 0; i < array.size(); i++) {
                // an array inside an array is always one ingredient's alternatives
                JsonElement replacement = upgrade(array.get(i), output, true, ingredientType);
                if (replacement != null) {
                    array.set(i, replacement);
                    changed = true;
                }
            }
            // 26.1 reads a bare array only as a list of item ids
            if (!output && alternatives && !array.isEmpty() && !onlyItemIds(array)) {
                JsonObject compound = new JsonObject();
                compound.addProperty(TYPE_KEY, "neoforge:compound");
                compound.add("children", array);
                return compound;
            }
            return changed ? array : null;
        }
        if (element instanceof JsonPrimitive id) return renamed(id);
        if (!(element instanceof JsonObject object)) return null;

        if (output && object.get("item") instanceof JsonPrimitive && !object.has("id")) {
            object.add("id", object.remove("item"));
            return object;
        }
        // pre-1.21 fluid stacks: {"name": fluid, "amount": n}
        if (object.has("name") && object.has("amount") && !object.has("fluid") && !object.has("id")) {
            object.add(output ? "id" : "fluid", object.remove("name"));
            return object;
        }
        if (!output && object.size() == 1 && object.get("item") instanceof JsonPrimitive item) return Objects.requireNonNullElse(renamed(item), item);
        if (!output && object.size() == 1 && object.get("tag") instanceof JsonPrimitive tag) return new JsonPrimitive("#" + tag.getAsString());
        if (!output && !object.has(TYPE_KEY) && object.get("type") instanceof JsonPrimitive type && ingredientType.test(type.getAsString())) {
            object.add(TYPE_KEY, object.remove("type"));
            upgrade(object, ingredientType);
            return object;
        }

        return upgrade(object, ingredientType) ? object : null;
    }

    private static JsonPrimitive renamed(JsonPrimitive id) {
        String to = RENAMED.get(id.getAsString());
        return to == null ? null : new JsonPrimitive(to);
    }

    private static boolean onlyItemIds(JsonArray array) {
        for (JsonElement entry : array) {
            if (!(entry instanceof JsonPrimitive id) || id.getAsString().startsWith("#")) return false;
        }
        return true;
    }

    private static boolean isIngredientType(String id) {
        Identifier key = Identifier.tryParse(id);
        return key != null && NeoForgeRegistries.INGREDIENT_TYPES.containsKey(key);
    }
}
