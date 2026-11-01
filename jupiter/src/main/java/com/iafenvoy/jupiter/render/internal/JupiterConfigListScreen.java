package com.iafenvoy.jupiter.render.internal;

import com.iafenvoy.jupiter.compat.ExtraConfigManager;
import com.iafenvoy.jupiter.config.ConfigSide;
import com.iafenvoy.jupiter.config.container.AbstractConfigContainer;
import com.iafenvoy.jupiter.config.container.wrapper.RemoteConfigWrapper;
import com.iafenvoy.jupiter.network.ClientConfigNetwork;
import com.iafenvoy.jupiter.render.screen.JupiterScreen;
import com.iafenvoy.jupiter.util.MinecraftHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;

import java.util.LinkedHashMap;
import java.util.Map;

@ApiStatus.Internal
public class JupiterConfigListScreen extends Screen implements JupiterScreen {
    private final Map<AbstractConfigContainer, RemoteConfigWrapper> remoteCache = new LinkedHashMap<>();
    private final Screen parent;
    private JupiterConfigListWidget widget;
    private Button editLocalButton = null, editRemoteButton = null;
    private boolean initialized = false;

    public JupiterConfigListScreen(Screen parent) {
        super(Component.translatable("jupiter.screen.config_list.title", new Object[]{}));
        this.parent = parent;
        ExtraConfigManager.scanConfigs();
    }

    @Override
    protected void init() {
        super.init();
        if (!this.initialized) {
            this.initialized = true;
            this.widget = new JupiterConfigListWidget(this, this.minecraft, this.width - 80, this.height - 80, 60);
        }
        this.widget.updateSize(this.width - 80, new HeaderAndFooterLayout(this, 50, 20));
        this.widget.setX(40);
        this.widget.update();
        this.remoteCache.clear();

        this.addRenderableWidget(this.widget);
        this.addRenderableWidget(JupiterScreen.createButton(40, 25, 60, 20, Component.translatable("jupiter.screen.back", new Object[]{}), button -> this.onClose()));
        this.addRenderableWidget(new EditBox(this.font, 105, 25, Math.max(10, this.width - 315), 20, Component.empty())).setResponder(this.widget::setFilter);
        this.editLocalButton = this.addRenderableWidget(JupiterScreen.createButton(this.width - 205, 25, 80, 20, Component.translatable("jupiter.screen.edit_local", new Object[]{}), button -> {
            JupiterConfigListWidget.Entry handler = this.widget.getSelected();
            if (handler != null)
                MinecraftHelper.openScreen(JupiterScreen.getConfigScreen(this, handler.getConfigContainer(), false));
        }));
        this.editRemoteButton = this.addRenderableWidget(JupiterScreen.createButton(this.width - 120, 25, 80, 20, Component.translatable("jupiter.screen.edit_remote", new Object[]{}), button -> {
            JupiterConfigListWidget.Entry handler = this.widget.getSelected();
            if (handler != null) {
                AbstractConfigContainer container = handler.getConfigContainer();
                if (this.remoteCache.containsKey(container))
                    MinecraftHelper.openScreen(JupiterScreen.getConfigScreen(this, this.remoteCache.get(container), false));
            }
        }));
        this.updateButtonState();
    }

    public void updateButtonState() {
        JupiterConfigListWidget.Entry entry = this.widget.getSelected();
        if (this.editLocalButton != null) this.editLocalButton.active = entry != null;
        if (this.editRemoteButton != null) {
            this.editRemoteButton.active = false;
            if (entry == null) return;
            final AbstractConfigContainer origin = entry.getConfigContainer();
            if (origin.getSide() == ConfigSide.CLIENT || this.remoteCache.containsKey(origin) || !JupiterScreen.connectedToDedicatedServer())
                return;
            ClientConfigNetwork.syncConfig(origin.getConfigId(), tag -> {
                if (tag != null) {
                    this.editRemoteButton.active = true;
                    RemoteConfigWrapper wrapper = new RemoteConfigWrapper(origin);
                    wrapper.deserializeNbt(tag);
                    this.remoteCache.put(origin, wrapper);
                }
            });
        }
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta) {
        super.extractRenderState(extractor, mouseX, mouseY, delta);
        this.widget.extractRenderState(extractor, mouseX, mouseY, delta);
        extractor.centeredText(this.font, this.title, this.width / 2, 10, 0xFFFFFFFF);
    }

    @Override
    public void onClose() {
        super.onClose();
        MinecraftHelper.openScreen(this.parent);
    }
}
