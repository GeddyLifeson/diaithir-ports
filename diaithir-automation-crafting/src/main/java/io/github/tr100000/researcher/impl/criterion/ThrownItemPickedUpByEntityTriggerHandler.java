package io.github.tr100000.researcher.impl.criterion;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.ResearchCriterion;
import io.github.tr100000.researcher.api.trigger.TriggerDisplay;
import io.github.tr100000.researcher.api.trigger.TriggerDisplayElement;
import io.github.tr100000.researcher.api.trigger.TriggerHandler;
import io.github.tr100000.researcher.api.trigger.element.TextElement;
import io.github.tr100000.researcher.api.trigger.util.EntityPredicateHelper;
import io.github.tr100000.researcher.api.trigger.util.ItemPredicateHelper;
import io.github.tr100000.researcher.api.trigger.util.PredicateHelper;
import io.github.tr100000.researcher.api.util.IndentedTextHolder;
import net.minecraft.advancements.criterion.PickedUpItemTrigger;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public class ThrownItemPickedUpByEntityTriggerHandler implements TriggerHandler<PickedUpItemTrigger.TriggerInstance> {
    public static final ThrownItemPickedUpByEntityTriggerHandler THROWN_ITEM_PICKED_UP_BY_ENTITY = new ThrownItemPickedUpByEntityTriggerHandler(
            ModUtils.getScreenTranslationKey("trigger.thrown_item_picked_up_by_entity"),
            ModUtils.getScreenTranslationKey("trigger.thrown_item_picked_up_by_entity.specific.before"),
            ModUtils.getScreenTranslationKey("trigger.thrown_item_picked_up_by_entity.specific.after"),
            ModUtils.getScreenTranslationKey("trigger.thrown_item_picked_up_by_entity.entity")
    );

    public static final ThrownItemPickedUpByEntityTriggerHandler THROWN_ITEM_PICKED_UP_BY_PLAYER = new ThrownItemPickedUpByEntityTriggerHandler(
            ModUtils.getScreenTranslationKey("trigger.thrown_item_picked_up_by_player"),
            ModUtils.getScreenTranslationKey("trigger.thrown_item_picked_up_by_player.specific.before"),
            ModUtils.getScreenTranslationKey("trigger.thrown_item_picked_up_by_player.specific.after"),
            ModUtils.getScreenTranslationKey("trigger.thrown_item_picked_up_by_player.entity")
    );

    private static final Component ITEM_CONDITIONS_HEADER = ModUtils.getScreenTranslated("predicate.item");
    private static final Component PLAYER_CONDITIONS_HEADER = ModUtils.getScreenTranslated("predicate.player");

    private final Component entityConditionsHeader;
    private final Component text;
    private final Component textWithConditions;
    private final Component textSpecificBefore;
    private final Component textSpecificBeforeWithConditions;
    private final Component textSpecificAfter;
    private final Component textSpecificAfterWithConditions;

    public ThrownItemPickedUpByEntityTriggerHandler(String textKey, String textSpecificBeforeKey, String textSpecificAfterKey, String entityConditionsHeaderKey) {
        this.entityConditionsHeader = Component.translatable(entityConditionsHeaderKey);
        this.text = Component.translatable(textKey);
        this.textWithConditions = Component.translatable(textKey + ".with_conditions");
        this.textSpecificBefore = Component.translatable(textSpecificBeforeKey);
        this.textSpecificBeforeWithConditions = Component.translatable(textSpecificBeforeKey + ".with_conditions");
        this.textSpecificAfter = Component.translatable(textSpecificAfterKey);
        this.textSpecificAfterWithConditions = Component.translatable(textSpecificAfterKey + ".with_conditions");
    }

    @Override
    public TriggerDisplayElement prepare(ResearchCriterion<PickedUpItemTrigger.TriggerInstance> criterion) {
        IndentedTextHolder textHolder = new IndentedTextHolder();

        PredicateHelper.optionalTooltip(criterion.conditions().item(), ItemPredicateHelper::tooltip, ITEM_CONDITIONS_HEADER)
                .ifPresent(textHolder::accept);
        PredicateHelper.optionalTooltip(criterion.conditions().entity(), EntityPredicateHelper::tooltip, entityConditionsHeader)
                .ifPresent(textHolder::accept);
        PredicateHelper.optionalTooltip(criterion.conditions().player(), EntityPredicateHelper::tooltip, PLAYER_CONDITIONS_HEADER)
                .ifPresent(textHolder::accept);

        Optional<TriggerDisplayElement> itemElement = criterion.conditions().item().flatMap(ItemPredicateHelper::element);

        if (itemElement.isPresent()) {
            TriggerDisplayElement beforeElement = new TextElement(textHolder.isEmpty() ? textSpecificBefore : textSpecificBeforeWithConditions);
            TriggerDisplayElement afterElement = new TextElement(textHolder.isEmpty() ? textSpecificAfter : textSpecificAfterWithConditions);
            if (!textHolder.isEmpty()) {
                beforeElement = beforeElement.withTextTooltip(textHolder.getText());
                afterElement = afterElement.withTextTooltip(textHolder.getText());
            }

            return new TriggerDisplay(
                    TriggerDisplay.makeCountElement(criterion),
                    beforeElement,
                    itemElement.get(),
                    afterElement
            );
        }
        else {
            TriggerDisplayElement element = new TextElement(textHolder.isEmpty() ? text : textWithConditions);
            if (!textHolder.isEmpty()) element = element.withTextTooltip(textHolder.getText());

            return new TriggerDisplay(
                    TriggerDisplay.makeCountElement(criterion),
                    element
            );
        }
    }
}
