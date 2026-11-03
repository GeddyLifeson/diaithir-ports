package io.github.tr100000.researcher;

import io.github.tr100000.researcher.api.ResearchRewardType;
import io.github.tr100000.researcher.reward.ExperienceReward;
import io.github.tr100000.researcher.reward.FireworksReward;
import io.github.tr100000.researcher.reward.LootReward;
import io.github.tr100000.trutils.api.registry.RegistryHelper;

public final class ResearcherRewardTypes {
    private ResearcherRewardTypes() {}

    private static final RegistryHelper<ResearchRewardType<?>> HELPER = new RegistryHelper<>(ResearcherRegistries.RESEARCH_REWARD_TYPE, Researcher.MODID);

    public static final ResearchRewardType<ExperienceReward> EXPERIENCE = HELPER.add(ExperienceReward.TYPE, "experience");
    public static final ResearchRewardType<FireworksReward> FIREWORKS = HELPER.add(FireworksReward.TYPE, "fireworks");
    public static final ResearchRewardType<LootReward> LOOT = HELPER.add(LootReward.TYPE, "loot");

    public static void flush() {
        HELPER.flush();
    }
}
