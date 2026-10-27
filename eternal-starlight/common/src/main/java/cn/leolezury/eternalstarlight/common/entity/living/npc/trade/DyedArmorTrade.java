package cn.leolezury.eternalstarlight.common.entity.living.npc.trade;

import com.google.common.collect.Lists;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.trading.VillagerTrades;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.List;

public class DyedArmorTrade implements cn.leolezury.eternalstarlight.common.entity.living.npc.trade.ESTradeListing {
	private final Item item;
	private final ItemStack currency;
	private final int maxUses;

	public DyedArmorTrade(Item item, Item currency, int currencyCount) {
		this(item, new ItemStack(currency, currencyCount), 12);
	}

	public DyedArmorTrade(Item item, ItemStack currency, int maxUses) {
		this.item = item;
		this.currency = currency;
		this.maxUses = maxUses;
	}

	@Override
	public MerchantOffer getOffer(Entity entity, RandomSource randomSource) {
		ItemStack stack = new ItemStack(this.item);
		if (stack.has(net.minecraft.core.component.DataComponents.DYED_COLOR)) {
			List<DyeColor> list = Lists.newArrayList();
			list.add(getRandomDye(randomSource));
			if (randomSource.nextFloat() > 0.7F) {
				list.add(getRandomDye(randomSource));
			}

			if (randomSource.nextFloat() > 0.8F) {
				list.add(getRandomDye(randomSource));
			}

			stack = DyedItemColor.applyDyes(stack, list);
		}

		return new MerchantOffer(new ItemCost(currency.getItem(), currency.getCount()), stack, this.maxUses, 0, 0);
	}

	private static DyeColor getRandomDye(RandomSource randomSource) {
		return DyeColor.byId(randomSource.nextInt(16));
	}
}
