package com.lowdragmc.lowdraglib.gui.editor.ui;

import com.lowdragmc.lowdraglib.gui.animation.Transform;
import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.editor.Icons;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.utils.Size;
import com.lowdragmc.lowdraglib.utils.interpolate.Eases;
import lombok.Getter;
import lombok.Setter;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author KilaBash
 * @date 2022/12/2
 * @implNote ResourcePanel
 */
public class ToolPanel extends WidgetGroup {
    @Deprecated
    public static final int WIDTH = 100;

    @Getter
    protected final Editor editor;
    @Getter
    protected final List<Widget> toolBoxes = new ArrayList<>();
    protected ButtonWidget buttonHide;
    @Getter
    protected TabContainer tabContainer;
    protected ImageWidget tabsBackground;
    @Setter
    protected String title = "ldlib.gui.editor.group.tool_box";

    @Getter
    protected boolean isShow;

    public ToolPanel(Editor editor) {
        super(-WIDTH, 30, WIDTH, Math.max(100, editor.getSize().getHeight() - ResourcePanel.HEIGHT - 30));
        setClientSideWidget();
        this.editor = editor;
    }

    @Override
    public void setSize(Size size) {
        var width = getSizeWidth();
        var newWidth = size.width;
        super.setSize(size);
        if (width != newWidth) {
            if (tabContainer != null) tabContainer.setSize(new Size(newWidth, size.height - 15));
            if (tabsBackground != null) tabsBackground.setSize(new Size(20, toolBoxes.size() * 20));
            if (buttonHide != null) buttonHide.setSelfPosition(newWidth - 13, 3);
            if (isShow) {
                setSelfPosition(0, getSelfPositionY());
            } else {
                setSelfPosition(-newWidth, getSelfPositionY());
            }
        }
    }

    @Override
    public void initWidget() {  /* ported stub */ }

    @Override
    public void clearAllWidgets() {
        toolBoxes.clear();
        tabContainer.clearAllWidgets();
        tabsBackground.setSize(new Size(20, 0));
    }

    @Deprecated
    public void addNewToolBox(String name, ResourceTexture texture, WidgetGroup toolBox) {
        addNewToolBox(name, texture, size -> {
            toolBox.setSize(size);
            return toolBox;
        });
    }

    public void addNewToolBox(String name, ResourceTexture texture, Function<Size, WidgetGroup> toolBoxSupplier) {  /* ported stub */ }

    public void hide() {
        hide(true);
    }

    public void hide(boolean animate) {  /* ported stub */ }

    public void show() {
        show(true);
    }

    public void show(boolean animate) {  /* ported stub */ }
}
