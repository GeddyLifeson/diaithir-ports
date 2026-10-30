package com.github.alexmodguy.alexscaves.client.gui;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.server.inventory.NuclearFurnaceMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class NuclearFurnaceScreen extends AbstractContainerScreen<NuclearFurnaceMenu> {

    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/gui/nuclear_furnace.png");

    public NuclearFurnaceScreen(NuclearFurnaceMenu furnaceMenu, Inventory inventory, Component component) {
        super(furnaceMenu, inventory, component);
        this.inventoryLabelY = this.imageHeight - 92;
        this.titleLabelX = this.imageHeight / 2 + 5;
        this.titleLabelY = 5;
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        guiGraphics.text(this.font, this.title, this.titleLabelX - (this.font.width(this.title) / 2), this.titleLabelY, 0xFF404040, false);
        guiGraphics.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFF404040, false);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
        int i = this.leftPos;
        int j = this.topPos;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i, j, 0F, 0F, this.imageWidth, this.imageHeight, 256, 256);
        if (!menu.getSlot(1).hasItem()) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i + 67, j + 53, 176F, 84F, 16, 16, 256, 256);
        }
        if (!menu.getSlot(2).hasItem()) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i + 37, j + 53, 192F, 84F, 16, 16, 256, 256);
        }
        float wasteAmount = menu.getWasteScale();
        if (wasteAmount > 0.0F) {
            int wastePixels = (int) Math.ceil(52 * wasteAmount);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i + 13, j + 17 + (52 - wastePixels), 176F, (float) (32 + (52 - wastePixels)), 16, wastePixels, 256, 256);
        }
        float barrelAmount = menu.getBarrelScale();
        if (barrelAmount > 0.0F) {
            int barrelPixels = (int) Math.ceil(14 * barrelAmount);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i + 38, j + 36 + (14 - barrelPixels), 192F, (float) (14 - barrelPixels), 15, barrelPixels, 256, 256);
        }
        float fissionAmount = menu.getFissionScale();
        if (fissionAmount > 0.0F) {
            int fissionPixels = (int) Math.ceil(14 * fissionAmount);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i + 68, j + 36 + (14 - fissionPixels), 176F, (float) (14 - fissionPixels), 14, fissionPixels, 256, 256);
        }
        float cookAmount = menu.getCookScale();
        if (cookAmount > 0.0F) {
            int cookPixels = (int) Math.ceil(24 * cookAmount);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i + 90, j + 35, 176F, 14F, cookPixels, 17, 256, 256);
        }
    }
}
