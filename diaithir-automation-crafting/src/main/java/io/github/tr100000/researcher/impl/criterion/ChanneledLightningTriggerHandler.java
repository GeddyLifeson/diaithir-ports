package io.github.tr100000.researcher.impl.criterion;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.ResearchCriterion;
import io.github.tr100000.researcher.api.trigger.util.EntityPredicateHelper;
import io.github.tr100000.researcher.api.util.IndentedTextHolder;
import net.minecraft.advancements.criterion.ChanneledLightningTrigger;
import net.minecraft.network.chat.Component;

public class ChanneledLightningTriggerHandler extends AbstractTriggerHandler<ChanneledLightningTrigger.TriggerInstance> {
    private static final Component VICTIM_CONDITIONS_HEADER = ModUtils.getScreenTranslated("trigger.channeled_lightning.victim");

    public ChanneledLightningTriggerHandler() {
        super(ModUtils.getScreenTranslationKey("trigger.channeled_lightning"));
    }

    @Override
    protected void fillTooltip(ResearchCriterion<ChanneledLightningTrigger.TriggerInstance> criterion, IndentedTextHolder textHolder) {
        criterion.conditions().victims().forEach(victim -> {
            textHolder.accept(VICTIM_CONDITIONS_HEADER);
            textHolder.push();
            EntityPredicateHelper.tooltip(victim, textHolder);
            textHolder.pop();
        });
        playerTooltip(criterion, textHolder);
    }
}
