package cn.leolezury.eternalstarlight.common.entity.living.npc.boarwarf;

import net.minecraft.world.item.trading.VillagerTrades;

public abstract class AbstractBoarwarfProfession {
	public abstract cn.leolezury.eternalstarlight.common.entity.living.npc.trade.ESTradeListing[] getTrades(Boarwarf boarwarf);
}
