package io.github.tr100000.researcher.impl.criterion;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.ResearchCriterion;
import io.github.tr100000.researcher.api.trigger.util.EntityPredicateHelper;
import io.github.tr100000.researcher.api.trigger.util.PredicateHelper;
import io.github.tr100000.researcher.api.util.IndentedTextHolder;
import net.minecraft.advancements.criterion.LightningStrikeTrigger;
import net.minecraft.network.chat.Component;

public class LightningStrikeTriggerHandler extends AbstractTriggerHandler<LightningStrikeTrigger.TriggerInstance> {
    private static final Component LIGHTNING_CONDITIONS_HEADER = ModUtils.getScreenTranslated("trigger.lightning_strike.lightning");
    private static final Component BYSTANDER_CONDITIONS_HEADER = ModUtils.getScreenTranslated("trigger.lightning_strike.bystander");

    public LightningStrikeTriggerHandler() {
        super(ModUtils.getScreenTranslationKey("trigger.lightning_strike"));
    }

    @Override
    protected void fillTooltip(ResearchCriterion<LightningStrikeTrigger.TriggerInstance> criterion, IndentedTextHolder textHolder) {
        PredicateHelper.optionalTooltip(criterion.conditions().lightning(), EntityPredicateHelper::tooltip, LIGHTNING_CONDITIONS_HEADER)
                .ifPresent(textHolder::accept);
        PredicateHelper.optionalTooltip(criterion.conditions().bystander(), EntityPredicateHelper::tooltip, BYSTANDER_CONDITIONS_HEADER)
                .ifPresent(textHolder::accept);
        playerTooltip(criterion, textHolder);
    }
}
