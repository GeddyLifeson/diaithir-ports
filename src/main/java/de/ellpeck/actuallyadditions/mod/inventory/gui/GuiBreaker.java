/*
 * This file ("GuiBreaker.java") is part of the Actually Additions mod for Minecraft.
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
import de.ellpeck.actuallyadditions.mod.inventory.ContainerBreaker;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityBreaker;
import de.ellpeck.actuallyadditions.mod.util.AssetUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;


public class GuiBreaker extends AAScreen<ContainerBreaker> {

    private static final Identifier RES_LOC = AssetUtil.getGuiLocation("gui_breaker");
    private final TileEntityBreaker breaker;

    public GuiBreaker(ContainerBreaker container, Inventory inventory, Component title) {
        super(container, inventory, title, 176, 93 + 86);
        this.breaker = container.breaker;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int x, int y, float partialTicks) {
        super.extractBackground(guiGraphics, x, y, partialTicks);

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, AssetUtil.GUI_INVENTORY_LOCATION, this.leftPos, this.topPos + 93, 0, 0, 176, 86, 256, 256);

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, RES_LOC, this.leftPos, this.topPos, 0, 0, 176, 93, 256, 256);
    }
}
