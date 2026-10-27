package com.github.alexmodguy.alexscaves.server.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class AlexMealItem extends ACBowlFoodItem {
    public AlexMealItem() {
        super(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().food(ACFoods.ALEX_MEAL.food(), ACFoods.ALEX_MEAL.consumable()).rarity(ACItemRegistry.getRarityRainbow()).stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(Component.translatable("item.alexscaves.alex_meal.desc").withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));
        super.appendHoverText(stack, context, display, tooltip, flagIn);
    }
}
