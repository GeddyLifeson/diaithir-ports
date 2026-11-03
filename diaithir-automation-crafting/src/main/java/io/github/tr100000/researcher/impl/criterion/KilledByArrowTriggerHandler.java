package io.github.tr100000.researcher.impl.criterion;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.ResearchCriterion;
import io.github.tr100000.researcher.api.trigger.util.EntityPredicateHelper;
import io.github.tr100000.researcher.api.trigger.util.ItemPredicateHelper;
import io.github.tr100000.researcher.api.trigger.util.MinMaxBoundsUtils;
import io.github.tr100000.researcher.api.trigger.util.PredicateHelper;
import io.github.tr100000.researcher.api.util.IndentedTextHolder;
import net.minecraft.advancements.criterion.KilledByArrowTrigger;
import net.minecraft.network.chat.Component;

public class KilledByArrowTriggerHandler extends AbstractTriggerHandler<KilledByArrowTrigger.TriggerInstance> {
    private static final Component NUM_ENTITY_TYPES = ModUtils.getScreenTranslated("triger.killed_by_arrow.entity_type_count");
    private static final Component WEAPON_CONDITIONS_HEADER = ModUtils.getScreenTranslated("triger.killed_by_arrow.weapon");
    private static final Component VICTIM_CONDITIONS_HEADER = ModUtils.getScreenTranslated("triger.killed_by_arrow.victim");

    public KilledByArrowTriggerHandler() {
        super(ModUtils.getScreenTranslationKey("trigger.killed_by_arrow"));
    }

    @Override
    protected void fillTooltip(ResearchCriterion<KilledByArrowTrigger.TriggerInstance> criterion, IndentedTextHolder textHolder) {
        MinMaxBoundsUtils.tooltip(criterion.conditions().uniqueEntityTypes(), NUM_ENTITY_TYPES, textHolder);
        PredicateHelper.optionalTooltip(criterion.conditions().firedFromWeapon(), ItemPredicateHelper::tooltip, WEAPON_CONDITIONS_HEADER);
        playerTooltip(criterion, textHolder);
        criterion.conditions().victims().forEach(victim -> {
            textHolder.accept(VICTIM_CONDITIONS_HEADER);
            textHolder.push();
            EntityPredicateHelper.tooltip(victim, textHolder);
            textHolder.pop();
        });
    }
}
