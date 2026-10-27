package cn.leolezury.eternalstarlight.common.item.loot;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public record BossChallengeCountCondition(Identifier bossId, Optional<Integer> minInclusive, Optional<Integer> maxInclusive) implements LootItemCondition {
	public static final MapCodec<BossChallengeCountCondition> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
		Identifier.CODEC.fieldOf("boss_id").forGetter(BossChallengeCountCondition::bossId),
		Codec.INT.optionalFieldOf("min_inclusive").forGetter(BossChallengeCountCondition::minInclusive),
		Codec.INT.optionalFieldOf("max_inclusive").forGetter(BossChallengeCountCondition::maxInclusive)
	).apply(instance, BossChallengeCountCondition::new));

	@Override
	public MapCodec<BossChallengeCountCondition> codec() {
		return CODEC;
	}

	@Override
	public Set<ContextKey<?>> getReferencedContextParams() {
		return ImmutableSet.of(ESLootContextParams.BOSS_CHALLENGE_COUNTS);
	}

	@Override
	public boolean test(LootContext context) {
		Map<Identifier, Integer> counts = context.getOptionalParameter(ESLootContextParams.BOSS_CHALLENGE_COUNTS);
		if (counts != null) {
			int count = counts.getOrDefault(bossId, 0);
			boolean result = true;
			if (minInclusive.isPresent()) {
				result = count >= minInclusive.get();
			}
			if (maxInclusive.isPresent()) {
				result = result && count <= maxInclusive.get();
			}
			return result;
		}
		return false;
	}

	public static LootItemCondition.Builder min(Identifier bossId, int min) {
		return () -> new BossChallengeCountCondition(bossId, Optional.of(min), Optional.empty());
	}

	public static LootItemCondition.Builder max(Identifier bossId, int max) {
		return () -> new BossChallengeCountCondition(bossId, Optional.empty(), Optional.of(max));
	}

	public static LootItemCondition.Builder range(Identifier bossId, int min, int max) {
		return () -> new BossChallengeCountCondition(bossId, Optional.of(min), Optional.of(max));
	}
}
