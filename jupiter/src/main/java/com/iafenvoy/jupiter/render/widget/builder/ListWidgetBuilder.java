package com.iafenvoy.jupiter.render.widget.builder;

import com.iafenvoy.jupiter.config.entry.ListBaseEntry;
import com.iafenvoy.jupiter.config.interfaces.ConfigMetaProvider;
import com.iafenvoy.jupiter.render.screen.JupiterScreen;
import com.iafenvoy.jupiter.render.screen.dialog.ListDialog;
import com.iafenvoy.jupiter.util.MinecraftHelper;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ListWidgetBuilder<T> extends AbstractButtonWidgetBuilder<List<T>> {
    protected final ListBaseEntry<T> config;

    public ListWidgetBuilder(ConfigMetaProvider provider, ListBaseEntry<T> config) {
        super(provider, config, () -> Component.literal(String.valueOf(config.getValue())));
        this.config = config;
    }

    @Override
    protected Button createButton(Context context, int x, int y, int width, int height) {
        return JupiterScreen.createButton(x, y, width, height, this.nameSupplier.get(), _ -> MinecraftHelper.openScreen(new ListDialog<>(context.parent(), context.push(this.config.getName()), this.provider, this.config)));
    }
}
