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
import net.minecraft.advancements.criterion.ChangeDimensionTrigger;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class ChangedDimensionTriggerHandler implements TriggerHandler<ChangeDimensionTrigger.TriggerInstance> {
    private static final Component PLAYER_CONDITIONS_HEADER = ModUtils.getScreenTranslated("predicate.player");
    private static final String TEXT = ModUtils.getScreenTranslationKey("trigger.changed_dimension");
    private static final String TEXT_WITH_CONDITIONS = ModUtils.getScreenTranslationKey("trigger.changed_dimension.with_conditions");
    private static final String FROM_TEXT = ModUtils.getScreenTranslationKey("trigger.changed_dimension.from");
    private static final String FROM_TEXT_WITH_CONDITIONS = ModUtils.getScreenTranslationKey("trigger.changed_dimension.from.with_conditions");
    private static final String TO_TEXT = ModUtils.getScreenTranslationKey("trigger.changed_dimension.to");
    private static final String TO_TEXT_WITH_CONDITIONS = ModUtils.getScreenTranslationKey("trigger.changed_dimension.to.with_conditions");
    private static final String ANY_TEXT = ModUtils.getScreenTranslationKey("trigger.changed_dimension.any");
    private static final String ANY_TEXT_WITH_CONDITIONS = ModUtils.getScreenTranslationKey("trigger.changed_dimension.any.with_conditions");

    @Override
    public TriggerDisplayElement prepare(ResearchCriterion<ChangeDimensionTrigger.TriggerInstance> criterion) {
        IndentedTextHolder textHolder = new IndentedTextHolder();

        PredicateHelper.optionalTooltip(criterion.conditions().player(), EntityPredicateHelper::tooltip, PLAYER_CONDITIONS_HEADER)
                .ifPresent(textHolder::accept);

        TriggerDisplayElement element;
        Optional<ResourceKey<Level>> worldFrom = criterion.conditions().from();
        Optional<ResourceKey<Level>> worldTo = criterion.conditions().to();
        if (worldFrom.isPresent() && worldTo.isPresent()) {
            element = new TextElement(Component.translatable(textHolder.isEmpty() ? TEXT : TEXT_WITH_CONDITIONS, worldFrom.get().identifier(), worldTo.get().identifier()));
        }
        else if (worldFrom.isPresent()) {
            element = new TextElement(Component.translatable(textHolder.isEmpty() ? FROM_TEXT : FROM_TEXT_WITH_CONDITIONS, worldFrom.get().identifier()));
        }
        else if (worldTo.isPresent()) {
            element = new TextElement(Component.translatable(textHolder.isEmpty() ? TO_TEXT : TO_TEXT_WITH_CONDITIONS, worldTo.get().identifier()));
        }
        else {
            element = new TextElement(Component.translatable(textHolder.isEmpty() ? ANY_TEXT : ANY_TEXT_WITH_CONDITIONS));
        }

        if (!textHolder.isEmpty()) element = element.withTextTooltip(textHolder.getText());

        return new TriggerDisplay(
                TriggerDisplay.makeCountElement(criterion),
                element
        );
    }
}
