package io.github.tr100000.researcher.impl.criterion;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.ResearchCriterion;
import io.github.tr100000.researcher.api.trigger.util.ItemPredicateHelper;
import io.github.tr100000.researcher.api.trigger.util.MinMaxBoundsUtils;
import io.github.tr100000.researcher.api.trigger.util.PredicateHelper;
import io.github.tr100000.researcher.api.util.IndentedTextHolder;
import net.minecraft.advancements.criterion.ItemDurabilityTrigger;
import net.minecraft.network.chat.Component;

public class ItemDurabilityChangedTriggerHandler extends AbstractTriggerHandler<ItemDurabilityTrigger.TriggerInstance> {
    private static final Component DURABILITY = ModUtils.getScreenTranslated("trigger.item_durability_changed.durability");
    private static final Component DELTA = ModUtils.getScreenTranslated("trigger.item_durability_changed.item");
    private static final Component ITEM_CONDITIONS_HEADER = ModUtils.getScreenTranslated("trigger.item_durability_changed.item");

    public ItemDurabilityChangedTriggerHandler() {
        super(ModUtils.getScreenTranslationKey("trigger.item_durability_changed"));
    }

    @Override
    protected void fillTooltip(ResearchCriterion<ItemDurabilityTrigger.TriggerInstance> criterion, IndentedTextHolder textHolder) {
        MinMaxBoundsUtils.tooltip(criterion.conditions().durability(), DURABILITY, textHolder);
        MinMaxBoundsUtils.tooltip(criterion.conditions().delta(), DELTA, textHolder);
        PredicateHelper.optionalTooltip(criterion.conditions().item(), ItemPredicateHelper::tooltip, ITEM_CONDITIONS_HEADER)
                .ifPresent(textHolder::accept);
        playerTooltip(criterion, textHolder);
    }
}
