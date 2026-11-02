package io.github.tr100000.researcher.impl.criterion;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.ResearchCriterion;
import io.github.tr100000.researcher.api.trigger.util.ItemPredicateHelper;
import io.github.tr100000.researcher.api.trigger.util.PredicateHelper;
import io.github.tr100000.researcher.api.util.IndentedTextHolder;
import net.minecraft.advancements.criterion.UsedTotemTrigger;

public class UsedTotemTriggerHandler extends AbstractTriggerHandler<UsedTotemTrigger.TriggerInstance> {
    public UsedTotemTriggerHandler() {
        super(ModUtils.getScreenTranslationKey("trigger.used_totem"));
    }

    @Override
    protected void fillTooltip(ResearchCriterion<UsedTotemTrigger.TriggerInstance> criterion, IndentedTextHolder textHolder) {
        PredicateHelper.optionalTooltip(criterion.conditions().item(), ItemPredicateHelper::tooltip, null)
                .ifPresent(textHolder::accept);
        playerTooltip(criterion, textHolder);
    }
}
