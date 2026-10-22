package com.iafenvoy.jupiter.render.widget.builder;

import com.iafenvoy.jupiter.config.interfaces.ConfigEntry;
import com.iafenvoy.jupiter.config.interfaces.ConfigMetaProvider;
import com.iafenvoy.jupiter.render.screen.JupiterScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public class ButtonWidgetBuilder<T> extends AbstractButtonWidgetBuilder<T> {
    private final Button.OnPress action;

    public ButtonWidgetBuilder(ConfigMetaProvider provider, ConfigEntry<T> config, Button.OnPress action, Supplier<Component> nameSupplier) {
        super(provider, config, nameSupplier);
        this.action = button -> {
            action.onPress(button);
            this.refresh();
        };
    }

    @Override
    protected Button createButton(Context context, int x, int y, int width, int height) {
        return JupiterScreen.createButton(x, y, width, height, this.nameSupplier.get(), this.action);
    }
}
