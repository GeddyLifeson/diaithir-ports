package io.github.tr100000.researcher.impl.criterion;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.ResearchCriterion;
import io.github.tr100000.researcher.api.trigger.TriggerDisplay;
import io.github.tr100000.researcher.api.trigger.TriggerDisplayElement;
import io.github.tr100000.researcher.api.trigger.TriggerHandler;
import io.github.tr100000.researcher.api.trigger.element.TextElement;
import io.github.tr100000.researcher.api.trigger.util.EntityPredicateHelper;
import io.github.tr100000.researcher.api.trigger.util.PredicateHelper;
import io.github.tr100000.researcher.api.util.IndentedTextHolder;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.network.chat.Component;

public abstract class AbstractTriggerHandler<T extends SimpleCriterionTrigger.SimpleInstance> implements TriggerHandler<T> {
    private static final Component PLAYER_CONDITIONS_HEADER = ModUtils.getScreenTranslated("predicate.player");

    private final Component text;
    private final Component textWithConditions;

    protected AbstractTriggerHandler(String textKey, String textWithConditionsKey) {
        this.text = Component.translatable(textKey);
        this.textWithConditions = Component.translatable(textWithConditionsKey);
    }

    protected AbstractTriggerHandler(String textKey) {
        this(textKey, textKey + ".with_conditions");
    }

    @Override
    public TriggerDisplayElement prepare(ResearchCriterion<T> criterion) {
        IndentedTextHolder textHolder = new IndentedTextHolder();

        fillTooltip(criterion, textHolder);

        TriggerDisplayElement element = new TextElement(textHolder.isEmpty() ? text : textWithConditions);
        if (!textHolder.isEmpty()) element = element.withTextTooltip(textHolder.getText());

        return new TriggerDisplay(
                TriggerDisplay.makeCountElement(criterion),
                element
        );
    }

    protected abstract void fillTooltip(ResearchCriterion<T> criterion, IndentedTextHolder textHolder);

    protected void playerTooltip(ResearchCriterion<T> criterion, IndentedTextHolder textHolder) {
        PredicateHelper.optionalTooltip(criterion.conditions().player(), EntityPredicateHelper::tooltip, PLAYER_CONDITIONS_HEADER)
                .ifPresent(textHolder::accept);
    }
}
