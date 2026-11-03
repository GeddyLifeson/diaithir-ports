package io.github.tr100000.researcher.impl.reward;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.api.reward.ResearchClientReward;
import io.github.tr100000.researcher.reward.ExperienceReward;
import io.github.tr100000.researcher.reward.FireworksReward;
import io.github.tr100000.researcher.reward.LootReward;
import io.github.tr100000.trutils.api.gui.ItemIcon;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.Items;

public final class ResearcherClientRewards {
    private ResearcherClientRewards() {}

    public static ResearchClientReward ofExperience(ExperienceReward reward) {
        return ResearchClientReward.of(
                new ItemIcon(Items.EXPERIENCE_BOTTLE),
                ClientTooltipComponent.create(ModUtils.getScreenTranslated("reward.experience.levels", reward.levels()).getVisualOrderText()),
                ClientTooltipComponent.create(ModUtils.getScreenTranslated("reward.experience.points", reward.points()).getVisualOrderText())
        );
    }

    public static ResearchClientReward ofFireworks(FireworksReward reward) {
        return ResearchClientReward.of(
                new ItemIcon(Items.FIREWORK_ROCKET),
                ClientTooltipComponent.create(ModUtils.getScreenTranslated("reward.fireworks", reward.amount()).getVisualOrderText())
        );
    }

    public static ResearchClientReward ofLoot(LootReward reward) {
        return ResearchClientReward.of(
                new ItemIcon(Items.CHEST),
                reward.loot().stream()
                        .map(key -> ClientTooltipComponent.create(ModUtils.getScreenTranslated("reward.loot", key.identifier()).getVisualOrderText()))
                        .toList()
        );
    }
}
