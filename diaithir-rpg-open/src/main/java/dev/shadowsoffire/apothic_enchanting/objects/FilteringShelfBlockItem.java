package dev.shadowsoffire.apothic_enchanting.objects;

import java.util.function.Consumer;

import dev.shadowsoffire.apothic_enchanting.util.TooltipUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

/**
 * BlockItem for the filtering shelf that surfaces the "info.apothic_enchanting.filtering_shelf"
 * description line in its tooltip.
 */
public class FilteringShelfBlockItem extends BlockItem {

    public FilteringShelfBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(TooltipUtil.lang("info", "filtering_shelf").withStyle(ChatFormatting.GRAY));
    }
}
