/*
 * This file ("GuiGrinder.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.inventory.gui;

import net.minecraft.client.renderer.RenderPipelines;

import de.ellpeck.actuallyadditions.mod.inventory.CrusherContainer;
import de.ellpeck.actuallyadditions.mod.network.PacketHelperClient;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityCrusher;
import de.ellpeck.actuallyadditions.mod.util.AssetUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;


public class CrusherScreen extends AAScreen<CrusherContainer> {

    private static final Identifier RES_LOC = AssetUtil.getGuiLocation("gui_grinder");
    private static final Identifier RES_LOC_DOUBLE = AssetUtil.getGuiLocation("gui_grinder_double");
    private final TileEntityCrusher tileGrinder;
    private final boolean isDouble;
    private EnergyDisplay energy;

    private Button buttonAutoSplit;

    public CrusherScreen(CrusherContainer container, Inventory inventory, Component title) {
        super(container, inventory, title, 176, 93 + 86);
        this.tileGrinder = container.tileGrinder;
        this.isDouble = container.isDouble;
    }

    @Override
    public void init() {
        super.init();
        this.energy = new EnergyDisplay(this.leftPos + (this.isDouble
            ? 13
            : 42), this.topPos + 5, this.tileGrinder.storage);

        if (this.isDouble) {
            this.buttonAutoSplit = Button.builder(Component.literal("S"), (button) -> actionPerformed(0))
                    .bounds(this.leftPos - 10, this.topPos, 16, 16)
                    .build();
            this.addRenderableWidget(this.buttonAutoSplit);
        }

        titleLabelX = (int) (imageWidth / 2.0f - font.width(title) / 2.0f);
        titleLabelY = -10;
    }

    protected void actionPerformed(int id) {
        if (this.isDouble && id == 0) {
            PacketHelperClient.sendButtonPacket(this.tileGrinder, id);
        }
    }

    @Override
    public void containerTick() {
        super.containerTick();

        if (this.isDouble) {
            this.buttonAutoSplit.setMessage(Component.literal("S").withStyle(this.tileGrinder.isAutoSplit
                ? ChatFormatting.DARK_GREEN
                : ChatFormatting.RED));
        }
    }

    @Override
    public void extractRenderState(@Nonnull GuiGraphicsExtractor guiGraphics, int x, int y, float f) {
        super.extractRenderState(guiGraphics, x, y, f);
        this.energy.render(guiGraphics, x, y);

        if (this.isDouble && this.buttonAutoSplit.isMouseOver(x,y)) {
            guiGraphics.setTooltipForNextFrame(font, Component.translatable("info.actuallyadditions.gui.autosplititems." + (tileGrinder.isAutoSplit?"on":"off")), x, y);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int x, int y, float f) {
        super.extractBackground(guiGraphics, x, y, f);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, AssetUtil.GUI_INVENTORY_LOCATION, this.leftPos, this.topPos + 93, 0, 0, 176, 86, 256, 256);

        Identifier usedLoc = this.isDouble ? RES_LOC_DOUBLE : RES_LOC;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, usedLoc, this.leftPos, this.topPos, 0, 0, 176, 93, 256, 256);

        if (this.tileGrinder.firstCrushTime > 0) {
            int i = this.tileGrinder.getFirstTimeToScale(23);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, usedLoc, this.leftPos + (this.isDouble
                ? 51
                : 80), this.topPos + 40, 176, 0, 24, i, 256, 256);
        }
        if (this.isDouble) {
            if (this.tileGrinder.secondCrushTime > 0) {
                int i = this.tileGrinder.getSecondTimeToScale(23);
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, usedLoc, this.leftPos + 101, this.topPos + 40, 176, 22, 24, i, 256, 256);
            }
        }

        this.energy.draw(guiGraphics);
    }

    public static class CrusherDoubleScreen extends CrusherScreen {

        public CrusherDoubleScreen(CrusherContainer crusherContainer, Inventory inventory, Component tile) {
            super(crusherContainer, inventory, tile);
        }
    }
}
