package cn.leolezury.eternalstarlight.common.item.loot;

import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;

import java.util.Map;

public class ESLootContextParams {
	public static final ContextKey<Map<Identifier, Integer>> BOSS_CHALLENGE_COUNTS = create("boss_challenge_counts");

	private static <T> ContextKey<T> create(String id) {
		return new ContextKey<>(Identifier.withDefaultNamespace(id));
	}
}
