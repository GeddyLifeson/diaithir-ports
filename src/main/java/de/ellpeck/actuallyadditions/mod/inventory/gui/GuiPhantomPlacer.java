/*
 * This file ("GuiPhantomPlacer.java") is part of the Actually Additions mod for Minecraft.
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
import de.ellpeck.actuallyadditions.mod.inventory.ContainerPhantomPlacer;
import de.ellpeck.actuallyadditions.mod.network.PacketHelperClient;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityPhantomPlacer;
import de.ellpeck.actuallyadditions.mod.util.AssetUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;


public class GuiPhantomPlacer extends AAScreen<ContainerPhantomPlacer> {

    private static final Identifier RES_LOC = AssetUtil.getGuiLocation("gui_breaker");
    private final TileEntityPhantomPlacer placer;
    private Button buttonSide;

    public GuiPhantomPlacer(ContainerPhantomPlacer container, Inventory inventory, Component title) {
        super(container, inventory, title, 176, 93 + 86);
        this.placer = container.placer;
    }

    @Override
    public void init() {
        super.init();

        if (!this.placer.isBreaker) {
            buttonSide = Button.builder(Component.literal(this.getSide()), (button) -> PacketHelperClient.sendButtonPacket(this.placer, 0))
                    .bounds(this.leftPos + 63, this.topPos + 75, 50, 20).build();
            this.addRenderableWidget(buttonSide);
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        if (!this.placer.isBreaker && this.buttonSide != null) {
            buttonSide.setMessage(Component.literal(this.getSide()));
        }
    }

    @Override
    public void extractRenderState(@Nonnull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);

        if (!this.placer.isBreaker && buttonSide.isMouseOver(mouseX, mouseY)) {
            String loc = "info.actuallyadditions.placer.sides";

            List<Component> textList = new ArrayList<>();
            textList.add(Component.translatable(loc + ".1").withStyle(ChatFormatting.GOLD));
            textList.add(Component.translatable(loc + ".2"));
            guiGraphics.setComponentTooltipForNextFrame(font, textList, mouseX, mouseY);
        }
    }


    private String getSide() {
        return SIDES[this.placer.side + 1];
    }
    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int x, int y, float f) {
        super.extractBackground(guiGraphics, x, y, f);

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, AssetUtil.GUI_INVENTORY_LOCATION, this.leftPos, this.topPos + 93, 0, 0, 176, 86, 256, 256);

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, RES_LOC, this.leftPos, this.topPos, 0, 0, 176, 93, 256, 256);
    }
}
