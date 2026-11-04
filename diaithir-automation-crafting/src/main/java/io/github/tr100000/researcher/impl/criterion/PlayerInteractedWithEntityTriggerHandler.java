package io.github.tr100000.researcher.impl.criterion;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.ResearchCriterion;
import io.github.tr100000.researcher.api.trigger.TriggerDisplay;
import io.github.tr100000.researcher.api.trigger.TriggerDisplayElement;
import io.github.tr100000.researcher.api.trigger.TriggerHandler;
import io.github.tr100000.researcher.api.trigger.element.GroupedElement;
import io.github.tr100000.researcher.api.trigger.element.TextElement;
import io.github.tr100000.researcher.api.trigger.util.EntityPredicateHelper;
import io.github.tr100000.researcher.api.trigger.util.ItemPredicateHelper;
import io.github.tr100000.researcher.api.trigger.util.PredicateHelper;
import io.github.tr100000.researcher.api.util.IndentedTextHolder;
import net.minecraft.advancements.criterion.PlayerInteractTrigger;
import net.minecraft.network.chat.Component;

public class PlayerInteractedWithEntityTriggerHandler implements TriggerHandler<PlayerInteractTrigger.TriggerInstance> {
    public static final PlayerInteractedWithEntityTriggerHandler PLAYER_INTERACTED_WITH_ENTITY = new PlayerInteractedWithEntityTriggerHandler(
            ModUtils.getScreenTranslationKey("trigger.player_interacted_with_entity.before"),
            ModUtils.getScreenTranslationKey("trigger.player_interacted_with_entity.after"),
            ModUtils.getScreenTranslationKey("trigger.player_interacted_with_entity.item")
    );

    public static final PlayerInteractedWithEntityTriggerHandler PLAYER_SHEARED_EQUIPMENT = new PlayerInteractedWithEntityTriggerHandler(
            ModUtils.getScreenTranslationKey("trigger.player_sheared_equipment.before"),
            ModUtils.getScreenTranslationKey("trigger.player_sheared_equipment.after"),
            ModUtils.getScreenTranslationKey("trigger.player_sheared_equipment.item")
    );

    private static final Component PLAYER_CONDITIONS_HEADER = ModUtils.getScreenTranslated("predicate.player");
    private static final Component ENTITY_CONDITIONS_HEADER = ModUtils.getScreenTranslated("trigger.summoned_entity.spawned");

    private final Component itemConditionsHeader;
    private final Component textBefore;
    private final Component textBeforeWithConditions;
    private final Component textAfter;
    private final Component textAfterWithConditions;

    public PlayerInteractedWithEntityTriggerHandler(String textBeforeKey, String textAfterKey, String itemConditionsHeaderKey) {
        itemConditionsHeader = Component.translatable(itemConditionsHeaderKey);
        textBefore = Component.translatable(textBeforeKey);
        textBeforeWithConditions = Component.translatable(textBeforeKey + ".with_conditions");
        textAfter = Component.translatable(textAfterKey);
        textAfterWithConditions = Component.translatable(textAfterKey + ".with_conditions");
    }

    @Override
    public TriggerDisplayElement prepare(ResearchCriterion<PlayerInteractTrigger.TriggerInstance> criterion) {
        IndentedTextHolder playerTextHolder = new IndentedTextHolder();
        PredicateHelper.optionalTooltip(criterion.conditions().item(), ItemPredicateHelper::tooltip, itemConditionsHeader)
                .ifPresent(playerTextHolder::accept);
        PredicateHelper.optionalTooltip(criterion.conditions().player(), EntityPredicateHelper::tooltip, PLAYER_CONDITIONS_HEADER)
                .ifPresent(playerTextHolder::accept);

        TriggerDisplayElement beforeText = new TextElement(playerTextHolder.isEmpty() ? textBefore : textBeforeWithConditions);
        TriggerDisplayElement afterText = new TextElement(playerTextHolder.isEmpty() ? textAfter : textAfterWithConditions);

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
