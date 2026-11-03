package io.github.tr100000.researcher.impl.criterion;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.ResearchCriterion;
import io.github.tr100000.researcher.api.trigger.util.EntityPredicateHelper;
import io.github.tr100000.researcher.api.trigger.util.PredicateHelper;
import io.github.tr100000.researcher.api.util.IndentedTextHolder;
import net.minecraft.advancements.criterion.EffectsChangedTrigger;
import net.minecraft.network.chat.Component;

public class EffectsChangedTriggerHandler extends AbstractTriggerHandler<EffectsChangedTrigger.TriggerInstance> {
    private static final Component SOURCE_CONDITIONS_HEADER = ModUtils.getScreenTranslated("trigger.effects_changed.source");

    public EffectsChangedTriggerHandler() {
        super(ModUtils.getScreenTranslationKey("trigger.effects_changed"));
    }

    @Override
    protected void fillTooltip(ResearchCriterion<EffectsChangedTrigger.TriggerInstance> criterion, IndentedTextHolder textHolder) {
        PredicateHelper.optionalTooltip(criterion.conditions().effects(), EntityPredicateHelper::effectTooltip, null)
                .ifPresent(textHolder::accept);
        PredicateHelper.optionalTooltip(criterion.conditions().source(), EntityPredicateHelper::tooltip, SOURCE_CONDITIONS_HEADER)
                .ifPresent(textHolder::accept);
        playerTooltip(criterion, textHolder);
    }
}
