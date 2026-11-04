package io.github.tr100000.researcher.impl.criterion;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.ResearchCriterion;
import io.github.tr100000.researcher.api.trigger.TriggerDisplay;
import io.github.tr100000.researcher.api.trigger.TriggerDisplayElement;
import io.github.tr100000.researcher.api.trigger.TriggerHandler;
import io.github.tr100000.researcher.api.trigger.element.ItemElement;
import io.github.tr100000.researcher.api.trigger.element.SpacingElement;
import io.github.tr100000.researcher.api.trigger.element.TextElement;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.criterion.ImpossibleTrigger;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ErrorTriggerHandler<T extends CriterionTriggerInstance> implements TriggerHandler<T> {
    public static final ErrorTriggerHandler<CriterionTriggerInstance> NULL = new ErrorTriggerHandler<>(ModUtils.getScreenTranslated("trigger.null"));
    public static final ErrorTriggerHandler<ImpossibleTrigger.TriggerInstance> IMPOSSIBLE = new ErrorTriggerHandler<>(ModUtils.getScreenTranslated("trigger.impossible"));
    public static final ErrorTriggerHandler<CriterionTriggerInstance> ERROR = new ErrorTriggerHandler<>(ModUtils.getScreenTranslated("trigger.error"));

    private final ItemStack stack = Items.BARRIER.getDefaultInstance();
    private final Component text;

    public ErrorTriggerHandler(Component text) {
        this.text = text;
    }

    @Override
    public TriggerDisplayElement prepare(ResearchCriterion<T> criterion) {
        return prepare();
    }

    public TriggerDisplayElement prepare() {
        return new TriggerDisplay(
                new ItemElement(stack, false),
                new SpacingElement(2),
                new TextElement(text)
        );
    }

    public TriggerDisplayElement prepareWithErrorTooltip(Component errorText) {
        return new TriggerDisplay(
                new ItemElement(stack, false).withTextTooltip(errorText),
                new SpacingElement(2),
                new TextElement(text)
        );
    }
}
