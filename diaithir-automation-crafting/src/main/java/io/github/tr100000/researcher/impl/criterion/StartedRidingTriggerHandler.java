package io.github.tr100000.researcher.impl.criterion;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.ResearchCriterion;
import io.github.tr100000.researcher.api.trigger.TriggerDisplay;
import io.github.tr100000.researcher.api.trigger.TriggerDisplayElement;
import io.github.tr100000.researcher.api.trigger.TriggerHandler;
import io.github.tr100000.researcher.api.trigger.element.TextElement;
import io.github.tr100000.researcher.api.trigger.util.EntityPredicateHelper;
import io.github.tr100000.researcher.api.trigger.util.PredicateHelper;
import io.github.tr100000.researcher.api.util.IndentedTextHolder;
import net.minecraft.advancements.criterion.StartRidingTrigger;
import net.minecraft.network.chat.Component;

public class StartedRidingTriggerHandler implements TriggerHandler<StartRidingTrigger.TriggerInstance> {
    private static final Component PLAYER_CONDITIONS_HEADER = ModUtils.getScreenTranslated("trigger.player");
    private static final Component TEXT_BEFORE = ModUtils.getScreenTranslated("trigger.started_riding.before");
    private static final Component TEXT_BEFORE_WITH_CONDITIONS = ModUtils.getScreenTranslated("trigger.started_riding.before.with_conditions");
    private static final Component TEXT_AFTER = ModUtils.getScreenTranslated("trigger.started_riding.after");
    private static final Component TEXT_AFTER_WITH_CONDITIONS = ModUtils.getScreenTranslated("trigger.started_riding.after.with_conditions");

    @Override
    public TriggerDisplayElement prepare(ResearchCriterion<StartRidingTrigger.TriggerInstance> criterion) {
        IndentedTextHolder playerTextHolder = new IndentedTextHolder();
        PredicateHelper.optionalTooltip(criterion.conditions().player(), EntityPredicateHelper::tooltip, PLAYER_CONDITIONS_HEADER)
                .ifPresent(playerTextHolder::accept);

        TriggerDisplayElement beforeText = new TextElement(playerTextHolder.isEmpty() ? TEXT_BEFORE : TEXT_BEFORE_WITH_CONDITIONS);
        TriggerDisplayElement afterText = new TextElement(playerTextHolder.isEmpty() ? TEXT_AFTER : TEXT_AFTER_WITH_CONDITIONS);

        if (!playerTextHolder.isEmpty()) {
            beforeText = beforeText.withTextTooltip(playerTextHolder.getText());
            afterText = afterText.withTextTooltip(playerTextHolder.getText());
        }

        TriggerDisplayElement entityElement = EntityPredicateHelper.vehicleElement(criterion.conditions().player().orElse(null));

        return new TriggerDisplay(
                TriggerDisplay.makeCountElement(criterion),
                beforeText,
                entityElement,
                afterText
        );
    }
}
