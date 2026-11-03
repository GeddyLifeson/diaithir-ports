package io.github.tr100000.researcher.impl.criterion;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.ResearchCriterion;
import io.github.tr100000.researcher.api.trigger.TriggerDisplay;
import io.github.tr100000.researcher.api.trigger.TriggerDisplayElement;
import io.github.tr100000.researcher.api.trigger.TriggerHandler;
import io.github.tr100000.researcher.api.trigger.element.TextElement;
import io.github.tr100000.researcher.api.trigger.util.BlockPredicateHelper;
import io.github.tr100000.researcher.api.trigger.util.EntityPredicateHelper;
import io.github.tr100000.researcher.api.trigger.util.ItemPredicateHelper;
import io.github.tr100000.researcher.api.trigger.util.MinMaxBoundsUtils;
import io.github.tr100000.researcher.api.trigger.util.PredicateHelper;
import io.github.tr100000.researcher.api.util.IndentedTextHolder;
import net.minecraft.advancements.criterion.BeeNestDestroyedTrigger;
import net.minecraft.network.chat.Component;

public class BeeNestDestroyedTriggerHandler implements TriggerHandler<BeeNestDestroyedTrigger.TriggerInstance> {
    private static final Component ITEM_CONDITIONS_HEADER = ModUtils.getScreenTranslated("trigger.bee_nest_destroyed.item");
    private static final Component NUM_BEES_INSIDE = ModUtils.getScreenTranslated("trigger.bee_nest_destroyed.bees_inside");
    private static final Component PLAYER_CONDITIONS_HEADER = ModUtils.getScreenTranslated("trigger.player");
    private static final Component BEFORE_KEY = ModUtils.getScreenTranslated("trigger.bee_nest_destroyed.before");
    private static final Component BEFORE_WITH_CONDITIONS_KEY = ModUtils.getScreenTranslated("trigger.bee_nest_destroyed.before.with_conditions");
    private static final Component AFTER_KEY = ModUtils.getScreenTranslated("trigger.bee_nest_destroyed.after");
    private static final Component AFTER_WITH_CONDITIONS = ModUtils.getScreenTranslated("trigger.bee_nest_destroyed.after.with_conditions");
    private static final Component ANY_BLOCK = ModUtils.getScreenTranslated("trigger.bee_nest_destroyed.any");

    @Override
    public TriggerDisplayElement prepare(ResearchCriterion<BeeNestDestroyedTrigger.TriggerInstance> criterion) {
        IndentedTextHolder textHolder = new IndentedTextHolder();

        PredicateHelper.optionalTooltip(criterion.conditions().item(), ItemPredicateHelper::tooltip, ITEM_CONDITIONS_HEADER)
                    .ifPresent(textHolder::accept);
        MinMaxBoundsUtils.tooltip(criterion.conditions().beesInside(), NUM_BEES_INSIDE, textHolder);
        PredicateHelper.optionalTooltip(criterion.conditions().player(), EntityPredicateHelper::tooltip, PLAYER_CONDITIONS_HEADER)
                .ifPresent(textHolder::accept);

        TriggerDisplayElement textBefore = new TextElement(textHolder.isEmpty() ? BEFORE_KEY : BEFORE_WITH_CONDITIONS_KEY);
        TriggerDisplayElement textAfter = new TextElement(textHolder.isEmpty() ? AFTER_KEY : AFTER_WITH_CONDITIONS);

        if (!textHolder.isEmpty()) {
            textBefore = textBefore.withTextTooltip(textHolder.getText());
            textAfter = textAfter.withTextTooltip(textHolder.getText());
        }

        TriggerDisplayElement block = criterion.conditions().block().map(BlockPredicateHelper::element).orElseGet(() -> new TextElement(ANY_BLOCK));

        return new TriggerDisplay(
                TriggerDisplay.makeCountElement(criterion),
                textBefore,
                block,
                textAfter
        );
    }
}
