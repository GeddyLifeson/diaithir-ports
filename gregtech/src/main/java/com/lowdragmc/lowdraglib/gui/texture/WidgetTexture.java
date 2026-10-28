package com.lowdragmc.lowdraglib.gui.texture;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.mojang.blaze3d.vertex.PoseStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import com.lowdragmc.lowdraglib.gui.compat.GuiGraphics;

/**
 * @author KilaBash
 * @date 2022/12/3
 * @implNote WidgetDraggingTexture
 */
public class WidgetTexture extends TransformTexture{
    private final Widget widget;
    private int centerX;
    private int centerY;
    private boolean isDragging;
    private boolean fixedCenter;
    private int mouseX, mouseY;

    public WidgetTexture(Widget widget) {
        this.widget = widget;
        this.centerX = widget.getPosition().x + widget.getSize().width / 2;
        this.centerY = widget.getPosition().y + widget.getSize().height / 2;
        this.mouseX = centerX;
        this.mouseY = centerY;
    }

    public WidgetTexture(int mouseX, int mouseY, Widget widget) {
        this.widget = widget;
        this.centerX = mouseX;
        this.centerY = mouseY;
        this.isDragging = true;
        this.fixedCenter = true;
        this.mouseX = widget.getPosition().x + widget.getSize().width / 2;
        this.mouseY = widget.getPosition().y + widget.getSize().height / 2;
    }

    public WidgetTexture setDragging(boolean dragging) {
        isDragging = dragging;
        return this;
    }

    public WidgetTexture setMouse(int mouseX, int mouseY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        return this;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void drawInternal(GuiGraphics graphics, int mouseX, int mouseY, float x, float y, int width, int height) {  /* ported stub */ }

}
