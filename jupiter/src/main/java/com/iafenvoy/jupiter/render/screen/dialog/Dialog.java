package com.iafenvoy.jupiter.render.screen.dialog;

import com.iafenvoy.jupiter.config.interfaces.ConfigEntry;
import com.iafenvoy.jupiter.config.interfaces.ConfigMetaProvider;
import com.iafenvoy.jupiter.render.TitleStack;
import com.iafenvoy.jupiter.render.screen.JupiterScreen;
import com.iafenvoy.jupiter.util.MinecraftHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class Dialog<T> extends Screen implements JupiterScreen {
    private final Screen parent;
    protected final TitleStack titleStack;
    protected final ConfigMetaProvider provider;
    protected final ConfigEntry<T> entry;

    protected Dialog(Screen parent, TitleStack titleStack, ConfigMetaProvider provider, ConfigEntry<T> entry) {
        super(Component.empty());
        this.parent = parent;
        this.titleStack = titleStack;
        this.provider = provider;
        this.entry = entry;
    }

    @Override
    protected void init() {
        super.init();
        this.titleStack.cacheTitle(this.width - 130);
    }

    @Override
    public @NotNull Component getTitle() {
        return this.titleStack.getTitle();
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(extractor, mouseX, mouseY, partialTicks);
        extractor.text(this.font, this.getTitle(), 40, 10, 0xFFFFFFFF, true);
    }

    @Override
    public void onClose() {
        super.onClose();
        MinecraftHelper.openScreen(this.parent);
    }
}
