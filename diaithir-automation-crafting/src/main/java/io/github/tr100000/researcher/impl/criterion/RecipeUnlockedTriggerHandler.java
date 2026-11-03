package io.github.tr100000.researcher.impl.criterion;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.ResearchCriterion;
import io.github.tr100000.researcher.api.trigger.TriggerDisplay;
import io.github.tr100000.researcher.api.trigger.TriggerDisplayElement;
import io.github.tr100000.researcher.api.trigger.TriggerHandler;
import io.github.tr100000.researcher.api.trigger.element.EmptyElement;
import io.github.tr100000.researcher.api.trigger.element.ItemElement;
import io.github.tr100000.researcher.api.trigger.element.TextElement;
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

public class RecipeUnlockedTriggerHandler implements TriggerHandler<RecipeUnlockedTrigger.TriggerInstance> {
    private static final String TEXT_KEY = ModUtils.getScreenTranslationKey("trigger.recipe_unlocked");

    @Override
    public TriggerDisplayElement prepare(ResearchCriterion<RecipeUnlockedTrigger.TriggerInstance> criterion) {
        return new TriggerDisplay(
                makeWarningElement(criterion),
                new TextElement(Component.translatable(TEXT_KEY, criterion.conditions().recipe().identifier()))
        );
    }

    private TriggerDisplayElement makeWarningElement(ResearchCriterion<RecipeUnlockedTrigger.TriggerInstance> criterion) {
        if (criterion.count() != 1) {
            return new ItemElement(Items.BARRIER, false).withTextTooltip(Component.literal("Criterion count should not 1, not " + criterion.count()));
        }
        if (criterion.conditions().player().isPresent()) {
            return new ItemElement(Items.BARRIER, false).withTextTooltip(Component.literal("Criterion should not have player conditions"));
        }

        return EmptyElement.INSTANCE;
    }
}
