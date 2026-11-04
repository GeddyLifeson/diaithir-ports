package io.github.tr100000.researcher.impl.criterion;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.ResearchCriterion;
import io.github.tr100000.researcher.api.trigger.TriggerDisplay;
import io.github.tr100000.researcher.api.trigger.TriggerDisplayElement;
import io.github.tr100000.researcher.api.trigger.TriggerHandler;
import io.github.tr100000.researcher.api.trigger.element.TextElement;
import io.github.tr100000.researcher.api.trigger.util.EntityPredicateHelper;
import io.github.tr100000.researcher.api.trigger.util.ItemPredicateHelper;
import io.github.tr100000.researcher.api.trigger.util.PredicateHelper;
import io.github.tr100000.researcher.api.util.IndentedTextHolder;
import net.minecraft.advancements.criterion.ConsumeItemTrigger;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public class ConsumeItemTriggerHandler implements TriggerHandler<ConsumeItemTrigger.TriggerInstance> {
    private static final Component PLAYER_CONDITIONS_HEADER = ModUtils.getScreenTranslated("predicate.player");
    private static final Component TEXT = ModUtils.getScreenTranslated("trigger.consume_item");
    private static final Component TEXT_WITH_CONDITIONS = ModUtils.getScreenTranslated("trigger.consume_item.with_conditions");
    private static final Component TEXT_SPECIFIC_BEFORE = ModUtils.getScreenTranslated("trigger.consume_item.specific.before");
    private static final Component TEXT_SPECIFIC_BEFORE_WITH_CONDITIONS = ModUtils.getScreenTranslated("trigger.consume_item.specific.before.with_conditions");
    private static final Component TEXT_SPECIFIC_AFTER = ModUtils.getScreenTranslated("trigger.consume_item.specific.after");
    private static final Component TEXT_SPECIFIC_AFTER_WITH_CONDITIONS = ModUtils.getScreenTranslated("trigger.consume_item.specific.after.with_conditions");

    @Override
    public TriggerDisplayElement prepare(ResearchCriterion<ConsumeItemTrigger.TriggerInstance> criterion) {
        IndentedTextHolder textHolder = new IndentedTextHolder();

        PredicateHelper.optionalTooltip(criterion.conditions().player(), EntityPredicateHelper::tooltip, PLAYER_CONDITIONS_HEADER)
                .ifPresent(textHolder::accept);

        Optional<TriggerDisplayElement> itemElement = criterion.conditions().item().flatMap(ItemPredicateHelper::element);

        if (itemElement.isPresent()) {
            TriggerDisplayElement beforeElement = new TextElement(textHolder.isEmpty() ? TEXT_SPECIFIC_BEFORE : TEXT_SPECIFIC_BEFORE_WITH_CONDITIONS);
            TriggerDisplayElement afterElement = new TextElement(textHolder.isEmpty() ? TEXT_SPECIFIC_AFTER : TEXT_SPECIFIC_AFTER_WITH_CONDITIONS);
            if (!textHolder.isEmpty()) {
                beforeElement = beforeElement.withTextTooltip(textHolder.getText());
                afterElement = afterElement.withTextTooltip(textHolder.getText());
            }

            return new TriggerDisplay(
                    TriggerDisplay.makeCountElement(criterion),
                    beforeElement,
                    itemElement.get(),
                    afterElement
            );
        }
        else {
            TriggerDisplayElement element = new TextElement(textHolder.isEmpty() ? TEXT : TEXT_WITH_CONDITIONS);
            if (!textHolder.isEmpty()) element = element.withTextTooltip(textHolder.getText());

            return new TriggerDisplay(
                    TriggerDisplay.makeCountElement(criterion),
                    element
            );
        }
    }
}
