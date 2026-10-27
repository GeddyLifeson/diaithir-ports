package com.github.alexmodguy.alexscaves.server.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class RadiantEssenceItem extends Item {

    public RadiantEssenceItem() {
        super(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().rarity(ACItemRegistry.getRarityRainbow()));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> tooltip, TooltipFlag flagIn) {
        if(this == ACItemRegistry.LICOWITCH_RADIANT_ESSENCE.get()){
            tooltip.add(Component.translatable("item.alexscaves.licowitch_radiant_essence.desc").withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));
        }
        super.appendHoverText(stack, context, display, tooltip, flagIn);
    }
}
