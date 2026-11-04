package com.iafenvoy.jupiter.render.internal;

import com.iafenvoy.jupiter.ConfigManager;
import com.iafenvoy.jupiter.config.ConfigSide;
import com.iafenvoy.jupiter.config.ConfigSource;
import com.iafenvoy.jupiter.config.container.AbstractConfigContainer;
import com.iafenvoy.jupiter.render.BadgeRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@ApiStatus.Internal
public class JupiterConfigListWidget extends ObjectSelectionList<JupiterConfigListWidget.Entry> {
    private final JupiterConfigListScreen screen;
    private final List<Entry> entries = new ArrayList<>();
    private String filter = "";

    public JupiterConfigListWidget(JupiterConfigListScreen screen, Minecraft client, int width, int height, int y) {
        super(client, width, height, y, 32);
        this.screen = screen;
    }

    public void update() {
        this.entries.clear();
        for (AbstractConfigContainer x : ConfigManager.getInstance().getConfigs()) this.entries.add(new Entry(this, x));
        this.updateEntries();
    }

    private void updateEntries() {
        this.clearEntries();
        this.entries.stream().filter(x -> x.match(this.filter)).forEach(this::addEntry);
        this.setScrollAmount(0);
    }

    public void setFilter(String filter) {
        this.filter = filter.toLowerCase(Locale.ROOT);
        this.updateEntries();
    }

    @Override
    protected int scrollBarX() {
        return this.getRight() - 8;
    }

    @Override
    public int getRowWidth() {
        return this.width - 4;
    }

    @Override
    public void setSelected(@Nullable JupiterConfigListWidget.Entry selected) {
        super.setSelected(selected);
        this.screen.updateButtonState();
    }

    public static class Entry extends ObjectSelectionList.Entry<Entry> {
        private final Minecraft client = Minecraft.getInstance();
        private final JupiterConfigListWidget widget;
        private final AbstractConfigContainer handler;

        public Entry(JupiterConfigListWidget widget, AbstractConfigContainer handler) {
            this.widget = widget;
            this.handler = handler;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor extractor, int mouseX, int mouseY, boolean isHovering, float partialTick) {
            int x = this.getX(), y = this.getY();
            extractor.text(this.client.font, this.handler.getTitle(), x + 65, y + 1, 0xFFFFFFFF);
            extractor.text(this.client.font, this.handler.getConfigId().toString(), x + 65, y + 1 + 9, 0xFF7F7F7F);
            extractor.text(this.client.font, this.handler.getPath(), x + 65, y + 1 + 18, 0xFF7F7F7F);
            //Badges
            ConfigSource source = this.handler.getSource();
            ConfigSide side = this.handler.getSide();
            BadgeRenderer.draw(extractor, this.client.font, x + 1, y + 1, source.name(), source.color());
            BadgeRenderer.draw(extractor, this.client.font, x + 1, y + 16, Component.literal(side.getDisplayText()), side.getColor());
        }

        @Override
        public boolean mouseClicked(@NotNull MouseButtonEvent event, boolean doubleClicked) {
            this.widget.setSelected(this);
            return false;
        }

        public AbstractConfigContainer getConfigContainer() {
            return this.handler;
        }

        @Override
        public @NotNull Component getNarration() {
            return Component.empty();
        }

        public boolean match(String filter) {//FIXME::WTF
            return this.handler.getTitle().getString().toLowerCase(Locale.ROOT).contains(filter) ||
                    this.handler.getConfigId().toString().contains(filter) ||
                    this.handler.getPath().contains(filter) ||
                    this.handler.getSource().name().getString().toLowerCase(Locale.ROOT).contains(filter) ||
                    this.handler.getSide().name().toLowerCase(Locale.ROOT).contains(filter);
        }
    }
}