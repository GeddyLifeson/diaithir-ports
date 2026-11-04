package com.iafenvoy.jupiter.render.screen;

import com.iafenvoy.jupiter.config.container.AbstractConfigContainer;
import com.iafenvoy.jupiter.render.TitleStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public class SingleConfigScreen extends ConfigListScreen {
    private final AbstractConfigContainer container;

    public SingleConfigScreen(Screen parent, AbstractConfigContainer container, boolean client) {
        super(parent, TitleStack.create(container.getTitle()), container.getConfigId(), container.getConfigTabs().getFirst().getConfigs(), client);
        this.container = container;
    }

    @Override
    public void onClose() {
        this.container.onConfigsChanged();
        super.onClose();
    }

    @Override
    protected @Nullable Identifier getBackgroundTexture(boolean ingame) {
        return this.container.getBackgroundTexture(ingame);
    }
}
