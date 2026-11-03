package io.github.tr100000.researcher.api.trigger.util;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.api.util.IndentedTextHolder;
import net.minecraft.advancements.criterion.CollectionPredicate;
import net.minecraft.advancements.criterion.NbtPredicate;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.advancements.criterion.TagPredicate;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class PredicateHelper {
    private PredicateHelper() {}

    private static final String TAG_EXPECTED = ModUtils.getScreenTranslationKey("predicate.tag.expected");
    private static final String TAG_NOT_EXPECTED = ModUtils.getScreenTranslationKey("predicate.tag.not_expected");

    private static final String NBT_KEY = ModUtils.getScreenTranslationKey("predicate.nbt");

    public static final Component STATE_HEADER = ModUtils.getScreenTranslated("predicate.state");
    private static final String STATE_VALUE_EXACTLY_KEY = ModUtils.getScreenTranslationKey("predicate.state.exactly");
    private static final String STATE_VALUE_BETWEEN_KEY = ModUtils.getScreenTranslationKey("predicate.state.between");
    private static final String STATE_VALUE_LESS_THAN_KEY = ModUtils.getScreenTranslationKey("predicate.state.less_than");
    private static final String STATE_VALUE_GREATER_THAN_KEY = ModUtils.getScreenTranslationKey("predicate.state.greater_than");

    private static final Component COLLECTION_SIZE = ModUtils.getScreenTranslated("predicate.collection.size");
    private static final Component COLLECTION_CONTAINS_HEADER = ModUtils.getScreenTranslated("predicate.collection.contains");
    private static final Component COLLECTION_COUNT_HEADER = ModUtils.getScreenTranslated("predicate.collection.counts");
    private static final Component COLLECTION_COUNT = ModUtils.getScreenTranslated("predicate.collection.counts.count");

    public static <T> Optional<List<MutableComponent>> optionalTooltip(Optional<T> optional, BiConsumer<T, IndentedTextHolder> tooltipProvider, @Nullable Component headerText) {
        return optional.flatMap(value -> tooltip(value, tooltipProvider, headerText));
    }

    public static <T> Optional<List<MutableComponent>> tooltip(T value, BiConsumer<T, IndentedTextHolder> tooltipProvider, @Nullable Component headerText) {
        IndentedTextHolder textHolder = new IndentedTextHolder();
        if (headerText != null) {
            textHolder.accept(headerText);
            textHolder.push();
        }
        tooltipProvider.accept(value, textHolder);

        if (textHolder.count() > (headerText != null ? 1 : 0)) {
            return Optional.of(textHolder.getText());
        }

        return Optional.empty();
    }

    @Contract(mutates = "param4")
    public static void optionalBooleanTooltip(Optional<Boolean> optional, Component trueText, Component falseText, IndentedTextHolder textHolder) {
        optional.ifPresent(value -> textHolder.accept(value ? trueText : falseText));
    }

    @Contract(mutates = "param4")
    public static void optionalBooleanTooltip(Optional<Boolean> optional, Supplier<Component> trueText, Supplier<Component> falseText, IndentedTextHolder textHolder) {
        optional.ifPresent(value -> textHolder.accept(value ? trueText.get() : falseText.get()));
    }

    @Contract(mutates = "param2")
    public static <T> void tagTooltip(TagPredicate<T> predicate, IndentedTextHolder textHolder) {
        textHolder.accept(Component.translatable(predicate.expected() ? TAG_EXPECTED : TAG_NOT_EXPECTED, predicate.tag().location().toString()));
    }

    @Contract(mutates = "param2")
    public static void nbtTooltip(NbtPredicate predicate, IndentedTextHolder textHolder) {
        textHolder.accept(Component.translatable(NBT_KEY, predicate.tag().toString()));
    }

    @Contract(mutates = "param2")
    public static void stateTooltip(StatePropertiesPredicate predicate, IndentedTextHolder textHolder) {
        predicate.properties().forEach(condition -> {
            if (condition.valueMatcher() instanceof StatePropertiesPredicate.ExactMatcher(String value)) {
                textHolder.accept(Component.translatable(STATE_VALUE_EXACTLY_KEY, condition.name(), value));
            }
            else if (condition.valueMatcher() instanceof StatePropertiesPredicate.RangedMatcher(Optional<String> min, Optional<String> max)) {
                if (min.isPresent() && max.isPresent()) {
                    textHolder.accept(Component.translatable(STATE_VALUE_BETWEEN_KEY, condition.name(), min.get(), max.get()));
                }
                else if (min.isEmpty() && max.isPresent()) {
                    textHolder.accept(Component.translatable(STATE_VALUE_LESS_THAN_KEY, condition.name(), max.get()));
                }
                else if (min.isPresent()) {
                    textHolder.accept(Component.translatable(STATE_VALUE_GREATER_THAN_KEY, condition.name(), min.get()));
                }
            }
        });
    }

    @Contract(mutates = "param3")
    public static <T, P extends Predicate<T>> void collectionTooltip(CollectionPredicate<T, P> predicate, BiConsumer<P, IndentedTextHolder> handler, IndentedTextHolder textHolder) {
        if (predicate.size().isPresent()) {
            MinMaxBoundsUtils.tooltip(predicate.size().get(), COLLECTION_SIZE, textHolder);
        }
        if (predicate.contains().isPresent()) {
            List<P> containsPredicates = predicate.contains().get().unpack();
            containsPredicates.forEach(p -> PredicateHelper.tooltip(p, handler, COLLECTION_CONTAINS_HEADER));
        }
        if (predicate.counts().isPresent()) {
            predicate.counts().get().unpack().forEach(entry -> {
                PredicateHelper.tooltip(entry.test(), (p, t) -> {
                    MinMaxBoundsUtils.tooltip(entry.count(), COLLECTION_COUNT, t);
                    handler.accept(p, t);
                }, COLLECTION_COUNT_HEADER);
            });
        }
    }
}
