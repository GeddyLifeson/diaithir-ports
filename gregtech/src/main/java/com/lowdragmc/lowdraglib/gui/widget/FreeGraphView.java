package com.lowdragmc.lowdraglib.gui.widget;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.utils.Rect;
import lombok.Getter;
import lombok.Setter;
import com.lowdragmc.lowdraglib.gui.compat.GuiGraphics;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector4f;

/**
 * FreeGraphView allows user to freely explore its view with any scale and offset.
 */
public class FreeGraphView extends WidgetGroup {
    @Getter
    @Setter
    protected float xOffset, yOffset;
    @Getter
    @Setter
    protected float scale = 1;
    @Getter
    @Setter
    protected boolean drawGrid;
    @Getter
    @Setter
    protected int gridWidth = 50;
    @Getter
    @Setter
    protected boolean useScissor = true;

    // runtime
    protected double lastMouseX, lastMouseY;
    protected boolean isDragging = false;

    public FreeGraphView(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public void resetFitScaleByWidgets() {  /* ported stub */ }

    public void resetFitScaleWithArea(Rect area) {  /* ported stub */ }

    public Vector2d getViewPosition(double x, double y) {
        var realX = ((x - this.getPositionX()) / scale + xOffset);
        var realY = ((y - this.getPositionY()) / scale + yOffset);
        return new Vector2d(realX, realY);
    }

    public Vector2i getViewPosition(int x, int y) {
        var realX = ((x - this.getPositionX()) / scale + xOffset);
        var realY = ((y - this.getPositionY()) / scale + yOffset);
        return new Vector2i((int) realX, (int) realY);
    }

    public Vector2f getViewPosition(float x, float y) {
        var realX = ((x - this.getPositionX()) / scale + xOffset);
        var realY = ((y - this.getPositionY()) / scale + yOffset);
        return new Vector2f(realX, realY);
    }

    /********** Correct Event Position and Rendering for child widgets. **********/
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        var realMouse = getViewPosition(mouseX, mouseY);
        if (super.mouseClicked(realMouse.x, realMouse.y, button)) {
            return true;
        }
        if (isMouseOverElement(mouseX, mouseY)) {
            if (button == 0) {
                isDragging = true;
            }
        }
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        isDragging = false;
        var realMouse = getViewPosition(mouseX, mouseY);
        return super.mouseReleased(realMouse.x, realMouse.y, button);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        // update view offset
        if (isDragging) {
            xOffset += (float) (lastMouseX - mouseX) / scale;
            yOffset += (float) (lastMouseY - mouseY) / scale;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
        }
        var realMouse = getViewPosition(mouseX, mouseY);
        dragX = dragX / scale;
        dragY = dragY / scale;
        return super.mouseDragged(realMouse.x, realMouse.y, button, dragX, dragY) || isDragging;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelX, double wheelY) {
        // update view scale
        var realMouse = getViewPosition(mouseX, mouseY);
        if (super.mouseWheelMove(realMouse.x, realMouse.y, wheelX, wheelY)) {
            return true;
        }
        if (isMouseOverElement(mouseX, mouseY)) {
            var newScale = (float) Mth.clamp(scale + wheelY * 0.1f, 0.1f, 10f);
            if (newScale != scale) {
                xOffset += (float) (mouseX - this.getPositionX()) / scale - (float) (mouseX - this.getPositionX()) / newScale;
                yOffset += (float) (mouseY - this.getPositionY()) / scale - (float) (mouseY - this.getPositionY()) / newScale;
                scale = newScale;
            }
            return true;
        }
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseMoved(double mouseX, double mouseY) {
        var realMouse = getViewPosition(mouseX, mouseY);
        return super.mouseMoved(realMouse.x, realMouse.y);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInForeground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        drawTooltipTexts(mouseX, mouseY);
        var realMouse = getViewPosition(mouseX, mouseY);

        graphics.pose().pushPose();
        graphics.pose().translate(this.getPositionX(), this.getPositionY(), 0);
        graphics.pose().scale(scale, scale, 1);
        graphics.pose().translate(-xOffset, -yOffset, 0);
        drawWidgetsForeground(graphics, realMouse.x, realMouse.y, partialTicks);
        graphics.pose().popPose();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {  /* ported stub */ }

}
