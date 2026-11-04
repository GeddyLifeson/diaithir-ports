package io.github.tr100000.researcher.impl.criterion;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.ResearchCriterion;
import io.github.tr100000.researcher.api.trigger.util.DamagePredicateHelper;
import io.github.tr100000.researcher.api.trigger.util.PredicateHelper;
import io.github.tr100000.researcher.api.util.IndentedTextHolder;
import net.minecraft.advancements.criterion.EntityHurtPlayerTrigger;
import net.minecraft.network.chat.Component;

public class EntityHurtPlayerTriggerHandler extends AbstractTriggerHandler<EntityHurtPlayerTrigger.TriggerInstance> {
    private static final Component DAMAGE_CONDITIONS_HEADER = ModUtils.getScreenTranslated("predicate.damage");

    public EntityHurtPlayerTriggerHandler() {
        super(ModUtils.getScreenTranslationKey("trigger.entity_hurt_player"));
    }

    @Override
    protected void fillTooltip(ResearchCriterion<EntityHurtPlayerTrigger.TriggerInstance> criterion, IndentedTextHolder textHolder) {
        playerTooltip(criterion, textHolder);
        PredicateHelper.optionalTooltip(criterion.conditions().damage(), DamagePredicateHelper::tooltip, DAMAGE_CONDITIONS_HEADER)
                .ifPresent(textHolder::accept);
    }
}
