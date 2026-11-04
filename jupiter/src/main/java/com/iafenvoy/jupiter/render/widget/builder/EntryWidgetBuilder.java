package com.iafenvoy.jupiter.render.widget.builder;

import com.iafenvoy.jupiter.config.entry.EntryBaseEntry;
import com.iafenvoy.jupiter.config.interfaces.ConfigMetaProvider;
import com.iafenvoy.jupiter.render.screen.WidgetBuilderManager;
import com.iafenvoy.jupiter.render.widget.WidgetBuilder;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.Map;

public class EntryWidgetBuilder<T> extends WidgetBuilder<Map.Entry<String, T>> {
    private final EntryBaseEntry<T> config;
    @Nullable
    private EditBox keyWidget;
    @Nullable
    private WidgetBuilder<T> valueBuilder;

    public EntryWidgetBuilder(ConfigMetaProvider provider, EntryBaseEntry<T> config) {
        super(provider, config);
        this.config = config;
    }

    @Override
    public void addCustomElements(Context context, int x, int y, int width, int height) {
        this.keyWidget = new EditBox(this.minecraft.font, x, y, width / 2 - 5, height, Component.empty());
        this.keyWidget.setValue(this.config.getValue().getKey());
        this.keyWidget.setResponder(s -> this.config.setValue(new AbstractMap.SimpleEntry<>(s, this.config.getValue().getValue())));
        context.addWidget(this.keyWidget);
        //TODO::Move newValueInstance to get config type
        this.valueBuilder = WidgetBuilderManager.get(this.provider, this.config.newValueInstance());
        this.valueBuilder.addCustomElements(context, x + width / 2, y, width / 2, height);
    }

    @Override
    public void updateCustom(boolean visible, int y) {
        if (this.keyWidget != null) {
            this.keyWidget.visible = visible;
            this.keyWidget.setY(y);
        }
        if (this.valueBuilder != null) this.valueBuilder.update(visible, y);
    }

    @Override
    public void refresh() {
        if (this.keyWidget != null) this.keyWidget.setValue(this.config.getValue().getKey());
        if (this.valueBuilder != null) this.valueBuilder.refresh();
    }
}
