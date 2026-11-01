package com.iafenvoy.jupiter.render.screen.dialog;

import com.iafenvoy.jupiter.config.interfaces.ConfigEntry;
import com.iafenvoy.jupiter.config.interfaces.ConfigMetaProvider;
import com.iafenvoy.jupiter.render.TitleStack;
import com.iafenvoy.jupiter.render.screen.JupiterScreen;
import com.iafenvoy.jupiter.render.screen.WidgetBuilderManager;
import com.iafenvoy.jupiter.render.screen.scrollbar.VerticalScrollBar;
import com.iafenvoy.jupiter.render.widget.WidgetBuilder;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractListDialog<T, S> extends Dialog<T> {
    protected final VerticalScrollBar itemScrollBar = new VerticalScrollBar();
    protected final List<WidgetBuilder<S>> widgets = new ArrayList<>();
    private int configPerPage;

    protected AbstractListDialog(Screen parent, TitleStack titleStack, ConfigMetaProvider provider, ConfigEntry<T> entry) {
        super(parent, titleStack, provider, entry);
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(JupiterScreen.createButton(10, 5, 20, ENTRY_HEIGHT, Component.literal("<"), button -> this.onClose()));
        this.addRenderableWidget(JupiterScreen.createButton(this.width - 80, 5, 20, ENTRY_HEIGHT, Component.literal("+"), button -> {
            this.addNewValue();
            this.rebuildWidgets();
        }));
        this.calculateMaxItems();
        this.widgets.clear();
        List<S> values = this.getValues().stream().toList();
        for (int i = 0; i < values.size(); i++) {
            WidgetBuilder<S> widget = WidgetBuilderManager.get(this.provider, this.newSingleInstance(values.get(i), i, this::rebuildWidgets));
            this.widgets.add(widget);
            widget.addDialogElements(new WidgetBuilder.Context(this, this::addRenderableWidget, this.titleStack), i + ":", 40, 0, Math.max(10, this.width - 70), ENTRY_HEIGHT);
        }
        this.updateItemPos();
    }

    protected abstract void addNewValue();

    protected abstract Collection<S> getValues();

    protected abstract ConfigEntry<S> newSingleInstance(S value, int index, Runnable reload);

    public void calculateMaxItems() {
        this.configPerPage = Math.max(0, (this.height - 30) / (ENTRY_HEIGHT + ENTRY_SEPARATOR));
        this.itemScrollBar.setMaxValue(Math.max(0, this.getValues().size() - this.configPerPage));
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        this.calculateMaxItems();
        this.updateItemPos();
    }

    public void updateItemPos() {
        int top = this.itemScrollBar.getValue();
        Collection<S> entries = this.getValues();
        for (int i = 0; i < top && i < entries.size(); i++)
            this.widgets.get(i).update(false, 0);
        for (int i = top; i < top + this.configPerPage && i < entries.size(); i++)
            this.widgets.get(i).update(true, 25 + ENTRY_SEPARATOR + (i - top) * (ENTRY_HEIGHT + ENTRY_SEPARATOR));
        for (int i = top + this.configPerPage; i < entries.size(); i++)
            this.widgets.get(i).update(false, 0);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;
        this.itemScrollBar.setValue(this.itemScrollBar.getValue() + (scrollY > 0 ? -1 : 1) * ENTRIES_PER_SCROLL);
        this.updateItemPos();
        return true;
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(extractor, mouseX, mouseY, partialTicks);
        this.itemScrollBar.render(extractor, mouseX, mouseY, partialTicks, this.width - 18, 30, 8, this.height - 50, (this.configPerPage + this.itemScrollBar.getMaxValue()) * (ENTRY_HEIGHT + ENTRY_SEPARATOR));
        if (this.itemScrollBar.isDragging()) this.updateItemPos();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        int button = event.button();
        if (button == 0 && this.itemScrollBar.wasMouseOver()) {
            this.itemScrollBar.setIsDragging(true);
            this.updateItemPos();
            return true;
        }
        boolean b = super.mouseClicked(event, isDoubleClick);
        if (!b) this.setFocused(null);
        return b;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        int button = event.button();
        if (button == 0) this.itemScrollBar.setIsDragging(false);
        return super.mouseReleased(event);
    }
}
