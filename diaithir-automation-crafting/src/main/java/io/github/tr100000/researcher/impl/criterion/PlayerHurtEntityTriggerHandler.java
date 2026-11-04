package io.github.tr100000.researcher.impl.criterion;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.ResearchCriterion;
import io.github.tr100000.researcher.api.trigger.TriggerDisplay;
import io.github.tr100000.researcher.api.trigger.TriggerDisplayElement;
import io.github.tr100000.researcher.api.trigger.TriggerHandler;
import io.github.tr100000.researcher.api.trigger.element.TextElement;
import io.github.tr100000.researcher.api.trigger.util.DamagePredicateHelper;
import io.github.tr100000.researcher.api.trigger.util.EntityPredicateHelper;
import io.github.tr100000.researcher.api.trigger.util.PredicateHelper;
import io.github.tr100000.researcher.api.util.IndentedTextHolder;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.DamagePredicate;
import net.minecraft.advancements.criterion.PlayerHurtEntityTrigger;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public class PlayerHurtEntityTriggerHandler implements TriggerHandler<PlayerHurtEntityTrigger.TriggerInstance> {
    private static final Component PLAYER_CONDITIONS_HEADER = ModUtils.getScreenTranslated("predicate.player");
    private static final Component DAMAGE_CONDITIONS_HEADER = ModUtils.getScreenTranslated("predicate.damage");
    private static final Component ENTITY_CONDITIONS_HEADER = ModUtils.getScreenTranslated("predicate.entity");

    private static final Component BEFORE_TEXT = ModUtils.getScreenTranslated("trigger.player_hurt_entity.before");
    private static final Component BEFORE_WITH_CONDITIONS_TEXT = ModUtils.getScreenTranslated("trigger.player_hurt_entity.before_with_conditions");
    private static final Component AFTER_TEXT = ModUtils.getScreenTranslated("trigger.player_hurt_entity.after");
    private static final Component AFTER_WITH_CONDITIONS_TEXT = ModUtils.getScreenTranslated("trigger.player_hurt_entity.after_with_conditions");

    @Override
    public TriggerDisplayElement prepare(ResearchCriterion<PlayerHurtEntityTrigger.TriggerInstance> criterion) {
        Optional<ContextAwarePredicate> playerPredicate = criterion.conditions().player();
        Optional<DamagePredicate> damagePredicate = criterion.conditions().damage();
        Optional<ContextAwarePredicate> entityPredicate = criterion.conditions().entity();

        IndentedTextHolder conditionTextHolder = new IndentedTextHolder();
        PredicateHelper.optionalTooltip(playerPredicate, EntityPredicateHelper::tooltip, PLAYER_CONDITIONS_HEADER)
                .ifPresent(conditionTextHolder::accept);
        PredicateHelper.optionalTooltip(damagePredicate, DamagePredicateHelper::tooltip, DAMAGE_CONDITIONS_HEADER)
                .ifPresent(conditionTextHolder::accept);

        TriggerDisplayElement beforeText = new TextElement(conditionTextHolder.isEmpty() ? BEFORE_TEXT : BEFORE_WITH_CONDITIONS_TEXT);
        TriggerDisplayElement afterText = new TextElement(conditionTextHolder.isEmpty() ? AFTER_TEXT : AFTER_WITH_CONDITIONS_TEXT);
        if (!conditionTextHolder.isEmpty()) {
            beforeText = beforeText.withTextTooltip(conditionTextHolder.getText());
            afterText = afterText.withTextTooltip(conditionTextHolder.getText());
        }

        IndentedTextHolder entityConditionTextHolder = new IndentedTextHolder();
        PredicateHelper.optionalTooltip(entityPredicate, EntityPredicateHelper::tooltip, ENTITY_CONDITIONS_HEADER)
                .ifPresent(entityConditionTextHolder::accept);

        TriggerDisplayElement entityElement = EntityPredicateHelper.element(entityPredicate.orElse(null));
        if (!entityConditionTextHolder.isEmpty()) {
            entityElement = entityElement.withTextTooltip(entityConditionTextHolder.getText());
        }

        return new TriggerDisplay(
                TriggerDisplay.makeCountElement(criterion),
                beforeText,
                entityElement,
                afterText
        );
    }
}
