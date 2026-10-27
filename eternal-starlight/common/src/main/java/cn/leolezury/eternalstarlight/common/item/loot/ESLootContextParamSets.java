package cn.leolezury.eternalstarlight.common.item.loot;

import net.minecraft.util.Util;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class ESLootContextParamSets {
	public static final ContextKeySet BOSS = Util.make(() -> new ContextKeySet.Builder()
		.required(LootContextParams.THIS_ENTITY)
		.required(LootContextParams.ORIGIN)
		.required(ESLootContextParams.BOSS_CHALLENGE_COUNTS)
		.build());
}
