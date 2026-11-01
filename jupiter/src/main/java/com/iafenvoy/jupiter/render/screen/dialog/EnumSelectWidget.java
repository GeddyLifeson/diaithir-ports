package com.iafenvoy.jupiter.render.screen.dialog;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EnumSelectWidget<T extends Enum<T>> extends ObjectSelectionList<EnumSelectWidget.Entry<T>> {
    private final EnumSelectDialog<T> dialog;
    private final List<Entry<T>> entries = new ArrayList<>();

    public EnumSelectWidget(EnumSelectDialog<T> dialog, Minecraft client, int width, int height, int y) {
        super(client, width, height, y, 14);
        this.dialog = dialog;
    }

    public void update() {
        this.entries.clear();
        for (T x : this.dialog.getEntry().getDefaultValue().getDeclaringClass().getEnumConstants())
            this.entries.add(new Entry<>(this, x));
        this.updateEntries();
        this.setSelected(this.entries.get(this.dialog.getEntry().getValue().ordinal()));
    }

    private void updateEntries() {
        this.clearEntries();
        this.entries.forEach(this::addEntry);
        this.setScrollAmount(0);
    }

    @Override
    public void setSelected(@Nullable EnumSelectWidget.Entry<T> selected) {
        super.setSelected(selected);
        if (selected != null) this.dialog.getEntry().setValue(selected.value);
    }

    @Override
    protected int scrollBarX() {
        return this.getRight() - 8;
    }

    @Override
    public int getRowWidth() {
        return this.width - 4;
    }

    public static class Entry<T extends Enum<T>> extends ObjectSelectionList.Entry<Entry<T>> {
        private final Minecraft client = Minecraft.getInstance();
        private final EnumSelectWidget<T> widget;
        private final T value;

        public Entry(EnumSelectWidget<T> widget, T value) {
            this.widget = widget;
            this.value = value;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor extractor, int mouseX, int mouseY, boolean isHovering, float partialTick) {
            int x = this.getX(), y = this.getY();
            extractor.centeredText(this.client.font, this.value.name(), x + this.widget.width / 2, y + 1, 0xFFFFFFFF);
        }

        @Override
        public boolean mouseClicked(@NotNull MouseButtonEvent event, boolean doubleClicked) {
            this.widget.setSelected(this);
            return false;
        }

        @Override
        public @NotNull Component getNarration() {
            return Component.empty();
        }
    }
}
