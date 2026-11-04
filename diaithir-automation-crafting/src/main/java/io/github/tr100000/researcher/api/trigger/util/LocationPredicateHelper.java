package io.github.tr100000.researcher.api.trigger.util;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.api.util.IndentedTextHolder;
import net.minecraft.advancements.criterion.LocationPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Contract;

public final class LocationPredicateHelper {
    private LocationPredicateHelper() {}

    private static final Component LOCATION_POSITION = ModUtils.getScreenTranslated("predicate.location.position");
    private static final Component LOCATION_POSITION_X = ModUtils.getScreenTranslated("predicate.location.position.x");
    private static final Component LOCATION_POSITION_Y = ModUtils.getScreenTranslated("predicate.location.position.y");
    private static final Component LOCATION_POSITION_Z = ModUtils.getScreenTranslated("predicate.location.position.z");
    private static final Component LOCATION_BIOMES = ModUtils.getScreenTranslated("predicate.location.biome");
    private static final Component LOCATION_STRUCTURES = ModUtils.getScreenTranslated("predicate.location.structure");
    private static final String LOCATION_DIMENSION_KEY = ModUtils.getScreenTranslationKey("predicate.location.dimension");
    private static final Component LOCATION_SMOKEY = ModUtils.getScreenTranslated("predicate.location.smokey");
    private static final Component LOCATION_NOT_SMOKEY = ModUtils.getScreenTranslated("predicate.location.not_smokey");
    private static final Component LOCATION_LIGHT = ModUtils.getScreenTranslated("predicate.location.light");
    private static final Component LOCATION_BLOCK = ModUtils.getScreenTranslated("predicate.location.block");
    private static final Component LOCATION_FLUID = ModUtils.getScreenTranslated("predicate.location.fluid");
    private static final Component LOCATION_SKY_VISIBLE = ModUtils.getScreenTranslated("predicate.location.sky_visible");
    private static final Component LOCATION_SKY_NOT_VISIBLE = ModUtils.getScreenTranslated("predicate.location.sky_not_visible");

    @Contract(mutates = "param2")
    public static void tooltip(LocationPredicate predicate, IndentedTextHolder textHolder) {
        if (predicate.position().isPresent()) {
            textHolder.accept(LOCATION_POSITION);
            textHolder.push();

            MinMaxBounds.Doubles xRange = predicate.position().get().x();
            MinMaxBounds.Doubles yRange = predicate.position().get().y();
            MinMaxBounds.Doubles zRange = predicate.position().get().z();

            MinMaxBoundsUtils.tooltip(xRange, LOCATION_POSITION_X, textHolder);
            MinMaxBoundsUtils.tooltip(yRange, LOCATION_POSITION_Y, textHolder);
            MinMaxBoundsUtils.tooltip(zRange, LOCATION_POSITION_Z, textHolder);

            textHolder.pop();
        }
        if (predicate.biomes().isPresent()) {
            textHolder.accept(LOCATION_BIOMES);
            textHolder.push();
            predicate.biomes().get().forEach(entry -> textHolder.accept(Component.translatable(entry.unwrapKey().orElseThrow().identifier().toLanguageKey("biome"))));
            textHolder.pop();
        }
        if (predicate.structures().isPresent()) {
            textHolder.accept(LOCATION_STRUCTURES);
            textHolder.push();
            predicate.structures().get().forEach(entry -> textHolder.accept(Component.literal(entry.unwrapKey().orElseThrow().identifier().toString())));
            textHolder.pop();
        }
        if (predicate.dimension().isPresent()) {
            textHolder.accept(Component.translatable(LOCATION_DIMENSION_KEY, predicate.dimension().get().identifier().toString()));
        }
        PredicateHelper.optionalBooleanTooltip(predicate.smokey(), LOCATION_SMOKEY, LOCATION_NOT_SMOKEY, textHolder);
        if (predicate.light().isPresent()) {
            MinMaxBoundsUtils.tooltip(predicate.light().get().composite(), LOCATION_LIGHT, textHolder);
        }
        if (predicate.block().isPresent()) {
            textHolder.accept(LOCATION_BLOCK);
            textHolder.push();
            BlockPredicateHelper.tooltip(predicate.block().get(), textHolder);
            textHolder.pop();
        }
        if (predicate.fluid().isPresent()) {
            textHolder.accept(LOCATION_FLUID);
            textHolder.push();
            FluidPredicateHelper.tooltip(predicate.fluid().get(), textHolder);
            textHolder.pop();
        }
        PredicateHelper.optionalBooleanTooltip(predicate.canSeeSky(), LOCATION_SKY_VISIBLE, LOCATION_SKY_NOT_VISIBLE, textHolder);
    }
}
