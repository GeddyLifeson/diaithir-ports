package io.github.tr100000.researcher.impl.criterion;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.ResearchCriterion;
import io.github.tr100000.researcher.api.trigger.TriggerDisplay;
import io.github.tr100000.researcher.api.trigger.TriggerDisplayElement;
import io.github.tr100000.researcher.api.trigger.TriggerHandler;
import io.github.tr100000.researcher.api.trigger.element.EmptyElement;
import io.github.tr100000.researcher.api.trigger.element.ItemElement;
import io.github.tr100000.researcher.api.trigger.element.TextElement;
import io.github.tr100000.researcher.criterion.ResearchUnlockedTrigger;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

public class ResearchUnlockedTriggerHandler implements TriggerHandler<ResearchUnlockedTrigger.TriggerInstance> {
    private static final String TEXT_KEY = ModUtils.getScreenTranslationKey("trigger.has_research");

    @Override
    public TriggerDisplayElement prepare(ResearchCriterion<ResearchUnlockedTrigger.TriggerInstance> criterion) {
        return new TriggerDisplay(
                makeWarningElement(criterion),
                new TextElement(Component.translatable(TEXT_KEY, criterion.conditions().researchId().toLanguageKey("research")))
        );
    }

    private TriggerDisplayElement makeWarningElement(ResearchCriterion<ResearchUnlockedTrigger.TriggerInstance> criterion) {
        if (criterion.count() != 1) {
            return new ItemElement(Items.BARRIER, false).withTextTooltip(Component.literal("Criterion count should not 1, not " + criterion.count()));
        }
        if (criterion.conditions().player().isPresent()) {
            return new ItemElement(Items.BARRIER, false).withTextTooltip(Component.literal("Criterion should not have player conditions"));
        }

        return EmptyElement.INSTANCE;
    }
}
