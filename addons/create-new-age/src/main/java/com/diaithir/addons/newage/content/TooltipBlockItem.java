package com.diaithir.addons.newage.content;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

/** Block item that asks its block for tooltip lines. */
public class TooltipBlockItem extends BlockItem {
    public TooltipBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> out, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, out, flag);
        if (getBlock() instanceof TooltipBlock t) {
            List<Component> lines = new ArrayList<>();
            t.appendTooltip(lines);
            lines.forEach(out);
        }
    }
}
