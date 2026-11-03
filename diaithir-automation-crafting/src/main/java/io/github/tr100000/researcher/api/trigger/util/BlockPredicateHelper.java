package io.github.tr100000.researcher.api.trigger.util;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.api.trigger.TriggerDisplayElement;
import io.github.tr100000.researcher.api.trigger.element.GroupedElement;
import io.github.tr100000.researcher.api.trigger.element.ItemElement;
import io.github.tr100000.researcher.api.trigger.element.TextElement;
import io.github.tr100000.researcher.api.trigger.element.TimedSwitchingElement;
import io.github.tr100000.researcher.api.util.IndentedTextHolder;
import net.minecraft.advancements.criterion.BlockPredicate;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.util.List;

public final class BlockPredicateHelper {
    private BlockPredicateHelper() {}

    private static final Component HEADER = ModUtils.getScreenTranslated("predicate.block");
    private static final Component ANY_BLOCK = ModUtils.getScreenTranslated("predicate.block.any");

    private static final Component BLOCK_LIST = ModUtils.getScreenTranslated("predicate.block.list");

    @Contract(mutates = "param2")
    public static void tooltip(BlockPredicate predicate, IndentedTextHolder textHolder) {
        if (predicate.blocks().isPresent()) {
            textHolder.accept(BLOCK_LIST);
            textHolder.push();
            predicate.blocks().get().forEach(entry -> textHolder.accept(entry.value().getName()));
            textHolder.pop();
        }
        if (predicate.properties().isPresent()) {
            textHolder.accept(PredicateHelper.STATE_HEADER);
            textHolder.push();
            PredicateHelper.stateTooltip(predicate.properties().get(), textHolder);
            textHolder.pop();
        }
        if (predicate.nbt().isPresent()) {
            PredicateHelper.nbtTooltip(predicate.nbt().get(), textHolder);
        }
        if (!predicate.components().isEmpty()) {
            ComponentsPredicateHelper.tooltip(predicate.components(), textHolder);
        }
    }

    @Contract(value = "_ -> new", pure = true)
    public static TriggerDisplayElement element(@Nullable BlockPredicate predicate) {
        if (predicate == null) return anyBlockElement();

        TriggerDisplayElement element;
        boolean shouldHaveTooltip = false;
        if (predicate.blocks().isPresent()) {
            if (predicate.blocks().get().size() > 1) shouldHaveTooltip = true;

            List<TriggerDisplayElement> list = predicate.blocks().get().stream().map(BlockPredicateHelper::element).toList();
            element = new TimedSwitchingElement(list);
        }
        else {
            element = new TextElement(ANY_BLOCK);
        }

        if (shouldHaveTooltip || predicate.properties().isPresent() || predicate.nbt().isPresent() || !predicate.components().isEmpty()) {
            IndentedTextHolder textHolder = new IndentedTextHolder();
            textHolder.accept(HEADER);
            textHolder.push();
            tooltip(predicate, textHolder);
            textHolder.pop();

            if (textHolder.count() > 1) element = new GroupedElement(element.withTextTooltip(), new TextElement(Component.literal("*")));
        }

        return element;
    }

    @Contract(value = "_ -> new", pure = true)
    public static TriggerDisplayElement element(@Nullable Block block) {
        if (block == null) return anyBlockElement();

        return new GroupedElement(
                new ItemElement(block, false),
                new TextElement(block.getName())
        );
    }

    @Contract(value = "_ -> new", pure = true)
    public static TriggerDisplayElement element(@Nullable Holder<Block> block) {
        if (block == null) return anyBlockElement();

        return element(block.value());
    }

    @Contract(value = "-> new", pure = true)
    private static TriggerDisplayElement anyBlockElement() {
        return new TextElement(ANY_BLOCK);
    }
}
