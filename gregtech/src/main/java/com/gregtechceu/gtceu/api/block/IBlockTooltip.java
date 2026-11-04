package com.gregtechceu.gtceu.api.block;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 26.1 removed {@code Block#appendHoverText}; blocks that want an item tooltip implement this instead and
 * {@code CommonEventListener#onTooltipEvent} forwards to it.
 */
public interface IBlockTooltip {

    void appendHoverText(ItemStack stack, @Nullable Item.TooltipContext context, List<Component> tooltip,
                         TooltipFlag flag);
}
