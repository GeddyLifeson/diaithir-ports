package io.github.tr100000.researcher.impl.criterion;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.ResearchCriterion;
import io.github.tr100000.researcher.api.trigger.util.EntityPredicateHelper;
import io.github.tr100000.researcher.api.trigger.util.PredicateHelper;
import io.github.tr100000.researcher.api.util.IndentedTextHolder;
import net.minecraft.advancements.criterion.CuredZombieVillagerTrigger;
import net.minecraft.network.chat.Component;

public class CuredZombieVillagerTriggerHandler extends AbstractTriggerHandler<CuredZombieVillagerTrigger.TriggerInstance> {
    private static final Component ZOMBIE_CONDITIONS_HEADER = ModUtils.getScreenTranslated("trigger.cured_zombie_villager.zombie");
    private static final Component VILLAGER_CONDITIONS_HEADER = ModUtils.getScreenTranslated("trigger.cured_zombie_villager.villager");

    public CuredZombieVillagerTriggerHandler() {
        super(ModUtils.getScreenTranslationKey("trigger.cured_zombie_villager"));
    }

    @Override
    protected void fillTooltip(ResearchCriterion<CuredZombieVillagerTrigger.TriggerInstance> criterion, IndentedTextHolder textHolder) {
        PredicateHelper.optionalTooltip(criterion.conditions().zombie(), EntityPredicateHelper::tooltip, ZOMBIE_CONDITIONS_HEADER)
                .ifPresent(textHolder::accept);
        PredicateHelper.optionalTooltip(criterion.conditions().villager(), EntityPredicateHelper::tooltip, VILLAGER_CONDITIONS_HEADER)
                .ifPresent(textHolder::accept);
        playerTooltip(criterion, textHolder);
    }
}
