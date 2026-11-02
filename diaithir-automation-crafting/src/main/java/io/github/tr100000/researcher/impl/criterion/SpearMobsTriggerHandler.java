package io.github.tr100000.researcher.impl.criterion;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.ResearchCriterion;
import io.github.tr100000.researcher.api.util.IndentedTextHolder;
import net.minecraft.advancements.criterion.SpearMobsTrigger;
import net.minecraft.network.chat.Component;

public class SpearMobsTriggerHandler extends AbstractTriggerHandler<SpearMobsTrigger.TriggerInstance> {
    private static final String COUNT_KEY = ModUtils.getScreenTranslationKey("trigger.spear_mobs.count");

    public SpearMobsTriggerHandler() {
        super(ModUtils.getScreenTranslationKey("trigger.spear_mobs"));
    }

    @Override
    protected void fillTooltip(ResearchCriterion<SpearMobsTrigger.TriggerInstance> criterion, IndentedTextHolder textHolder) {
        if (criterion.conditions().count().isPresent()) {
            textHolder.accept(Component.translatable(COUNT_KEY, criterion.conditions().count().get()));
        }
        playerTooltip(criterion, textHolder);
    }
}
