package cn.leolezury.eternalstarlight.common.entity.living.npc.trade;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.Nullable;

/** PORT: cn.leolezury.eternalstarlight.common.entity.living.npc.trade.ESTradeListing was removed in 26.1 (trades are data driven), so mod merchants keep this tiny equivalent. */
@FunctionalInterface
public interface ESTradeListing {
	@Nullable
	MerchantOffer getOffer(Entity trader, RandomSource random);
}
