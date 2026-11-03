package io.github.tr100000.researcher.impl.criterion;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.ResearchCriterion;
import io.github.tr100000.researcher.api.trigger.util.DistancePredicateHelper;
import io.github.tr100000.researcher.api.trigger.util.LocationPredicateHelper;
import io.github.tr100000.researcher.api.trigger.util.PredicateHelper;
import io.github.tr100000.researcher.api.util.IndentedTextHolder;
import net.minecraft.advancements.criterion.DistanceTrigger;
import net.minecraft.network.chat.Component;

public class TravelTriggerHandler extends AbstractTriggerHandler<DistanceTrigger.TriggerInstance> {
    public static final TravelTriggerHandler NETHER_TRAVEL = new TravelTriggerHandler(ModUtils.getScreenTranslationKey("trigger.nether_travel"));
    public static final TravelTriggerHandler FALL_FROM_HEIGHT = new TravelTriggerHandler(ModUtils.getScreenTranslationKey("trigger.fall_from_height"));
    public static final TravelTriggerHandler RIDE_ENTITY_IN_LAVA = new TravelTriggerHandler(ModUtils.getScreenTranslationKey("trigger.ride_entity_in_lava"));

    private static final Component START_CONDITIONS_HEADER = ModUtils.getScreenTranslated("trigger.travel.start");
    private static final Component DISTANCE_CONDITIONS_HEADER = ModUtils.getScreenTranslated("trigger.travel.distance");

    public TravelTriggerHandler(String textKey) {
        super(textKey);
    }

    @Override
    protected void fillTooltip(ResearchCriterion<DistanceTrigger.TriggerInstance> criterion, IndentedTextHolder textHolder) {
        PredicateHelper.optionalTooltip(criterion.conditions().startPosition(), LocationPredicateHelper::tooltip, START_CONDITIONS_HEADER)
                .ifPresent(textHolder::accept);
        PredicateHelper.optionalTooltip(criterion.conditions().distance(), DistancePredicateHelper::tooltip, DISTANCE_CONDITIONS_HEADER)
                .ifPresent(textHolder::accept);
        playerTooltip(criterion, textHolder);
    }
}
