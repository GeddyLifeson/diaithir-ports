package io.github.tr100000.researcher.api.trigger.util;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.api.trigger.TriggerDisplayElement;
import io.github.tr100000.researcher.api.trigger.element.GroupedElement;
import io.github.tr100000.researcher.api.trigger.element.ItemElement;
import io.github.tr100000.researcher.api.trigger.element.TextElement;
import io.github.tr100000.researcher.api.trigger.element.TimedSwitchingElement;
import io.github.tr100000.researcher.api.util.IndentedTextHolder;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Contract;

import java.util.List;
import java.util.Optional;

public final class ItemPredicateHelper {
    private ItemPredicateHelper() {}

    private static final Component ITEM_LIST = ModUtils.getScreenTranslated("predicate.item.list");
    private static final Component ITEM_COUNT = ModUtils.getScreenTranslated("predicate.item.count");

    private static final Component ANY_ITEM = ModUtils.getScreenTranslated("predicate.item.any");

    @Contract(mutates = "param2")
    public static void tooltip(ItemPredicate predicate, IndentedTextHolder textHolder) {
        if (predicate.items().isPresent()) {
            textHolder.accept(ITEM_LIST);
            textHolder.push();
            predicate.items().get().forEach(entry -> textHolder.accept(entry.value().getName(entry.value().getDefaultInstance())));
            textHolder.pop();
        }
        MinMaxBoundsUtils.tooltip(predicate.count(), ITEM_COUNT, textHolder);
        if (!predicate.components().isEmpty()) {
            ComponentsPredicateHelper.tooltip(predicate.components(), textHolder);
        }
    }

    @Contract(pure = true)
    public static Optional<TriggerDisplayElement> element(ItemPredicate predicate) {
        if (predicate.items().isPresent() && predicate.items().get().size() > 0) {
            List<TriggerDisplayElement> subElements = predicate.items().get()
                    .stream()
                    .map(ItemPredicateHelper::entryElement)
                    .toList();

            return Optional.of(new TimedSwitchingElement(subElements));
        }

        return Optional.empty();
    }

    @Contract(value = "_ -> new", pure = true)
    public static TriggerDisplayElement element(Optional<ItemPredicate> predicate) {
        return predicate.flatMap(ItemPredicateHelper::element).orElseGet(ItemPredicateHelper::anyItemElement);
    }

    @Contract(value = "_ -> new", pure = true)
    public static TriggerDisplayElement entryElement(Holder<Item> item) {
        return new GroupedElement(
                new ItemElement(item.value(), true),
                new TextElement(item.value().getName(item.value().getDefaultInstance()))
        );
    }

    @Contract(value = "-> new", pure = true)
    private static TriggerDisplayElement anyItemElement() {
        return new TextElement(ANY_ITEM);
    }
}
