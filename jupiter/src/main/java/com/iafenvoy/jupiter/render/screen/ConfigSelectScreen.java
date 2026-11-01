package com.iafenvoy.jupiter.render.screen;

import com.iafenvoy.jupiter.Jupiter;
import com.iafenvoy.jupiter.config.container.AbstractConfigContainer;
import com.iafenvoy.jupiter.config.container.wrapper.RemoteConfigWrapper;
import com.iafenvoy.jupiter.network.ClientConfigNetwork;
import com.iafenvoy.jupiter.util.MinecraftHelper;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class ConfigSelectScreen extends Screen implements JupiterScreen {
    private final Screen parent;
    @Nullable
    private final AbstractConfigContainer common, client, server;
    private final boolean displayCommon;

    protected ConfigSelectScreen(Component title, Screen parent, @Nullable AbstractConfigContainer common, @Nullable AbstractConfigContainer client, @Nullable AbstractConfigContainer server, boolean displayCommon) {
        super(title);
        this.parent = parent;
        this.common = common;
        this.client = client;
        this.server = server;
        this.displayCommon = displayCommon;
    }

    @Override
    protected void init() {
        super.init();
        int x = this.width / 2;
        int y = this.height / 2;
        //Back
        this.addRenderableWidget(JupiterScreen.createButton(x - 100, y - (this.displayCommon ? 60 : 45), 200, 20, Component.translatable("jupiter.screen.back", new Object[]{}), _ -> MinecraftHelper.openScreen(this.parent)));

        final boolean connectedToRemote = JupiterScreen.connectedToDedicatedServer();

        if (this.displayCommon) {
            String text = this.common != null ? "jupiter.screen.open_local_common" : "jupiter.screen.unavailable";
            this.addRenderableWidget(JupiterScreen.createButtonWithTooltip(this, x - 100, y - 30, 95, 20, Component.translatable("jupiter.screen.local_common_config", new Object[]{}), _ -> {
                assert this.common != null;
                MinecraftHelper.openScreen(JupiterScreen.getConfigScreen(this, this.common, true));
            }, Component.translatable(text, new Object[]{}))).active = this.common != null;

            Pair<Button, Consumer<Component>> commonPair = JupiterScreen.createButtonWithDynamicTooltip(this, x + 5, y - 30, 95, 20, Component.translatable("jupiter.screen.remote_common_config", new Object[]{}), _ -> {
                assert this.common != null;
                MinecraftHelper.openScreen(JupiterScreen.getConfigScreen(this, new RemoteConfigWrapper(this.common), false));
            }, Component.translatable("jupiter.screen.unavailable", new Object[]{}));
            this.addRenderableWidget(commonPair.getFirst()).active = this.common != null && connectedToRemote;
            if (this.common != null)
                if (connectedToRemote)
                    handleRemoteConfig(this.common, "jupiter.screen.open_remote_common", b -> commonPair.getFirst().active = b, commonPair.getSecond());
                else
                    commonPair.getSecond().accept(Component.translatable("jupiter.screen.need_remote_server", new Object[]{}));
        }

        String text1 = this.server != null ? "jupiter.screen.open_local_server" : "jupiter.screen.unavailable";
        this.addRenderableWidget(JupiterScreen.createButtonWithTooltip(this, x - 100, y - (this.displayCommon ? 0 : 15), 95, 20, Component.translatable("jupiter.screen.local_server_config", new Object[]{}), button -> {
            assert this.server != null;
            MinecraftHelper.openScreen(JupiterScreen.getConfigScreen(this, this.server, true));
        }, Component.translatable(text1, new Object[]{}))).active = this.server != null;

        Pair<Button, Consumer<Component>> serverPair = JupiterScreen.createButtonWithDynamicTooltip(this, x + 5, y - (this.displayCommon ? 0 : 15), 95, 20, Component.translatable("jupiter.screen.remove_server_config", new Object[]{}), button -> {
            assert this.server != null;
            MinecraftHelper.openScreen(JupiterScreen.getConfigScreen(this, new RemoteConfigWrapper(this.server), false));
        }, Component.translatable("jupiter.screen.unavailable", new Object[]{}));
        this.addRenderableWidget(serverPair.getFirst()).active = this.server != null && connectedToRemote;
        if (this.server != null)
            if (connectedToRemote)
                handleRemoteConfig(this.server, "jupiter.screen.open_remote_server", b -> serverPair.getFirst().active = b, serverPair.getSecond());
            else
                serverPair.getSecond().accept(Component.translatable("jupiter.screen.need_remote_server", new Object[]{}));

        String text = this.client != null ? "jupiter.screen.open_client" : "jupiter.screen.unavailable";
        this.addRenderableWidget(JupiterScreen.createButtonWithTooltip(this, x - 100, y + (this.displayCommon ? 30 : 15), 200, 20, Component.translatable("jupiter.screen.client_config", new Object[]{}), button -> {
            assert this.client != null;
            MinecraftHelper.openScreen(JupiterScreen.getConfigScreen(this, this.client, true));
        }, Component.translatable(text, new Object[]{}))).active = this.client != null;
    }

    private static void handleRemoteConfig(AbstractConfigContainer container, String openKey, BooleanConsumer buttonActive, Consumer<Component> tooltip) {
        tooltip.accept(Component.translatable("jupiter.screen.check_server", new Object[]{}));
        buttonActive.accept(false);
        ClientConfigNetwork.syncConfig(container.getConfigId(), nbt -> {
            if (nbt == null) tooltip.accept(Component.translatable("jupiter.screen.disable_server", new Object[]{}));
            else {
                try {
                    container.deserializeNbt(nbt);
                    tooltip.accept(Component.translatable(openKey, new Object[]{}));
                    buttonActive.accept(true);
                } catch (Exception e) {
                    Jupiter.LOGGER.error("Failed to parse config data from server: {}", container.getConfigId(), e);
                    tooltip.accept(Component.translatable("jupiter.screen.error_server", new Object[]{}));
                }
            }
        });
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor extractor, int mouseX, int mouseY, float particleTicks) {
        super.extractRenderState(extractor, mouseX, mouseY, particleTicks);
        extractor.centeredText(this.minecraft.font, this.title, this.width / 2, this.height / 2 - (this.displayCommon ? 80 : 65), -1);
    }

    @Override
    public void onClose() {
        super.onClose();
        MinecraftHelper.openScreen(this.parent);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    public static Builder builder(String titleKey, Screen parent) {
        return new Builder(titleKey, parent);
    }

    public static Builder builder(Component title, Screen parent) {
        return new Builder(title, parent);
    }

    public static class Builder {
        private final Component title;
        private final Screen parent;
        private AbstractConfigContainer common, client, server;
        private boolean displayCommon = false;

        public Builder(String titleKey, Screen parent) {
            this(Component.translatable(titleKey, new Object[]{}), parent);
        }

        public Builder(Component title, Screen parent) {
            this.title = title;
            this.parent = parent;
        }

        public Builder common(AbstractConfigContainer common) {
            this.common = common;
            return this.displayCommon();
        }

        public Builder displayCommon() {
            this.displayCommon = true;
            return this;
        }

        public Builder client(AbstractConfigContainer client) {
            this.client = client;
            return this;
        }

        public Builder server(AbstractConfigContainer server) {
            this.server = server;
            return this;
        }

        public ConfigSelectScreen build() {
            return new ConfigSelectScreen(this.title, this.parent, this.common, this.client, this.server, this.displayCommon);
        }
    }
}
