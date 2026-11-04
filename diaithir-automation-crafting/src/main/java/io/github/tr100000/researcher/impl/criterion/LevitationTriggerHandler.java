package io.github.tr100000.researcher.impl.criterion;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.ResearchCriterion;
import io.github.tr100000.researcher.api.trigger.util.DistancePredicateHelper;
import io.github.tr100000.researcher.api.trigger.util.MinMaxBoundsUtils;
import io.github.tr100000.researcher.api.trigger.util.PredicateHelper;
import io.github.tr100000.researcher.api.util.IndentedTextHolder;
import net.minecraft.advancements.criterion.LevitationTrigger;
import net.minecraft.network.chat.Component;

public class LevitationTriggerHandler extends AbstractTriggerHandler<LevitationTrigger.TriggerInstance> {
    public static final Component DISTANCE = ModUtils.getScreenTranslated("trigger.levitation.distance");
    public static final Component DURATION = ModUtils.getScreenTranslated("trigger.levitation.duration");

    public LevitationTriggerHandler() {
        super(ModUtils.getScreenTranslationKey("trigger.levitation"));
    }

    @Override
    protected void fillTooltip(ResearchCriterion<LevitationTrigger.TriggerInstance> criterion, IndentedTextHolder textHolder) {
        PredicateHelper.optionalTooltip(criterion.conditions().distance(), DistancePredicateHelper::tooltip, DISTANCE)
                .ifPresent(textHolder::accept);
        MinMaxBoundsUtils.tooltip(criterion.conditions().duration(), DURATION, textHolder);
        playerTooltip(criterion, textHolder);
    }
}
