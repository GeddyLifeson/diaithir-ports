package io.github.tr100000.researcher.impl.criterion;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.ResearchCriterion;
import io.github.tr100000.researcher.api.trigger.util.ItemPredicateHelper;
import io.github.tr100000.researcher.api.trigger.util.PredicateHelper;
import io.github.tr100000.researcher.api.util.IndentedTextHolder;
import net.minecraft.advancements.criterion.FilledBucketTrigger;
import net.minecraft.network.chat.Component;

public class FilledBucketTriggerHandler extends AbstractTriggerHandler<FilledBucketTrigger.TriggerInstance> {
    private static final Component RESULT_CONDITIONS_HEADER = ModUtils.getScreenTranslated("trigger.filled_bucket.result_conditions");

    public FilledBucketTriggerHandler() {
        super(ModUtils.getScreenTranslationKey("trigger.filled_bucket"));
    }

    @Override
    protected void fillTooltip(ResearchCriterion<FilledBucketTrigger.TriggerInstance> criterion, IndentedTextHolder textHolder) {
        PredicateHelper.optionalTooltip(criterion.conditions().item(), ItemPredicateHelper::tooltip, RESULT_CONDITIONS_HEADER)
                .ifPresent(textHolder::accept);
        playerTooltip(criterion, textHolder);
    }
}
