package com.gregtechceu.gtceu.api.item.component;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.client.IItemDecorator;

public interface IItemDecoratorComponent extends IItemComponent, IItemDecorator {

    @Override
    default boolean render(GuiGraphicsExtractor graphics, Font font, ItemStack stack, int x, int y) {
        return false;
    }
}
