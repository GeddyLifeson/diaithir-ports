package cn.leolezury.eternalstarlight.common.item.loot;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

import java.util.Map;

public class ESLootContextParams {
	public static final LootContextParam<Map<Identifier, Integer>> BOSS_CHALLENGE_COUNTS = create("boss_challenge_counts");

	private static <T> LootContextParam<T> create(String id) {
		return new LootContextParam<>(Identifier.withDefaultNamespace(id));
	}
}
