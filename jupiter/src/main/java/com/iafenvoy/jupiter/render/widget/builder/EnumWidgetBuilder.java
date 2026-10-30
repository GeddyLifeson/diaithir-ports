package com.iafenvoy.jupiter.render.widget.builder;

import com.iafenvoy.jupiter.config.interfaces.ConfigEntry;
import com.iafenvoy.jupiter.config.interfaces.ConfigMetaProvider;
import com.iafenvoy.jupiter.render.screen.JupiterScreen;
import com.iafenvoy.jupiter.render.screen.dialog.EnumSelectDialog;
import com.iafenvoy.jupiter.util.MinecraftHelper;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class EnumWidgetBuilder<T extends Enum<T>> extends AbstractButtonWidgetBuilder<T> {
    public EnumWidgetBuilder(ConfigMetaProvider provider, ConfigEntry<T> config) {
        super(provider, config, () -> Component.literal(config.getValue().name()));
    }

    @Override
    protected Button createButton(Context context, int x, int y, int width, int height) {
        return JupiterScreen.createButton(x, y, width, height, this.nameSupplier.get(), _ -> MinecraftHelper.openScreen(new EnumSelectDialog<>(context.parent(), context.push(this.config.getName()), this.provider, this.config)));
    }
}
