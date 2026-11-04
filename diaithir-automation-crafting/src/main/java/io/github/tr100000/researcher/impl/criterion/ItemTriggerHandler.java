package io.github.tr100000.researcher.impl.criterion;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.ResearchCriterion;
import io.github.tr100000.researcher.api.trigger.util.ItemPredicateHelper;
import io.github.tr100000.researcher.api.trigger.util.PredicateHelper;
import io.github.tr100000.researcher.api.util.IndentedTextHolder;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.ItemUsedOnLocationTrigger;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;

import java.util.Optional;

public class ItemTriggerHandler extends AbstractTriggerHandler<ItemUsedOnLocationTrigger.TriggerInstance> {
    public static final ItemTriggerHandler PLACED_BLOCK = new ItemTriggerHandler(ModUtils.getScreenTranslationKey("trigger.placed_block"));
    public static final ItemTriggerHandler ITEM_USED_ON_BLOCK = new ItemTriggerHandler(ModUtils.getScreenTranslationKey("trigger.item_used_on_block"));
    public static final ItemTriggerHandler ALLAY_DROP_ITEM_ON_BLOCK = new ItemTriggerHandler(ModUtils.getScreenTranslationKey("trigger.allay_drop_item_on_block"));

    private static final String BLOCK_KEY = ModUtils.getScreenTranslationKey("trigger.item.block");
    private static final Component BLOCK_PROPERTIES_TEXT = ModUtils.getScreenTranslated("trigger.item.block_properties");
    private static final Component TOOL_TEXT = ModUtils.getScreenTranslated("trigger.item.tool");

    public ItemTriggerHandler(String key) {
        super(key);
    }

    @Override
    protected void fillTooltip(ResearchCriterion<ItemUsedOnLocationTrigger.TriggerInstance> criterion, IndentedTextHolder textHolder) {
        PredicateHelper.optionalTooltip(criterion.conditions().location(), ItemTriggerHandler::locationTooltip, null)
                .ifPresent(textHolder::accept);
        playerTooltip(criterion, textHolder);
    }

    public static void locationTooltip(ContextAwarePredicate predicate, IndentedTextHolder textHolder) {
        predicate.conditions.forEach(condition -> {
            if (condition instanceof LootItemBlockStatePropertyCondition(Holder<Block> block, Optional<StatePropertiesPredicate> properties)) {
                textHolder.accept(Component.translatable(BLOCK_KEY, block.unwrapKey().orElseThrow().identifier()));
                PredicateHelper.optionalTooltip(properties, PredicateHelper::stateTooltip, BLOCK_PROPERTIES_TEXT)
                        .ifPresent(textHolder::accept);
            }
            else if (condition instanceof MatchTool(Optional<ItemPredicate> toolItem)) {
                PredicateHelper.optionalTooltip(toolItem, ItemPredicateHelper::tooltip, TOOL_TEXT)
                        .ifPresent(textHolder::accept);
            }
        });
    }
}
