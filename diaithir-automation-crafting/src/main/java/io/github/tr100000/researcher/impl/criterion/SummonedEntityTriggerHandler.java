package io.github.tr100000.researcher.impl.criterion;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.ResearchCriterion;
import io.github.tr100000.researcher.api.trigger.TriggerDisplay;
import io.github.tr100000.researcher.api.trigger.TriggerDisplayElement;
import io.github.tr100000.researcher.api.trigger.TriggerHandler;
import io.github.tr100000.researcher.api.trigger.element.GroupedElement;
import io.github.tr100000.researcher.api.trigger.element.TextElement;
import io.github.tr100000.researcher.api.trigger.util.EntityPredicateHelper;
import io.github.tr100000.researcher.api.trigger.util.PredicateHelper;
import io.github.tr100000.researcher.api.util.IndentedTextHolder;
import net.minecraft.advancements.criterion.SummonedEntityTrigger;
import net.minecraft.network.chat.Component;

public class SummonedEntityTriggerHandler implements TriggerHandler<SummonedEntityTrigger.TriggerInstance> {
    private static final Component PLAYER_CONDITIONS_HEADER = ModUtils.getScreenTranslated("predicate.player");
    private static final Component ENTITY_CONDITIONS_HEADER = ModUtils.getScreenTranslated("trigger.summoned_entity.spawned");
    private static final Component TEXT_BEFORE = ModUtils.getScreenTranslated("trigger.summoned_entity.before");
    private static final Component TEXT_BEFORE_WITH_CONDITIONS = ModUtils.getScreenTranslated("trigger.summoned_entity.before.with_conditions");
    private static final Component TEXT_AFTER = ModUtils.getScreenTranslated("trigger.summoned_entity.after");
    private static final Component TEXT_AFTER_WITH_CONDITIONS = ModUtils.getScreenTranslated("trigger.summoned_entity.after.with_conditions");

    @Override
    public TriggerDisplayElement prepare(ResearchCriterion<SummonedEntityTrigger.TriggerInstance> criterion) {
        IndentedTextHolder playerTextHolder = new IndentedTextHolder();
        PredicateHelper.optionalTooltip(criterion.conditions().player(), EntityPredicateHelper::tooltip, PLAYER_CONDITIONS_HEADER)
                .ifPresent(playerTextHolder::accept);

        TriggerDisplayElement beforeText = new TextElement(playerTextHolder.isEmpty() ? TEXT_BEFORE : TEXT_BEFORE_WITH_CONDITIONS);
        TriggerDisplayElement afterText = new TextElement(playerTextHolder.isEmpty() ? TEXT_AFTER : TEXT_AFTER_WITH_CONDITIONS);

        if (!playerTextHolder.isEmpty()) {
            beforeText = beforeText.withTextTooltip(playerTextHolder.getText());
            afterText = afterText.withTextTooltip(playerTextHolder.getText());
        }

        IndentedTextHolder entityTextHolder = new IndentedTextHolder();
        PredicateHelper.optionalTooltip(criterion.conditions().entity(), EntityPredicateHelper::tooltip, ENTITY_CONDITIONS_HEADER)
                .ifPresent(entityTextHolder::accept);

        TriggerDisplayElement entityElement = EntityPredicateHelper.element(criterion.conditions().entity().orElse(null));
        if (!entityTextHolder.isEmpty()) {
            entityElement = new GroupedElement(entityElement, new TextElement(Component.literal("*")));
            entityElement = entityElement.withTextTooltip(entityTextHolder.getText());
        }

        return new TriggerDisplay(
                TriggerDisplay.makeCountElement(criterion),
                beforeText,
                entityElement,
                afterText
        );
    }
}
