package io.github.tr100000.researcher.impl.criterion;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.ResearchCriterion;
import io.github.tr100000.researcher.api.trigger.util.PredicateHelper;
import io.github.tr100000.researcher.api.util.IndentedTextHolder;
import net.minecraft.advancements.criterion.DefaultBlockInteractionTrigger;

public class DefaultBlockUseTriggerHandler extends AbstractTriggerHandler<DefaultBlockInteractionTrigger.TriggerInstance> {
    public DefaultBlockUseTriggerHandler() {
        super(ModUtils.getScreenTranslationKey("trigger.default_block_use"));
    }

    @Override
    protected void fillTooltip(ResearchCriterion<DefaultBlockInteractionTrigger.TriggerInstance> criterion, IndentedTextHolder textHolder) {
        PredicateHelper.optionalTooltip(criterion.conditions().location(), ItemTriggerHandler::locationTooltip, null)
                .ifPresent(textHolder::accept);
        playerTooltip(criterion, textHolder);

    }
}
