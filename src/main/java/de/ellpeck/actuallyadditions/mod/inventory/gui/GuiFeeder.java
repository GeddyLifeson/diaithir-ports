/*
 * This file ("GuiFeeder.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.inventory.gui;

import net.minecraft.client.renderer.RenderPipelines;

import com.mojang.blaze3d.systems.RenderSystem;
import de.ellpeck.actuallyadditions.mod.inventory.ContainerFeeder;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityFeeder;
import de.ellpeck.actuallyadditions.mod.util.AssetUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;
import java.util.List;


public class GuiFeeder extends AAScreen<ContainerFeeder> {

    private static final Identifier RES_LOC = AssetUtil.getGuiLocation("gui_feeder");
    public final TileEntityFeeder tileFeeder;

    public GuiFeeder(ContainerFeeder container, Inventory inventory, Component title) {
        super(container, inventory, title, 176, 70 + 86);
        this.tileFeeder = container.feeder;
    }

    @Override
    public void extractRenderState(@Nonnull GuiGraphicsExtractor guiGraphics, int x, int y, float f) {
        super.extractRenderState(guiGraphics, x, y, f);
        if (x >= this.leftPos + 69 && y >= this.topPos + 30 && x <= this.leftPos + 69 + 10 && y <= this.topPos + 30 + 10) {
            List<Component> array = List.of(
                    Component.literal(this.tileFeeder.currentAnimalAmount + " ").append(Component.translatable("info.actuallyadditions.gui.animals")),
                    this.tileFeeder.currentAnimalAmount >= 2 && this.tileFeeder.currentAnimalAmount < TileEntityFeeder.THRESHOLD
                ? Component.translatable("info.actuallyadditions.gui.enoughToBreed")
                : this.tileFeeder.currentAnimalAmount >= TileEntityFeeder.THRESHOLD
                    ? Component.translatable("info.actuallyadditions.gui.tooMany")
                    : Component.translatable("info.actuallyadditions.gui.notEnough")
            );
            guiGraphics.setComponentTooltipForNextFrame(this.font, array, x, y);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int x, int y, float f) {
        super.extractBackground(guiGraphics, x, y, f);

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, AssetUtil.GUI_INVENTORY_LOCATION, this.leftPos, this.topPos + 70, 0, 0, 176, 86, 256, 256);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, RES_LOC, this.leftPos, this.topPos, 0, 0, 176, 70, 256, 256);

        if (this.tileFeeder.currentTimer > 0) {
            int i = this.tileFeeder.getCurrentTimerToScale(20);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, RES_LOC, this.leftPos + 85, this.topPos + 42 - i, 181, 19 + 19 - i, 6, 20, 256, 256);
        }

        if (this.tileFeeder.currentAnimalAmount >= 2 && this.tileFeeder.currentAnimalAmount < TileEntityFeeder.THRESHOLD) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, RES_LOC, this.leftPos + 70, this.topPos + 31, 192, 16, 8, 8, 256, 256);
        }

        if (this.tileFeeder.currentAnimalAmount >= TileEntityFeeder.THRESHOLD) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, RES_LOC, this.leftPos + 70, this.topPos + 31, 192, 24, 8, 8, 256, 256);
        }
    }
}
