package com.iafenvoy.jupiter.render.widget.builder;

import com.iafenvoy.jupiter.config.interfaces.ConfigEntry;
import com.iafenvoy.jupiter.config.interfaces.ConfigMetaProvider;
import com.iafenvoy.jupiter.render.widget.WidgetBuilder;
import com.mojang.datafixers.util.Unit;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;

public class SeparatorWidgetBuilder extends WidgetBuilder<Unit> {
    public SeparatorWidgetBuilder(ConfigMetaProvider provider, ConfigEntry<Unit> config) {
        super(provider, config);
    }

    @Override
    public void addElements(Context context, int x, int y, int width, int height) {
        Font font = this.minecraft.font;
        width = width + x - 20;
        Component text;
        if (this.config.getName() == null) {
            int w = font.width("-"), k = 0;
            while ((k + 1) * w <= width) k++;
            text = Component.literal("-".repeat(k));
        } else text = this.config.getName();
        this.textWidget = new StringWidget(20, y, font.width(text), height, text, font);
        context.addWidget(this.textWidget);
    }

    @Override
    public void addCustomElements(Context context, int x, int y, int width, int height) {
        //No Need
    }

    @Override
    public void updateCustom(boolean visible, int y) {
        //No Need
    }

    @Override
    public void refresh() {
        //No Need
    }
}
