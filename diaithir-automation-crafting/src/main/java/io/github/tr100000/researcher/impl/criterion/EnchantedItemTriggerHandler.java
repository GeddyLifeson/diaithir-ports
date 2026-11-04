package io.github.tr100000.researcher.impl.criterion;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.ResearchCriterion;
import io.github.tr100000.researcher.api.trigger.util.ItemPredicateHelper;
import io.github.tr100000.researcher.api.trigger.util.MinMaxBoundsUtils;
import io.github.tr100000.researcher.api.trigger.util.PredicateHelper;
import io.github.tr100000.researcher.api.util.IndentedTextHolder;
import net.minecraft.advancements.criterion.EnchantedItemTrigger;
import net.minecraft.network.chat.Component;

public class EnchantedItemTriggerHandler extends AbstractTriggerHandler<EnchantedItemTrigger.TriggerInstance> {
    private static final Component ITEM_CONDITIONS_HEADER = ModUtils.getScreenTranslated("trigger.enchanted_item.result_conditions");
    private static final Component LEVEL_TEXT = ModUtils.getScreenTranslated("trigger.enchanted_item.level");

    public EnchantedItemTriggerHandler() {
        super(ModUtils.getScreenTranslationKey("trigger.enchanted_item"));
    }

    @Override
    protected void fillTooltip(ResearchCriterion<EnchantedItemTrigger.TriggerInstance> criterion, IndentedTextHolder textHolder) {
        PredicateHelper.optionalTooltip(criterion.conditions().item(), ItemPredicateHelper::tooltip, ITEM_CONDITIONS_HEADER)
                .ifPresent(textHolder::accept);
        MinMaxBoundsUtils.tooltip(criterion.conditions().levels(), LEVEL_TEXT, textHolder);
        playerTooltip(criterion, textHolder);
    }
}
