package io.github.tr100000.researcher.api.trigger.util;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.api.util.IndentedTextHolder;
import net.minecraft.advancements.criterion.DistancePredicate;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Contract;

public final class DistancePredicateHelper {
    private DistancePredicateHelper() {}

    private static final Component DISTANCE_ABSOLUTE_KEY = ModUtils.getScreenTranslated("predicate.distance.absolute");
    private static final Component DISTANCE_HORIZONTAL_KEY = ModUtils.getScreenTranslated("predicate.distance.horizontal");
    private static final Component DISTANCE_X_KEY = ModUtils.getScreenTranslated("predicate.distance.x");
    private static final Component DISTANCE_Y_KEY = ModUtils.getScreenTranslated("predicate.distance.y");
    private static final Component DISTANCE_Z_KEY = ModUtils.getScreenTranslated("predicate.distance.z");

    @Contract(mutates = "param2")
    public static void tooltip(DistancePredicate predicate, IndentedTextHolder textHolder) {
        MinMaxBoundsUtils.tooltip(predicate.absolute(), DISTANCE_ABSOLUTE_KEY, textHolder);
        MinMaxBoundsUtils.tooltip(predicate.horizontal(), DISTANCE_HORIZONTAL_KEY, textHolder);
        MinMaxBoundsUtils.tooltip(predicate.x(), DISTANCE_X_KEY, textHolder);
        MinMaxBoundsUtils.tooltip(predicate.y(), DISTANCE_Y_KEY, textHolder);
        MinMaxBoundsUtils.tooltip(predicate.z(), DISTANCE_Z_KEY, textHolder);
    }
}
