package com.lowdragmc.lowdraglib.gui.compass;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CompassSelectorWidget extends WidgetGroup {
    private final List<CompassNode> nodes;

    public CompassSelectorWidget(List<CompassNode> nodes) {
        super(0, 0, 210, 100);
        setClientSideWidget();
        this.nodes = nodes;
    }

    @Override
    public void initWidget() {  /* ported stub */ }
}
