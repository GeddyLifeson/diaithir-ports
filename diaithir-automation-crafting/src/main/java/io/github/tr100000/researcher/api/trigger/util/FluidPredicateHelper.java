package io.github.tr100000.researcher.api.trigger.util;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.api.trigger.TriggerDisplayElement;
import io.github.tr100000.researcher.api.trigger.element.GroupedElement;
import io.github.tr100000.researcher.api.trigger.element.ItemElement;
import io.github.tr100000.researcher.api.trigger.element.TextElement;
import io.github.tr100000.researcher.api.trigger.element.TimedSwitchingElement;
import io.github.tr100000.researcher.api.util.IndentedTextHolder;
import net.minecraft.advancements.criterion.FluidPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Contract;

import java.util.List;

public final class FluidPredicateHelper {
    private FluidPredicateHelper() {}

    private static final Component HEADER = ModUtils.getScreenTranslated("predicate.fluid");
    private static final Component ANY_FLUID = ModUtils.getScreenTranslated("predicate.fluid.any");

    private static final Component FLUID_LIST = ModUtils.getScreenTranslated("predicate.fluid.list");

    @Contract(mutates = "param2")
    public static void tooltip(FluidPredicate predicate, IndentedTextHolder textHolder) {
        if (predicate.fluids().isPresent()) {
            textHolder.accept(FLUID_LIST);
            textHolder.push();
            predicate.fluids().get().forEach(entry -> textHolder.accept(Component.literal(entry.getRegisteredName())));
            textHolder.pop();
        }
        if (predicate.properties().isPresent()) {
            textHolder.accept(PredicateHelper.STATE_HEADER);
            textHolder.push();
            PredicateHelper.stateTooltip(predicate.properties().get(), textHolder);
            textHolder.pop();
        }
    }

    @Contract(value = "_ -> new", pure = true)
    public static TriggerDisplayElement element(FluidPredicate predicate) {
        TriggerDisplayElement element;
        boolean shouldHaveTooltip = false;
        if (predicate.fluids().isPresent()) {
            if (predicate.fluids().get().size() > 1) shouldHaveTooltip = true;

            List<TriggerDisplayElement> list = predicate.fluids().get().stream().map(FluidPredicateHelper::element).toList();
            element = new TimedSwitchingElement(list);
        }
        else {
            element = new TextElement(ANY_FLUID);
        }

        if (shouldHaveTooltip || predicate.properties().isPresent()) {
            IndentedTextHolder textHolder = new IndentedTextHolder();
            textHolder.accept(HEADER);
            textHolder.push();
            tooltip(predicate, textHolder);
            textHolder.pop();

            if (textHolder.count() > 1) element = new GroupedElement(element.withTextTooltip(textHolder.getText()), new TextElement(Component.literal("*")));
        }

        return element;
    }

    @Contract(value = "_ -> new", pure = true)
    public static TriggerDisplayElement element(Fluid fluid) {
        return new GroupedElement(
                new ItemElement(fluid.getBucket(), false),
                new TextElement(getFluidName(fluid))
        );
    }

    @Contract(value = "_ -> new", pure = true)
    public static TriggerDisplayElement element(Holder<Fluid> fluid) {
        return element(fluid.value());
    }

    private static Component getFluidName(Fluid fluid) {
        if (fluid instanceof FlowingFluid flowable) {
            return Component.translatable(BuiltInRegistries.FLUID.getKey(flowable.getSource()).toLanguageKey("fluid"));
        }
        else {
            return Component.translatable(BuiltInRegistries.FLUID.getKey(fluid).toLanguageKey("fluid"));
        }
    }
}
