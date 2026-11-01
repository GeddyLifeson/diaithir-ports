package com.lowdragmc.lowdraglib.gui.editor.ui.view;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.editor.Icons;
import com.lowdragmc.lowdraglib.gui.editor.annotation.LDLRegister;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import lombok.Getter;

/**
 * @author KilaBash
 * @date 2022/12/17
 * @implNote HistoryView
 */
@LDLRegister(name = "history", group = "editor")
@Getter
public class HistoryView extends FloatViewWidget {
    private WidgetGroup container;

    public HistoryView() {
        super(100, 100, 180, 120, false);
    }

    @Override
    public IGuiTexture getIcon() {
        return Icons.HISTORY.copy();
    }

    @Override
    public void initWidget() {
        super.initWidget();
        content.addWidget(new DraggableScrollableWidgetGroup(0, 0, content.getSizeWidth(), content.getSizeHeight())
                .setYScrollBarWidth(2).setYBarStyle(null, ColorPattern.T_WHITE.rectTexture().setRadius(1).transform(-0.5f, 0))
                .addWidget(container = new WidgetGroup(0, 0, content.getSizeWidth() - 2, 0)));
        loadList();
    }

    public void loadList() {  /* ported stub */ }
}
