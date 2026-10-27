package cn.leolezury.eternalstarlight.common.registry;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.item.loot.BossChallengeCountCondition;
import cn.leolezury.eternalstarlight.common.platform.registry.RegistrationProvider;
import cn.leolezury.eternalstarlight.common.platform.registry.RegistryObject;
import net.minecraft.core.registries.Registries;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ESLootItemConditions {
	public static final RegistrationProvider<MapCodec<? extends LootItemCondition>> LOOT_ITEM_CONDITIONS = RegistrationProvider.get(Registries.LOOT_CONDITION_TYPE, EternalStarlight.ID);
	public static final RegistryObject<MapCodec<? extends LootItemCondition>, MapCodec<BossChallengeCountCondition>> BOSS_CHALLENGE_COUNT = LOOT_ITEM_CONDITIONS.register("boss_challenge_count", () -> BossChallengeCountCondition.CODEC);

	public static void loadClass() {
	}
}
