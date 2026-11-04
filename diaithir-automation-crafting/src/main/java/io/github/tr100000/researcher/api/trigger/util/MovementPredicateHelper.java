package io.github.tr100000.researcher.api.trigger.util;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.api.util.IndentedTextHolder;
import net.minecraft.advancements.criterion.MovementPredicate;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Contract;

public final class MovementPredicateHelper {
    private MovementPredicateHelper() {}

    private static final Component MOVEMENT_ABSOLUTE = ModUtils.getScreenTranslated("predicate.movement.absolute");
    private static final Component MOVEMENT_HORIZONTAL = ModUtils.getScreenTranslated("predicate.movement.horizontal");
    private static final Component MOVEMENT_VERTICAL = ModUtils.getScreenTranslated("predicate.movement.vertical");
    private static final Component MOVEMENT_FALL_DISTANCE = ModUtils.getScreenTranslated("predicate.movement.fall_distance");
    private static final Component MOVEMENT_X = ModUtils.getScreenTranslated("predicate.movement.x");
    private static final Component MOVEMENT_Y = ModUtils.getScreenTranslated("predicate.movement.y");
    private static final Component MOVEMENT_Z = ModUtils.getScreenTranslated("predicate.movement.z");

    @Contract(mutates = "param2")
    public static void tooltip(MovementPredicate predicate, IndentedTextHolder textHolder) {
        MinMaxBoundsUtils.tooltip(predicate.speed(), MOVEMENT_ABSOLUTE, textHolder);
        MinMaxBoundsUtils.tooltip(predicate.horizontalSpeed(), MOVEMENT_HORIZONTAL, textHolder);
        MinMaxBoundsUtils.tooltip(predicate.verticalSpeed(), MOVEMENT_VERTICAL, textHolder);
        MinMaxBoundsUtils.tooltip(predicate.fallDistance(), MOVEMENT_FALL_DISTANCE, textHolder);
        MinMaxBoundsUtils.tooltip(predicate.x(), MOVEMENT_X, textHolder);
        MinMaxBoundsUtils.tooltip(predicate.y(), MOVEMENT_Y, textHolder);
        MinMaxBoundsUtils.tooltip(predicate.z(), MOVEMENT_Z, textHolder);
    }
}
