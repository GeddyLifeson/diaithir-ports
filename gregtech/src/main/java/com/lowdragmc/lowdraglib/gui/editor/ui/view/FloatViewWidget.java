package com.lowdragmc.lowdraglib.gui.editor.ui.view;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.editor.ILDLRegister;
import com.lowdragmc.lowdraglib.gui.editor.ui.Editor;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Size;
import lombok.Getter;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * @author KilaBash
 * @date 2022/12/17
 * @implNote FloatViewWidget, view are some float window widgets.
 * They are technically project-independent and can be used for any project.
 */
@Getter
public class FloatViewWidget extends WidgetGroup implements ILDLRegister {
    protected final Editor editor;
    protected final boolean isFixedView;
    protected WidgetGroup title, content;
    protected boolean isDragging, isCollapse;
    protected double lastDeltaX, lastDeltaY;

    public FloatViewWidget(int x, int y, int width, int height, boolean isFixedView) {
        super(x, y, width, height + 15);
        this.editor = Editor.INSTANCE;
        if (this.editor == null) {
            throw new RuntimeException("editor is null while creating a float view %s".formatted(name()));
        }
        this.isFixedView = isFixedView;
        setClientSideWidget();
    }

    @Override
    public void initWidget() {  /* ported stub */ }

    protected void collapse(ClickData clickData) {  /* ported stub */ }

    public IGuiTexture getIcon() {
        return IGuiTexture.EMPTY;
    }

    public IGuiTexture getHoverIcon() {
        return getIcon().setColor(ColorPattern.GREEN.color);
    }

    public String getTitle() {
        return getTranslateKey();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        isDragging = false;
        if (title != null && title.isMouseOverElement(mouseX, mouseY)) {
            lastDeltaX = 0;
            lastDeltaY = 0;
            isDragging = true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDragging) {
            double dx = dragX + lastDeltaX;
            double dy = dragY + lastDeltaY;
            dragX = (int) dx;
            dragY = (int) dy;
            lastDeltaX = dx- dragX;
            lastDeltaY = dy - dragY;
            addSelfPosition((int) dragX, (int) dragY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        isDragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }
}
