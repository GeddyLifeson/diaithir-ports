package net.diaithir.brewing;

import net.minecraft.world.item.Item;

import java.util.Map;

/** One fermenting recipe (abv above 0 is stamped on every drink poured, as custom_data abv, plus aged in days from an oak cask): a base held in the vessel, solid ingredients, a time, and servings of a drink poured into a container. */
public record BrewRecipe(String station, Item base, Item baseReturns, Map<Item, Integer> ingredients, int ticks, Item drink, int servings, Item container, Item spoiled, float spoilChance, double abv) {
    boolean matches(String station, Item base, Map<Item, Integer> held) {
        return this.station.equals(station) && this.base == base && ingredients.equals(held);
    }
}
