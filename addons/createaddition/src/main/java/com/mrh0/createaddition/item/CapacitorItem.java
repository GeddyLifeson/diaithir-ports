package com.mrh0.createaddition.item;

import java.util.function.Consumer;

import com.mrh0.createaddition.config.CommonConfig;
import com.mrh0.createaddition.index.CAItems;
import com.mrh0.createaddition.index.CADataComponents;
import com.mrh0.createaddition.util.Util;
import com.mrh0.createaddition.energy.fe.Fe;
import com.mrh0.createaddition.energy.fe.IEnergyStorage;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.energy.ItemAccessEnergyHandler;

public class CapacitorItem extends Item {
    public CapacitorItem(Properties props) {
        super(props.stacksTo(16));
    }

    public static int getEnergy(ItemStack stack) {
        return stack.getOrDefault(CADataComponents.ENERGY, 0);
    }

    public static void setEnergy(ItemStack stack, int energy) {
        if (energy <= 0) stack.remove(CADataComponents.ENERGY);
        else stack.set(CADataComponents.ENERGY, Math.min(energy, CommonConfig.CAPACITOR_CAPACITY.get()));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        IEnergyStorage es = Fe.item(stack);
        if (es != null) tooltip.accept(Util.getTextComponent(es));
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return getEnergy(stack) > 0 ? 1 : 16;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getEnergy(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round((float) getEnergy(stack) / CommonConfig.CAPACITOR_CAPACITY.get() * 13f);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x00BFFF;
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(Capabilities.Energy.ITEM,
            (stack, access) -> new ItemAccessEnergyHandler(access, CADataComponents.ENERGY, CommonConfig.CAPACITOR_CAPACITY.get(), CommonConfig.CAPACITOR_CHARGE_RATE.get()),
            CAItems.CAPACITOR.get());
    }
}
