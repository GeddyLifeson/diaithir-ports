package com.iafenvoy.jupiter.render.screen;

import com.iafenvoy.jupiter.config.container.wrapper.RemoteConfigWrapper;
import com.iafenvoy.jupiter.config.entry.BaseEntry;
import com.iafenvoy.jupiter.config.interfaces.ConfigEntry;
import com.iafenvoy.jupiter.config.interfaces.ConfigMetaProvider;
import com.iafenvoy.jupiter.render.TitleStack;
import com.iafenvoy.jupiter.render.screen.scrollbar.VerticalScrollBar;
import com.iafenvoy.jupiter.render.widget.WidgetBuilder;
import com.iafenvoy.jupiter.util.MinecraftHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ConfigListScreen extends Screen implements JupiterScreen {
    private final Screen parent;
    private final TitleStack titleStack;
    private final Identifier id;
    private final boolean client;
    protected final List<WidgetBuilder<?>> configWidgets = new ArrayList<>();
    protected final VerticalScrollBar entryScrollBar = new VerticalScrollBar();
    protected List<ConfigEntry<?>> entries = List.of();
    protected int topBorder = 30;
    private int configPerPage, textMaxLength;

    public ConfigListScreen(Screen parent, TitleStack titleStack, Identifier id, List<ConfigEntry<?>> entries, boolean client) {
        this(parent, titleStack, id, client);
        this.entries = entries;
    }

    public ConfigListScreen(Screen parent, TitleStack titleStack, Identifier id, boolean client) {
        super(Component.empty());
        this.parent = parent;
        this.titleStack = titleStack;
        this.id = id;
        this.client = client;
    }

    @Override
    protected void init() {
        super.init();
        this.titleStack.cacheTitle(this.width - this.font.width(this.getCurrentEditText()) - 70);
        this.addRenderableWidget(JupiterScreen.createButton(10, 5, 20, ENTRY_HEIGHT, Component.literal("<"), button -> this.onClose()));
        this.calculateMaxEntries();
        this.textMaxLength = Mth.clamp(this.entries.stream().filter(x -> x instanceof BaseEntry).map(ConfigEntry::getName).filter(Objects::nonNull).map(this.font::width).max(Comparator.naturalOrder()).orElse(0) + 30, this.width / 2, this.width - 150);
        this.configWidgets.clear();
        this.configWidgets.addAll(this.entries.stream().map(c -> WidgetBuilderManager.get(new ConfigMetaProvider.SimpleProvider(this.id, "%ERROR%", this.client), c)).toList());
        this.configWidgets.forEach(b -> b.addElements(new WidgetBuilder.Context(this, this::addRenderableWidget, this.titleStack), this.textMaxLength, 0, Math.max(10, this.width - this.textMaxLength - 30), ENTRY_HEIGHT));
        this.updateEntryPos();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        this.calculateMaxEntries();
        this.updateEntryPos();
    }

    @Override
    public @NotNull Component getTitle() {
        return this.titleStack.getTitle();
    }

    public void calculateMaxEntries() {
        this.configPerPage = Math.max(0, (this.height - this.topBorder - 10) / (ENTRY_HEIGHT + ENTRY_SEPARATOR));
        this.entryScrollBar.setMaxValue(Math.max(0, this.entries.size() - this.configPerPage));
    }

    public void updateEntryPos() {
        int top = this.entryScrollBar.getValue();
        for (int i = 0; i < top && i < this.entries.size(); i++)
            this.configWidgets.get(i).update(false, 0);
        for (int i = top; i < top + this.configPerPage && i < this.entries.size(); i++)
            this.configWidgets.get(i).update(true, this.topBorder + ENTRY_SEPARATOR + (i - top) * (ENTRY_HEIGHT + ENTRY_SEPARATOR));
        for (int i = top + this.configPerPage; i < this.entries.size(); i++)
            this.configWidgets.get(i).update(false, 0);
    }

    @Nullable
    public ConfigEntry<?> getMouseOverEntry(int mouseX, int mouseY) {
        return this.configWidgets.stream().filter(widget -> widget.isMouseOver(mouseX, mouseY)).findFirst().map(WidgetBuilder::getConfig).orElse(null);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;
        if (mouseY >= this.topBorder) {
            this.entryScrollBar.setValue(this.entryScrollBar.getValue() + (scrollY > 0 ? -1 : 1) * ENTRIES_PER_SCROLL);
            this.updateEntryPos();
            return true;
        }
        return false;
    }

    @Override
    public void onClose() {
        super.onClose();
        MinecraftHelper.openScreen(this.parent);
    }

    @Nullable
    protected Identifier getBackgroundTexture(boolean ingame) {
        return null;
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor extractor, int mouseX, int mouseY, float a) {
        Identifier texture = this.getBackgroundTexture(this.minecraft.level != null);
        if (texture == null) super.extractBackground(extractor, mouseX, mouseY, a);
        else extractMenuBackgroundTexture(extractor, texture, mouseX, mouseY, 0.0F, 0.0F, this.width, this.height);
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(extractor, mouseX, mouseY, partialTicks);
        String currentText = this.getCurrentEditText();
        int textWidth = this.font.width(currentText);
        extractor.text(this.font, this.getTitle(), 40, 10, -1, true);
        extractor.text(this.font, currentText, this.width - textWidth - 10, 10, -1);
        this.entryScrollBar.render(extractor, mouseX, mouseY, partialTicks, this.width - 18, this.topBorder, 8, this.height - this.topBorder - 10, (this.configPerPage + this.entryScrollBar.getMaxValue()) * (ENTRY_HEIGHT + ENTRY_SEPARATOR));
        if (this.entryScrollBar.isDragging()) this.updateEntryPos();
        ConfigEntry<?> entry = this.getMouseOverEntry(mouseX, mouseY);
        if (entry != null && entry.getTooltip() != null)
            extractor.setTooltipForNextFrame(entry.getTooltip(), mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        int button = event.button();
        if (button == 0 && this.entryScrollBar.wasMouseOver()) {
            this.entryScrollBar.setIsDragging(true);
            this.updateEntryPos();
            return true;
        }
        boolean b = super.mouseClicked(event, isDoubleClick);
        if (!b) this.setFocused(null);
        return b;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        int button = event.button();
        if (button == 0) this.entryScrollBar.setIsDragging(false);
        return super.mouseReleased(event);
    }

    protected String getCurrentEditText() {
        if (this.client) return I18n.get("jupiter.screen.current_modifying_client");
        if (this.entries instanceof RemoteConfigWrapper)
            return I18n.get("jupiter.screen.current_modifying_dedicate_server");
        return I18n.get("jupiter.screen.current_modifying_local_server");
    }
}
