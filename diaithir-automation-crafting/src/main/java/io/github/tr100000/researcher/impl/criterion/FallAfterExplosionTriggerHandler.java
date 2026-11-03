package io.github.tr100000.researcher.impl.criterion;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.ResearchCriterion;
import io.github.tr100000.researcher.api.trigger.util.DistancePredicateHelper;
import io.github.tr100000.researcher.api.trigger.util.EntityPredicateHelper;
import io.github.tr100000.researcher.api.trigger.util.LocationPredicateHelper;
import io.github.tr100000.researcher.api.trigger.util.PredicateHelper;
import io.github.tr100000.researcher.api.util.IndentedTextHolder;
import net.minecraft.advancements.criterion.FallAfterExplosionTrigger;
import net.minecraft.network.chat.Component;

public class FallAfterExplosionTriggerHandler extends AbstractTriggerHandler<FallAfterExplosionTrigger.TriggerInstance> {
    private static final Component START_CONDITIONS_HEADER = ModUtils.getScreenTranslated("trigger.fall_after_explosion.start");
    private static final Component DISTANCE_CONDITIONS_HEADER = ModUtils.getScreenTranslated("trigger.fall_after_explosion.distance");
    private static final Component CAUSE_CONDITIONS_HEADER = ModUtils.getScreenTranslated("trigger.fall_after_explosion.cause");

    public FallAfterExplosionTriggerHandler() {
        super(ModUtils.getScreenTranslationKey("trigger.fall_after_explosion"));
    }

    @Override
    protected void fillTooltip(ResearchCriterion<FallAfterExplosionTrigger.TriggerInstance> criterion, IndentedTextHolder textHolder) {
        PredicateHelper.optionalTooltip(criterion.conditions().startPosition(), LocationPredicateHelper::tooltip, START_CONDITIONS_HEADER)
                .ifPresent(textHolder::accept);
        PredicateHelper.optionalTooltip(criterion.conditions().distance(), DistancePredicateHelper::tooltip, DISTANCE_CONDITIONS_HEADER)
                .ifPresent(textHolder::accept);
        PredicateHelper.optionalTooltip(criterion.conditions().cause(), EntityPredicateHelper::tooltip, CAUSE_CONDITIONS_HEADER)
                .ifPresent(textHolder::accept);
        playerTooltip(criterion, textHolder);
    }
}
