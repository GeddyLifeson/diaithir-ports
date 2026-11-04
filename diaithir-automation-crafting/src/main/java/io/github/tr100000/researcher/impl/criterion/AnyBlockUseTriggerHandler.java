package io.github.tr100000.researcher.impl.criterion;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.ResearchCriterion;
import io.github.tr100000.researcher.api.trigger.util.PredicateHelper;
import io.github.tr100000.researcher.api.util.IndentedTextHolder;
import net.minecraft.advancements.criterion.AnyBlockInteractionTrigger;

public class AnyBlockUseTriggerHandler extends AbstractTriggerHandler<AnyBlockInteractionTrigger.TriggerInstance> {
    public AnyBlockUseTriggerHandler() {
        super(ModUtils.getScreenTranslationKey("trigger.any_block_use"));
    }

    @Override
    protected void fillTooltip(ResearchCriterion<AnyBlockInteractionTrigger.TriggerInstance> criterion, IndentedTextHolder textHolder) {
        PredicateHelper.optionalTooltip(criterion.conditions().location(), ItemTriggerHandler::locationTooltip, null)
                .ifPresent(textHolder::accept);
    }
}
