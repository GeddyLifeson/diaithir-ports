package io.github.tr100000.researcher;

import io.github.tr100000.researcher.api.ResearchRewardType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

public final class ResearcherRegistries {
    private ResearcherRegistries() {}

    public static final ResourceKey<Registry<ResearchRewardType<?>>> RESEARCH_REWARD_TYPE_KEY = ResourceKey.createRegistryKey(ModUtils.id("research_reward_type"));
    public static final Registry<ResearchRewardType<?>> RESEARCH_REWARD_TYPE = new RegistryBuilder<>(RESEARCH_REWARD_TYPE_KEY).sync(true).create();

    public static void onNewRegistry(NewRegistryEvent event) {
        event.register(RESEARCH_REWARD_TYPE);
    }
}
