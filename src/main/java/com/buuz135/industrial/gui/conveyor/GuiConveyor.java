/*
 * This file is part of Industrial Foregoing.
 *
 * Copyright 2021, Buuz135
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.buuz135.industrial.gui.conveyor;

import net.minecraft.client.input.MouseButtonEvent;

import net.minecraft.client.renderer.RenderPipelines;

import com.buuz135.industrial.IndustrialForegoing;
import com.buuz135.industrial.api.conveyor.ConveyorUpgrade;
import com.buuz135.industrial.api.conveyor.gui.IGuiComponent;
import com.buuz135.industrial.gui.component.custom.FilterGuiComponent;
import com.buuz135.industrial.gui.component.custom.ICanSendNetworkMessage;
import com.buuz135.industrial.proxy.block.filter.IFilter;
import com.buuz135.industrial.proxy.network.ConveyorButtonInteractMessage;
import com.buuz135.industrial.utils.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuiConveyor extends AbstractContainerScreen<ContainerConveyor> implements ICanSendNetworkMessage {

    public static final Identifier BG_TEXTURE = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/conveyor.png");

    private ConveyorUpgrade upgrade;
    private List<IGuiComponent> componentList;
    private int x;
    private int y;
    private List<IFilter.GhostSlot> ghostSlots;

    public GuiConveyor(ContainerConveyor inventorySlotsIn, Inventory inventory, Component component) {
        super(inventorySlotsIn, inventory, component);
        this.upgrade = getMenu().getConveyor().getUpgradeMap().get(getMenu().getFacing());
        this.componentList = new ArrayList<>();
        this.ghostSlots = new ArrayList<>();
    }

    @Override
    protected void init() {
        super.init();
        componentList.clear();
        upgrade.addComponentsToGui(componentList);
        for (IGuiComponent iGuiComponent : componentList) {
            if (iGuiComponent instanceof FilterGuiComponent) {
                ghostSlots.addAll(Arrays.asList(((FilterGuiComponent) iGuiComponent).getFilter().getFilter()));
            }
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks); //background
        x = (width - imageWidth) / 2;
        y = (height - imageHeight) / 2;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BG_TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);
        if (upgrade != null) {
            String localized = Component.translatable(String.format("conveyor.upgrade.%s.%s", BuiltInRegistries.ITEM.getKey(upgrade.getFactory().getUpgradeItem()).getNamespace(), BuiltInRegistries.ITEM.getKey(upgrade.getFactory().getUpgradeItem()).getPath())).getString();
            guiGraphics.text(Minecraft.getInstance().font, localized, x + imageWidth / 2 - getMinecraft().font.width(localized) / 2, y + 6, 0xFF404040, false);
        }
        for (IGuiComponent iGuiComponent : componentList) {
            iGuiComponent.drawGuiBackgroundLayer(guiGraphics, x, y, mouseX, mouseY);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) { //foreground
        x = (width - imageWidth) / 2;
        y = (height - imageHeight) / 2;
        for (IGuiComponent iGuiComponent : componentList) {
            iGuiComponent.drawGuiForegroundLayer(guiGraphics, x, y, mouseX, mouseY);
        }
        for (IGuiComponent iGuiComponent : componentList) {
            if (iGuiComponent.isInside(mouseX - x, mouseY - y)) {
                List<Component> tooltips = iGuiComponent.getTooltip(x, y, mouseX, mouseY);
                if (tooltips != null) guiGraphics.setComponentTooltipForNextFrame(Minecraft.getInstance().font, tooltips, mouseX - x, mouseY - y);
            }
        }
    }

    public ContainerConveyor getMenu() {
        return (ContainerConveyor) super.getMenu();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) { //mouseClicked
        boolean click = super.mouseClicked(event, doubleClick);
        for (IGuiComponent iGuiComponent : componentList) {
            if (iGuiComponent.isInside(event.x() - x, event.y() - y)) {
                if (iGuiComponent.handleClick(this, x, y, event.x(), event.y()))
                    return true;
            }
        }
        return click;
    }

    @Override
    public void sendMessage(int id, CompoundTag compound) {
        IndustrialForegoing.NETWORK.sendToServer(new ConveyorButtonInteractMessage(upgrade.getPos(), id, upgrade.getSide(), compound));
    }

    public List<IFilter.GhostSlot> getGhostSlots() {
        return ghostSlots;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
