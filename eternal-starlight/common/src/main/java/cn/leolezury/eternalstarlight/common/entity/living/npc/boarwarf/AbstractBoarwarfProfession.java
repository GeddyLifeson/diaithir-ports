package cn.leolezury.eternalstarlight.common.entity.living.npc.boarwarf;

import net.minecraft.world.item.trading.VillagerTrades;

public abstract class AbstractBoarwarfProfession {
	public abstract VillagerTrades.ItemListing[] getTrades(Boarwarf boarwarf);
}
