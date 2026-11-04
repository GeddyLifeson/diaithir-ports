package com.lowdragmc.lowdraglib.gui.editor.ui;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.editor.Icons;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.TabButton;
import com.lowdragmc.lowdraglib.gui.widget.TabContainer;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import lombok.Getter;
import com.lowdragmc.lowdraglib.gui.compat.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;

@Getter
public class StringTabContainer extends TabContainer {
    public final static int TAB_HEIGHT = 16;
    protected Editor editor;

    protected final Map<WidgetGroup, Runnable> onSelected;
    protected final Map<WidgetGroup, Runnable> onDeselected;
    public BiConsumer<WidgetGroup, WidgetGroup> onChanged;
    private final List<WidgetGroup> tabGroups;


    public StringTabContainer(Editor editor) {
        super(0, 0, editor.getSize().width - editor.getConfigPanel().getSize().width, editor.getSize().height);
        this.editor = editor;
        this.onSelected = new HashMap<>();
        this.onDeselected = new HashMap<>();
        this.tabGroups = new ArrayList<>();
        super.setOnChanged(this::onTabChanged);
    }

    public int getTabIndex() {
        if (focus == null) return -1;
        return tabGroups.indexOf(focus);
    }

    public int getTabIndex(WidgetGroup group) {
        return tabGroups.indexOf(group);
    }

    public void switchTabIndex(int index) {
        if (tabGroups.size() > index && index >= 0) {
            switchTag(tabGroups.get(index));
        }
    }

    @Override
    public void clearAllWidgets() {
        super.clearAllWidgets();
        this.onSelected.clear();
        this.onDeselected.clear();
        this.tabGroups.clear();
    }

    public TabContainer setOnChanged(BiConsumer<WidgetGroup, WidgetGroup> onChanged) {
        this.onChanged = onChanged;
        return this;
    }

    protected void onTabChanged(WidgetGroup oldGroup, WidgetGroup newGroup) {
        Optional.ofNullable(onDeselected.get(oldGroup)).ifPresent(Runnable::run);
        Optional.ofNullable(onSelected.get(newGroup)).ifPresent(Runnable::run);
        if (onChanged != null) {
            onChanged.accept(oldGroup, newGroup);
        }
    }

    @Override
    public final void addTab(TabButton tabButton, WidgetGroup tabWidget) {
        super.addTab(tabButton, tabWidget);
        tabGroups.add(tabWidget);
    }

    @Override
    public void removeTab(TabButton tabButton) {
        var group = tabs.get(tabButton);
        var lastIndex = getTabIndex(group);
        super.removeTab(tabButton);
        tabGroups.remove(group);
        if (focus == group) {
            Optional.ofNullable(onDeselected.get(group)).ifPresent(Runnable::run);
            // find a new tabs
            focus = null;
            if (lastIndex > 0) {
                switchTabIndex(lastIndex - 1);
            }
        }
        onSelected.remove(group);
        onDeselected.remove(group);
        calculateTabSize();
    }

    public void addTab(@Nullable IGuiTexture icon, String name, WidgetGroup group, @Nullable Runnable onSelected, @Nullable Runnable onDeselected, @Nullable Runnable onRemoved) {  /* ported stub */ }

    public void addTab(String name, WidgetGroup group, @Nullable Runnable onSelected, @Nullable Runnable onDeselected) {
        this.addTab(null, name, group, onSelected, onDeselected, null);
    }

    public void addTab(String name, WidgetGroup group, Runnable onSelected) {
        this.addTab(name, group, onSelected, null);
    }

    public void addTab(String name, WidgetGroup group) {
        this.addTab(name, group, null, null);
    }

    public void calculateTabSize() {  /* ported stub */ }
}
