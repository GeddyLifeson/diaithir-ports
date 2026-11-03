package io.github.tr100000.researcher.impl.criterion;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.ResearchCriterion;
import io.github.tr100000.researcher.api.trigger.util.MinMaxBoundsUtils;
import io.github.tr100000.researcher.api.util.IndentedTextHolder;
import net.minecraft.advancements.criterion.UsedEnderEyeTrigger;
import net.minecraft.network.chat.Component;

public class UsedEnderEyeTriggerHandler extends AbstractTriggerHandler<UsedEnderEyeTrigger.TriggerInstance> {
    private static final Component STRONGHOLD_DISTANCE = ModUtils.getScreenTranslated("trigger.used_ender_eye.stronghold_distance");

    public UsedEnderEyeTriggerHandler() {
        super(ModUtils.getScreenTranslationKey("trigger.used_ender_eye"));
    }

    @Override
    protected void fillTooltip(ResearchCriterion<UsedEnderEyeTrigger.TriggerInstance> criterion, IndentedTextHolder textHolder) {
        MinMaxBoundsUtils.tooltip(criterion.conditions().distance(), STRONGHOLD_DISTANCE, textHolder);
        playerTooltip(criterion, textHolder);
    }
}
