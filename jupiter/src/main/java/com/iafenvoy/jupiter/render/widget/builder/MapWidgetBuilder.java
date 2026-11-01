package com.iafenvoy.jupiter.render.widget.builder;

import com.iafenvoy.jupiter.config.entry.MapBaseEntry;
import com.iafenvoy.jupiter.config.interfaces.ConfigMetaProvider;
import com.iafenvoy.jupiter.render.screen.JupiterScreen;
import com.iafenvoy.jupiter.render.screen.dialog.MapDialog;
import com.iafenvoy.jupiter.util.MinecraftHelper;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.Map;

public class MapWidgetBuilder<T> extends AbstractButtonWidgetBuilder<Map<String, T>> {
    protected final MapBaseEntry<T> config;

    public MapWidgetBuilder(ConfigMetaProvider provider, MapBaseEntry<T> config) {
        super(provider, config, () -> Component.literal(String.valueOf(config.getValue())));
        this.config = config;
    }

    @Override
    protected Button createButton(Context context, int x, int y, int width, int height) {
        return JupiterScreen.createButton(x, y, width, height, this.nameSupplier.get(), _ -> MinecraftHelper.openScreen(new MapDialog<>(context.parent(), context.push(this.config.getName()), this.provider, this.config)));
    }
}
