package io.github.tr100000.researcher.impl.criterion;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.ResearchCriterion;
import io.github.tr100000.researcher.api.trigger.TriggerDisplay;
import io.github.tr100000.researcher.api.trigger.TriggerDisplayElement;
import io.github.tr100000.researcher.api.trigger.TriggerHandler;
import io.github.tr100000.researcher.api.trigger.element.TextElement;
import io.github.tr100000.researcher.api.trigger.util.BlockPredicateHelper;
import io.github.tr100000.researcher.api.trigger.util.EntityPredicateHelper;
import io.github.tr100000.researcher.api.trigger.util.PredicateHelper;
import io.github.tr100000.researcher.api.util.IndentedTextHolder;
import io.github.tr100000.researcher.criterion.BlockBrokenTrigger;
import net.minecraft.network.chat.Component;

public class BlockBrokenTriggerHandler implements TriggerHandler<BlockBrokenTrigger.Conditions> {
    private static final Component BLOCK_CONDITIONS_HEADER = ModUtils.getScreenTranslated("trigger.block");
    private static final Component PLAYER_CONDITIONS_HEADER = ModUtils.getScreenTranslated("trigger.player");
    private static final Component TEXT_BEFORE = ModUtils.getScreenTranslated("trigger.block_broken.before");
    private static final Component TEXT_BEFORE_WITH_CONDITIONS = ModUtils.getScreenTranslated("trigger.block_broken.before.with_conditions");
    private static final Component TEXT_AFTER = ModUtils.getScreenTranslated("trigger.block_broken.after");
    private static final Component TEXT_AFTER_WITH_CONDITIONS = ModUtils.getScreenTranslated("trigger.block_broken.after.with_conditions");

    @Override
    public TriggerDisplayElement prepare(ResearchCriterion<BlockBrokenTrigger.Conditions> criterion) {
        IndentedTextHolder textHolder = new IndentedTextHolder();

        PredicateHelper.optionalTooltip(criterion.conditions().state(), PredicateHelper::stateTooltip, BLOCK_CONDITIONS_HEADER)
                .ifPresent(textHolder::accept);
        PredicateHelper.optionalTooltip(criterion.conditions().player(), EntityPredicateHelper::tooltip, PLAYER_CONDITIONS_HEADER)
                .ifPresent(textHolder::accept);

        TriggerDisplayElement beforeText = new TextElement(textHolder.isEmpty() ? TEXT_BEFORE : TEXT_BEFORE_WITH_CONDITIONS);
        TriggerDisplayElement afterText = new TextElement(textHolder.isEmpty() ? TEXT_AFTER : TEXT_AFTER_WITH_CONDITIONS);
        if (!textHolder.isEmpty()) {
            beforeText = beforeText.withTextTooltip(textHolder.getText());
            afterText = afterText.withTextTooltip(textHolder.getText());
        }

        TriggerDisplayElement blockElement = BlockPredicateHelper.element(criterion.conditions().block().orElse(null));

        return new TriggerDisplay(
                TriggerDisplay.makeCountElement(criterion),
                beforeText,
                blockElement,
                afterText
        );
    }
}
