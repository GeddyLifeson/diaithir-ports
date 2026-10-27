package cn.leolezury.eternalstarlight.common.item.misc;

import java.util.function.Consumer;

import net.minecraft.world.item.component.TooltipDisplay;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class BlossomOfStarsItem extends Item {
	public BlossomOfStarsItem(Properties properties) {
		super(properties);
	}

	@Override
	public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, TooltipDisplay tooltipDisplay, Consumer<Component> list, TooltipFlag tooltipFlag) {
		list.accept(Component.translatable("tooltip." + EternalStarlight.ID + ".blossom_of_stars").withColor(0x5187c4).withStyle(ChatFormatting.ITALIC));
		super.appendHoverText(itemStack, tooltipContext, tooltipDisplay, list, tooltipFlag);
	}
}
