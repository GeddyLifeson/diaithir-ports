/*
 * This file ("TexturedButton.java") is part of the Actually Additions mod for Minecraft.
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class TexturedButton extends Button {

    public final List<String> textList = new ArrayList<>();
    private final Identifier resLoc;
    public int texturePosX;
    public int texturePosY;

    public TexturedButton(Identifier resLoc, int x, int y, int texturePosX, int texturePosY, int width, int height, OnPress pressable) {
        this(resLoc, x, y, texturePosX, texturePosY, width, height, new ArrayList<>(), pressable);
    }

    public TexturedButton(Identifier resLoc, int x, int y, int texturePosX, int texturePosY, int width, int height, List<String> hoverTextList, OnPress pressable) {
        super(x, y, width, height, Component.empty(), pressable, DEFAULT_NARRATION);
        this.texturePosX = texturePosX;
        this.texturePosY = texturePosY;
        this.resLoc = resLoc;
        this.textList.addAll(hoverTextList);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        int k = this.isHovered
                ? 1
                : 0;

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, this.resLoc, this.getX(), this.getY(), this.texturePosX, this.texturePosY - this.height + k * this.height, this.width, this.height, 256, 256);
        //            this.mouseDragged(minecraft, x, y);
    }

    public void drawHover(GuiGraphicsExtractor guiGraphics, int x, int y) {
        if (this.isMouseOver(x, y)) {
            Minecraft mc = Minecraft.getInstance();
            guiGraphics.setComponentTooltipForNextFrame(mc.font, this.textList.stream().map(Component::literal).collect(Collectors.toList()), x, y);
        }
    }
}
