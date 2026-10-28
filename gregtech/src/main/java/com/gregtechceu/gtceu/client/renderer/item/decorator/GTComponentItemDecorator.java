package com.gregtechceu.gtceu.client.renderer.item.decorator;

import com.gregtechceu.gtceu.api.item.IComponentItem;

import net.minecraft.client.gui.Font;
import com.lowdragmc.lowdraglib.gui.compat.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IItemDecorator;

import org.jetbrains.annotations.NotNull;

public final class GTComponentItemDecorator implements IItemDecorator {

    public static final GTComponentItemDecorator INSTANCE = new GTComponentItemDecorator();

    private GTComponentItemDecorator() {}

    
    public boolean render(@NotNull net.minecraft.client.gui.GuiGraphicsExtractor guiGraphics, @NotNull Font font,
                          ItemStack stack, int xOffset, int yOffset) { return false; /* ported stub */ }
}
