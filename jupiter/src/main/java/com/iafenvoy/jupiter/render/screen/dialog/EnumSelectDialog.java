package com.iafenvoy.jupiter.render.screen.dialog;

import com.iafenvoy.jupiter.config.interfaces.ConfigEntry;
import com.iafenvoy.jupiter.config.interfaces.ConfigMetaProvider;
import com.iafenvoy.jupiter.render.TitleStack;
import com.iafenvoy.jupiter.render.screen.JupiterScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public class EnumSelectDialog<T extends Enum<T>> extends Dialog<T> {
    private EnumSelectWidget<T> widget;
    private boolean initialized = false;

    public EnumSelectDialog(Screen parent, TitleStack titleStack, ConfigMetaProvider provider, ConfigEntry<T> entry) {
        super(parent, titleStack, provider, entry);
    }

    @Override
    protected void init() {
        super.init();
        if (!this.initialized) {
            this.initialized = true;
            this.widget = new EnumSelectWidget<>(this, this.minecraft, this.width - 80, this.height - 80, 60);
        }
        this.widget.updateSize(this.width - 80, new HeaderAndFooterLayout(this, 50, 20));
        this.widget.setX(40);
        this.widget.update();
        this.addRenderableWidget(this.widget);
        this.addRenderableWidget(JupiterScreen.createButton(40, 25, 60, 20, Component.translatable("jupiter.screen.back", new Object[]{}), button -> this.onClose()));
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(extractor, mouseX, mouseY, partialTicks);
        this.widget.extractRenderState(extractor, mouseX, mouseY, partialTicks);
        extractor.centeredText(this.font, this.title, this.width / 2, 10, 0xFFFFFFFF);
    }

    ConfigEntry<T> getEntry() {
        return this.entry;
    }
}
