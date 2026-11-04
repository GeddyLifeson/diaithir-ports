package com.misterd.agritechevolved.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class ClocheItem extends Item {
    public ClocheItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> adder, TooltipFlag flag) {
        adder.accept(Component.translatable("item.agritechevolved.cloche.tooltip.line1"));
        adder.accept(Component.translatable("item.agritechevolved.cloche.tooltip.line2"));
        adder.accept(Component.translatable("item.agritechevolved.cloche.tooltip.line3"));
    }
}