package io.github.tr100000.researcher.impl.criterion;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.ResearchCriterion;
import io.github.tr100000.researcher.api.trigger.TriggerDisplay;
import io.github.tr100000.researcher.api.trigger.TriggerDisplayElement;
import io.github.tr100000.researcher.api.trigger.TriggerHandler;
import io.github.tr100000.researcher.api.trigger.element.GroupedElement;
import io.github.tr100000.researcher.api.trigger.element.TextElement;
import io.github.tr100000.researcher.api.trigger.util.BlockPredicateHelper;
import io.github.tr100000.researcher.api.trigger.util.EntityPredicateHelper;
import io.github.tr100000.researcher.api.trigger.util.PredicateHelper;
import io.github.tr100000.researcher.api.util.IndentedTextHolder;
import net.minecraft.advancements.criterion.EnterBlockTrigger;
import net.minecraft.network.chat.Component;

public class EnterBlockTriggerHandler implements TriggerHandler<EnterBlockTrigger.TriggerInstance> {
    private static final Component PLAYER_CONDITIONS_HEADER = ModUtils.getScreenTranslated("trigger.player");
    private static final Component BLOCK_CONDITIONS_HEADER = ModUtils.getScreenTranslated("trigger.block");
    private static final Component BEFORE = ModUtils.getScreenTranslated("trigger.enter_block.before");
    private static final Component BEFORE_WITH_CONDITIONS = ModUtils.getScreenTranslated("trigger.enter_block.before.with_conditions");
    private static final Component AFTER = ModUtils.getScreenTranslated("trigger.enter_block.after");
    private static final Component AFTER_WITH_CONDITIONS = ModUtils.getScreenTranslated("trigger.enter_block.after.with_conditions");
    private static final Component ANY_BLOCK = ModUtils.getScreenTranslated("trigger.enter_block.any");

    @Override
    public TriggerDisplayElement prepare(ResearchCriterion<EnterBlockTrigger.TriggerInstance> criterion) {
        IndentedTextHolder playerConditionTextHolder = new IndentedTextHolder();
        PredicateHelper.optionalTooltip(criterion.conditions().player(), EntityPredicateHelper::tooltip, PLAYER_CONDITIONS_HEADER)
                .ifPresent(playerConditionTextHolder::accept);

        TriggerDisplayElement textBefore = new TextElement(playerConditionTextHolder.isEmpty() ? BEFORE : BEFORE_WITH_CONDITIONS);
        TriggerDisplayElement textAfter = new TextElement(playerConditionTextHolder.isEmpty() ? AFTER : AFTER_WITH_CONDITIONS);

        if (!playerConditionTextHolder.isEmpty()) {
            textBefore = textBefore.withTextTooltip(playerConditionTextHolder.getText());
            textAfter = textAfter.withTextTooltip(playerConditionTextHolder.getText());
        }

        IndentedTextHolder blockConditionTextHolder = new IndentedTextHolder();
        PredicateHelper.optionalTooltip(criterion.conditions().state(), PredicateHelper::stateTooltip, BLOCK_CONDITIONS_HEADER)
                .ifPresent(blockConditionTextHolder::accept);

        TriggerDisplayElement block = criterion.conditions().block().map(BlockPredicateHelper::element).orElseGet(() -> new TextElement(ANY_BLOCK));

        if (!blockConditionTextHolder.isEmpty()) {
            block = new GroupedElement(block, new TextElement(Component.literal("*"))).withTextTooltip(blockConditionTextHolder.getText());
        }

        return new TriggerDisplay(
                TriggerDisplay.makeCountElement(criterion),
                textBefore,
                block,
                textAfter
        );
    }
}
