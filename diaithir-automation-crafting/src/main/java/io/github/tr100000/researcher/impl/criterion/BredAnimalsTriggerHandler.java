package io.github.tr100000.researcher.impl.criterion;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.ResearchCriterion;
import io.github.tr100000.researcher.api.trigger.util.EntityPredicateHelper;
import io.github.tr100000.researcher.api.trigger.util.PredicateHelper;
import io.github.tr100000.researcher.api.util.IndentedTextHolder;
import net.minecraft.advancements.criterion.BredAnimalsTrigger;
import net.minecraft.network.chat.Component;

public class BredAnimalsTriggerHandler extends AbstractTriggerHandler<BredAnimalsTrigger.TriggerInstance> {
    private static final Component PARENT_CONDITIONS_HEADER = ModUtils.getScreenTranslated("trigger.bred_animals.parent");
    private static final Component PARTNER_CONDITIONS_HEADER = ModUtils.getScreenTranslated("trigger.bred_animals.partner");
    private static final Component CHILD_CONDITIONS_HEADER = ModUtils.getScreenTranslated("trigger.bred_animals.child");

    public BredAnimalsTriggerHandler() {
        super(ModUtils.getScreenTranslationKey("trigger.bred_animals"));
    }

    @Override
    protected void fillTooltip(ResearchCriterion<BredAnimalsTrigger.TriggerInstance> criterion, IndentedTextHolder textHolder) {
        PredicateHelper.optionalTooltip(criterion.conditions().parent(), EntityPredicateHelper::tooltip, PARENT_CONDITIONS_HEADER)
                .ifPresent(textHolder::accept);
        PredicateHelper.optionalTooltip(criterion.conditions().partner(), EntityPredicateHelper::tooltip, PARTNER_CONDITIONS_HEADER)
                .ifPresent(textHolder::accept);
        PredicateHelper.optionalTooltip(criterion.conditions().child(), EntityPredicateHelper::tooltip, CHILD_CONDITIONS_HEADER)
                .ifPresent(textHolder::accept);
        playerTooltip(criterion, textHolder);
    }
}
