package org.exodusstudio.stellaris.common.data;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Planet list of the clean-room Stellaris module, under the class name the pack's KubeJS Age gate loads. Filled from
 * data/&lt;namespace&gt;/planets/*.json on every server start and data reload; scripts may narrow it with {@link #setPlanets}.
 */
public final class PlanetsData {
    public static final List<Planet> PLANETS = new ArrayList<>();

    private PlanetsData() {}

    public record Planet(String translationKey, ResourceKey<Level> dimension, float gravity, boolean hasOxygen, Identifier planetBar) {
        public static Planet fromJson(com.google.gson.JsonObject o) {
            return new Planet(
                    o.has("translation_key") ? o.get("translation_key").getAsString() : "",
                    ResourceKey.create(Registries.DIMENSION, Identifier.parse(o.get("dimension").getAsString())),
                    o.has("gravity") ? o.get("gravity").getAsFloat() : 1f,
                    o.has("has_oxygen") && o.get("has_oxygen").getAsBoolean(),
                    Identifier.parse(o.has("planet_bar") ? o.get("planet_bar").getAsString() : "stellaris:textures/planet_bar/moon_planet_bar.png"));
        }
    }

    public static void setPlanets(List<Planet> planets) {
        List<Planet> copy = new ArrayList<>(planets);
        PLANETS.clear();
        PLANETS.addAll(copy);
    }
}
