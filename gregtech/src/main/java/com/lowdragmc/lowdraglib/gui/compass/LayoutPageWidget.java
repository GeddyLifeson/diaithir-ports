package com.lowdragmc.lowdraglib.gui.compass;

import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.utils.Position;
import lombok.Getter;

/**
 * @author KilaBash
 * @date 2022/9/3
 * @implNote LayoutWidget
 */
public class LayoutPageWidget extends DraggableScrollableWidgetGroup {

    @Getter
    protected int pageWidth;
    protected int offset = 0;

    public LayoutPageWidget(int width, int height) {
        super(4, 4, width - 8, height - 4);
        setClientSideWidget();
        this.pageWidth = width - 8;
    }

    public LayoutPageWidget addOffsetSpace(int offset) {
        return addStreamWidget(new Widget(0, 0, pageWidth, offset));
    }

    public LayoutPageWidget addStreamWidget(Widget widget) { return null; /* ported stub */ }

}
