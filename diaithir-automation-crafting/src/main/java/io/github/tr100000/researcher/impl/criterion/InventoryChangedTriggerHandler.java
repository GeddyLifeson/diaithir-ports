package io.github.tr100000.researcher.impl.criterion;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.ResearchCriterion;
import io.github.tr100000.researcher.api.trigger.util.ItemPredicateHelper;
import io.github.tr100000.researcher.api.trigger.util.MinMaxBoundsUtils;
import io.github.tr100000.researcher.api.util.IndentedTextHolder;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.network.chat.Component;

public class InventoryChangedTriggerHandler extends AbstractTriggerHandler<InventoryChangeTrigger.TriggerInstance> {
    private static final Component ITEM_LIST = ModUtils.getScreenTranslated("trigger.inventory_changed.items");
    private static final Component INVENTORY_OCCUPIED_SLOTS = ModUtils.getScreenTranslated("trigger.inventory_changed.occupied_slots");
    private static final Component INVENTORY_FULL_SLOTS = ModUtils.getScreenTranslated("trigger.inventory_changed.full_slots");
    private static final Component INVENTORY_EMPTY_SLOTS = ModUtils.getScreenTranslated("trigger.inventory_changed.empty_slots");

    public InventoryChangedTriggerHandler() {
        super(ModUtils.getScreenTranslationKey("trigger.inventory_changed"));
    }

    @Override
    protected void fillTooltip(ResearchCriterion<InventoryChangeTrigger.TriggerInstance> criterion, IndentedTextHolder textHolder) {
        if (!criterion.conditions().items().isEmpty()) {
            textHolder.accept(ITEM_LIST);
            textHolder.push();
            criterion.conditions().items().forEach(itemPredicate -> ItemPredicateHelper.tooltip(itemPredicate, textHolder));
            textHolder.pop();
        }

        playerTooltip(criterion, textHolder);

        if (!criterion.conditions().items().isEmpty()) {
            slotConditionsTooltip(criterion.conditions().slots(), textHolder);
        }
    }

    private void slotConditionsTooltip(InventoryChangeTrigger.TriggerInstance.Slots slots, IndentedTextHolder textHolder) {
        MinMaxBoundsUtils.tooltip(slots.occupied(), INVENTORY_OCCUPIED_SLOTS, textHolder);
        MinMaxBoundsUtils.tooltip(slots.full(), INVENTORY_FULL_SLOTS, textHolder);
        MinMaxBoundsUtils.tooltip(slots.empty(), INVENTORY_EMPTY_SLOTS, textHolder);
    }
}
