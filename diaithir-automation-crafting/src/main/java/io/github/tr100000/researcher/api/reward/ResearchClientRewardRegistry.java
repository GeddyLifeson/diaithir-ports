package io.github.tr100000.researcher.api.reward;

import io.github.tr100000.researcher.Researcher;
import io.github.tr100000.researcher.ResearcherRegistries;
import io.github.tr100000.researcher.api.ResearchReward;
import io.github.tr100000.researcher.api.ResearchRewardType;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public final class ResearchClientRewardRegistry {
    private ResearchClientRewardRegistry() {}

    private static final Map<ResearchRewardType<?>, Function<ResearchReward, ResearchClientReward>> REGISTRY = new Object2ObjectOpenHashMap<>();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends ResearchReward> void register(ResearchRewardType<T> type, Function<T, ResearchClientReward> factory) {
        Objects.requireNonNull(type, "type is null");
        Objects.requireNonNull(factory, "factory is null");
        REGISTRY.put(type, (Function) factory);
    }

    @Contract(value = "null -> null", pure = true)
    public static @Nullable ResearchClientReward get(@Nullable ResearchReward reward) {
        if (reward != null && REGISTRY.containsKey(reward.getType())) {
            return REGISTRY.get(reward.getType()).apply(reward);
        }
        return null;
    }

    @ApiStatus.Internal
    public static void printNonRegistered() {
        ResearcherRegistries.RESEARCH_REWARD_TYPE.forEach(type -> {
            if (!REGISTRY.containsKey(type)) {
                Researcher.LOGGER.warn("Reward type {} does not have a registered handler", ResearcherRegistries.RESEARCH_REWARD_TYPE.getKey(type));
            }
        });
    }
}
