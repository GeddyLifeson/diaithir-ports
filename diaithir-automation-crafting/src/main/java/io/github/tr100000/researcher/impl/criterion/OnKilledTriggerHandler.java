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
import net.minecraft.advancements.criterion.DamageSourcePredicate;
import net.minecraft.advancements.criterion.KilledTrigger;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public class OnKilledTriggerHandler implements TriggerHandler<KilledTrigger.TriggerInstance> {
    public static final OnKilledTriggerHandler PLAYER_KILLED_ENTITY = new OnKilledTriggerHandler(
            ModUtils.getScreenTranslationKey("trigger.player_killed_entity.before"),
            ModUtils.getScreenTranslationKey("trigger.player_killed_entity.after")
    );

    public static final OnKilledTriggerHandler ENTITY_KILLED_PLAYER = new OnKilledTriggerHandler(
            ModUtils.getScreenTranslationKey("trigger.player_killed_entity.before"),
            ModUtils.getScreenTranslationKey("trigger.player_killed_entity.after")
    );

    public static final OnKilledTriggerHandler KILL_MOB_NEAR_SKULK_CATALYST = new OnKilledTriggerHandler(
            ModUtils.getScreenTranslationKey("trigger.kill_mob_near_skulk_catalyst.before"),
            ModUtils.getScreenTranslationKey("trigger.kill_mob_near_skulk_catalyst.after")
    );

    private final String beforeKey;
    private final String beforeWithConditionsKey;
    private final String afterKey;
    private final String afterWithConditionsKey;

    private static final Component PLAYER_CONDITIONS_HEADER = ModUtils.getScreenTranslated("predicate.player");
    private static final Component KILLING_BLOW_CONDITIONS_HEADER = ModUtils.getScreenTranslated("predicate.kill.damage_source");
    private static final Component ENTITY_CONDITIONS_HEADER = ModUtils.getScreenTranslated("predicate.entity");

    public OnKilledTriggerHandler(String beforeKey, String beforeWithConditionsKey, String afterKey, String afterWithConditionsKey) {
        this.beforeKey = beforeKey;
        this.beforeWithConditionsKey = beforeWithConditionsKey;
        this.afterKey = afterKey;
        this.afterWithConditionsKey = afterWithConditionsKey;
    }

    public OnKilledTriggerHandler(String beforeKey, String afterKey) {
        this(
                beforeKey,
                beforeKey + ".with_conditions",
                afterKey,
                afterKey + ".with_conditions"
        );
    }

    @Override
    public TriggerDisplayElement prepare(ResearchCriterion<KilledTrigger.TriggerInstance> criterion) {
        Optional<ContextAwarePredicate> playerPredicate = criterion.conditions().player();
        Optional<DamageSourcePredicate> killingBlowPredicate = criterion.conditions().killingBlow();
        Optional<ContextAwarePredicate> entityPredicate = criterion.conditions().entity();

        IndentedTextHolder killConditionTextHolder = new IndentedTextHolder();
        PredicateHelper.optionalTooltip(playerPredicate, EntityPredicateHelper::tooltip, PLAYER_CONDITIONS_HEADER)
                .ifPresent(killConditionTextHolder::accept);
        PredicateHelper.optionalTooltip(killingBlowPredicate, DamagePredicateHelper::tooltip, KILLING_BLOW_CONDITIONS_HEADER)
                .ifPresent(killConditionTextHolder::accept);

        boolean hasKillConditions = !killConditionTextHolder.isEmpty();
        TriggerDisplayElement beforeText = new TextElement(Component.translatable(hasKillConditions ? beforeWithConditionsKey : beforeKey));
        TriggerDisplayElement afterText = new TextElement(Component.translatable(hasKillConditions ? afterWithConditionsKey : afterKey));
        if (hasKillConditions) {
            beforeText = beforeText.withTextTooltip(killConditionTextHolder.getText());
            afterText = afterText.withTextTooltip(killConditionTextHolder.getText());
        }

        TriggerDisplayElement entityElement = EntityPredicateHelper.element(entityPredicate.orElse(null));
        IndentedTextHolder entityConditionTextHolder = new IndentedTextHolder();
        PredicateHelper.optionalTooltip(entityPredicate, EntityPredicateHelper::tooltip, ENTITY_CONDITIONS_HEADER)
                .ifPresent(entityConditionTextHolder::accept);
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
