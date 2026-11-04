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
import net.minecraft.advancements.criterion.TradeTrigger;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public class TradeTriggerHandler implements TriggerHandler<TradeTrigger.TriggerInstance> {
    private static final Component ITEM_CONDITIONS_HEADER = ModUtils.getScreenTranslated("trigger.trade.item");
    private static final Component VILLAGER_CONDITIONS_HEADER = ModUtils.getScreenTranslated("trigger.trade.villager");
    private static final Component PLAYER_CONDITIONS_HEADER = ModUtils.getScreenTranslated("predicate.player");
    private static final Component TEXT = ModUtils.getScreenTranslated("trigger.trade");
    private static final Component TEXT_WITH_CONDITIONS = ModUtils.getScreenTranslated("trigger.trade.with_conditions");
    private static final Component TEXT_SPECIFIC_BEFORE = ModUtils.getScreenTranslated("trigger.trade.specific.before");
    private static final Component TEXT_SPECIFIC_BEFORE_WITH_CONDITIONS = ModUtils.getScreenTranslated("trigger.trade.specific.with_conditions.before");
    private static final Component TEXT_SPECIFIC_AFTER = ModUtils.getScreenTranslated("trigger.trade.specific.after");
    private static final Component TEXT_SPECIFIC_AFTER_WITH_CONDITIONS = ModUtils.getScreenTranslated("trigger.trade.specific.with_conditions.after");

    @Override
    public TriggerDisplayElement prepare(ResearchCriterion<TradeTrigger.TriggerInstance> criterion) {
        IndentedTextHolder textHolder = new IndentedTextHolder();

        PredicateHelper.optionalTooltip(criterion.conditions().item(), ItemPredicateHelper::tooltip, ITEM_CONDITIONS_HEADER)
                .ifPresent(textHolder::accept);
        PredicateHelper.optionalTooltip(criterion.conditions().villager(), EntityPredicateHelper::tooltip, VILLAGER_CONDITIONS_HEADER)
                .ifPresent(textHolder::accept);
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
