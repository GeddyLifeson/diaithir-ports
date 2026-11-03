package io.github.tr100000.researcher.impl.criterion;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.ResearchCriterion;
import io.github.tr100000.researcher.api.trigger.util.MinMaxBoundsUtils;
import io.github.tr100000.researcher.api.util.IndentedTextHolder;
import net.minecraft.advancements.criterion.ConstructBeaconTrigger;
import net.minecraft.network.chat.Component;

public class ConstructBeaconTriggerHandler extends AbstractTriggerHandler<ConstructBeaconTrigger.TriggerInstance> {
    private static final Component BEACON_LEVEL = ModUtils.getScreenTranslated("trigger.construct_beacon.beacon_level");

    public ConstructBeaconTriggerHandler() {
        super(ModUtils.getScreenTranslationKey("trigger.construct_beacon"));
    }

    @Override
    protected void fillTooltip(ResearchCriterion<ConstructBeaconTrigger.TriggerInstance> criterion, IndentedTextHolder textHolder) {
        MinMaxBoundsUtils.tooltip(criterion.conditions().level(), BEACON_LEVEL, textHolder);
        playerTooltip(criterion, textHolder);
    }
}
