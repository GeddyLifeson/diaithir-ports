package io.github.tr100000.researcher.api.trigger.util;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.api.util.IndentedTextHolder;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Contract;

import java.util.Optional;

public final class MinMaxBoundsUtils {
    private MinMaxBoundsUtils() {}

    private static final String RANGE_EXACTLY_KEY = ModUtils.getScreenTranslationKey("predicate.range.exactly");
    private static final String RANGE_BETWEEN_KEY = ModUtils.getScreenTranslationKey("predicate.range.between");
    private static final String RANGE_LESS_THAN_KEY = ModUtils.getScreenTranslationKey("predicate.range.less_than");
    private static final String RANGE_GREATER_THAN_KEY = ModUtils.getScreenTranslationKey("predicate.range.greater_than");

    @Contract(pure = true)
    public static <T extends Number & Comparable<T>> Optional<Component> getText(MinMaxBounds<T> range) {
        if (range.max().isPresent() && range.min().isPresent()) {
            // Both max and min values are present (either range or exact)
            if (range.max().get().equals(range.min().get())) {
                return Optional.of(Component.translatable(RANGE_EXACTLY_KEY, range.min().get()));
            }
            else {
                return Optional.of(Component.translatable(RANGE_BETWEEN_KEY, range.max().get(), range.min().get()));
            }
        }
        else if (range.max().isPresent() && range.min().isEmpty()) {
            // Only max value is present
            return Optional.of(Component.translatable(RANGE_LESS_THAN_KEY, range.max().get()));
        }
        else if (range.max().isEmpty() && range.min().isPresent()) {
            // Only min value is present
            return Optional.of(Component.translatable(RANGE_GREATER_THAN_KEY, range.min().get()));
        }
        else {
            // No values are present
            return Optional.empty();
        }
    }

    @Contract(mutates = "param2")
    public static <T extends Number & Comparable<T>> void tooltip(MinMaxBounds<T> range, Component label, IndentedTextHolder textHolder) {
        getText(range).ifPresent(t -> textHolder.accept(label.copy().append(t)));
    }
}
