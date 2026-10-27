package net.diaithir.brewing;

import net.minecraft.world.item.Item;

import java.util.Map;

/** One fermenting recipe: a base held in the vessel, solid ingredients, a time, and servings of a drink poured into a container. */
public record BrewRecipe(String station, Item base, Item baseReturns, Map<Item, Integer> ingredients, int ticks, Item drink, int servings, Item container) {
    boolean matches(String station, Item base, Map<Item, Integer> held) {
        return this.station.equals(station) && this.base == base && ingredients.equals(held);
    }
}
