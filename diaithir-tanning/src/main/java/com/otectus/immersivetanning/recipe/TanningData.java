package com.otectus.immersivetanning.recipe;

import com.otectus.immersivetanning.ImmersiveTanning;
import com.otectus.immersivetanning.minigame.SeamPatterns;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable server-side snapshot of the active leatherworking recipes. Rebuilt whenever the leatherworking files
 * reload; gameplay code only reads {@link #current()}. Clients receive the same list through a payload when they join
 * or the data reloads, so the vanilla recipe manager is never touched.
 */
public final class TanningData {
    private static final TanningData EMPTY = new TanningData(List.of());
    private static volatile TanningData current = EMPTY;

    private final Map<Identifier, LeatherworkingRecipe> byId;
    private final Map<Item, List<LeatherworkingRecipe>> byResult;

    private TanningData(List<LeatherworkingRecipe> recipes) {
        Map<Identifier, LeatherworkingRecipe> ids = new LinkedHashMap<>();
        Map<Item, List<LeatherworkingRecipe>> results = new HashMap<>();
        for (LeatherworkingRecipe r : recipes) {
            ids.put(r.getId(), r);
            results.computeIfAbsent(r.resultItem(), k -> new ArrayList<>()).add(r);
        }
        results.replaceAll((k, v) -> List.copyOf(v));
        this.byId = Map.copyOf(ids);
        this.byResult = Map.copyOf(results);
    }

    public static TanningData current() {
        return current;
    }

    @Nullable
    public LeatherworkingRecipe recipe(Identifier id) { return byId.get(id); }
    public Collection<LeatherworkingRecipe> recipes() { return byId.values(); }
    public List<LeatherworkingRecipe> recipesProducing(Item item) { return byResult.getOrDefault(item, List.of()); }

    public String summary() {
        return byId.size() + " leatherworking recipes";
    }

    /** Replaces the active recipes with the explicit ones from the datapacks, resolving each recipe's seam. */
    static synchronized void setExplicit(List<LeatherworkingRecipe> recipes) {
        for (LeatherworkingRecipe r : recipes) {
            Identifier seam = r.explicitSeam() != null ? r.explicitSeam() : EquipmentSeams.classify(r.result());
            if (!SeamPatterns.has(seam)) ImmersiveTanning.LOGGER.debug("Seam {} for {} is not defined; the generic seam is used", seam, r.getId());
            r.resolveSeam(seam);
        }
        current = new TanningData(recipes);
    }

    /** Replaces the active recipes with a list received from the server (client side). */
    public static synchronized void setFromServer(List<LeatherworkingRecipe> recipes) {
        current = new TanningData(recipes);
    }

    /** Clears state when the server stops, so a later world starts clean. */
    public static void reset() {
        current = EMPTY;
    }
}
