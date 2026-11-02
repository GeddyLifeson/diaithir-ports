package io.github.tr100000.researcher.impl.criterion;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.ResearchCriterion;
import io.github.tr100000.researcher.api.trigger.util.EntityPredicateHelper;
import io.github.tr100000.researcher.api.trigger.util.MinMaxBoundsUtils;
import io.github.tr100000.researcher.api.trigger.util.PredicateHelper;
import io.github.tr100000.researcher.api.util.IndentedTextHolder;
import net.minecraft.advancements.criterion.TargetBlockTrigger;
import net.minecraft.network.chat.Component;

public class TargetHitTriggerHandler extends AbstractTriggerHandler<TargetBlockTrigger.TriggerInstance> {
    private static final Component SIGNAL_STRENGTH = ModUtils.getScreenTranslated("trigger.target_hit.signal_strength");
    private static final Component PROJECTILE_CONDITIONS_HEADER = ModUtils.getScreenTranslated("trigger.target_hit.projectile");

    public TargetHitTriggerHandler() {
        super(ModUtils.getScreenTranslationKey("trigger.target_hit"));
    }

    @Override
    protected void fillTooltip(ResearchCriterion<TargetBlockTrigger.TriggerInstance> criterion, IndentedTextHolder textHolder) {
        MinMaxBoundsUtils.tooltip(criterion.conditions().signalStrength(), SIGNAL_STRENGTH, textHolder);
        PredicateHelper.optionalTooltip(criterion.conditions().projectile(), EntityPredicateHelper::tooltip, PROJECTILE_CONDITIONS_HEADER)
                .ifPresent(textHolder::accept);
        playerTooltip(criterion, textHolder);
    }
}
