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
import net.minecraft.advancements.criterion.LootTableTrigger;
import net.minecraft.network.chat.Component;

public class PlayerGeneratesContainerLootTriggerHandler implements TriggerHandler<LootTableTrigger.TriggerInstance> {
    private static final Component PLAYER_CONDITIONS_HEADER = ModUtils.getScreenTranslated("trigger.player");
    private static final String TEXT_KEY = ModUtils.getScreenTranslationKey("trigger.player_generates_container_loot");
    private static final String TEXT_WITH_CONDITIONS_KEY = ModUtils.getScreenTranslationKey("trigger.player_generates_container_loot");

    @Override
    public TriggerDisplayElement prepare(ResearchCriterion<LootTableTrigger.TriggerInstance> criterion) {
        IndentedTextHolder textHolder = new IndentedTextHolder();

        PredicateHelper.optionalTooltip(criterion.conditions().player(), EntityPredicateHelper::tooltip, PLAYER_CONDITIONS_HEADER)
                .ifPresent(textHolder::accept);

        TriggerDisplayElement element = new TextElement(Component.translatable(textHolder.isEmpty() ? TEXT_KEY : TEXT_WITH_CONDITIONS_KEY, criterion.conditions().lootTable().identifier()));
        if (!textHolder.isEmpty()) element = element.withTextTooltip(textHolder.getText());

        return new TriggerDisplay(
                TriggerDisplay.makeCountElement(criterion),
                element
        );
    }
}
