package com.iafenvoy.jupiter.render.widget.builder;

import com.iafenvoy.jupiter.config.interfaces.ConfigEntry;
import com.iafenvoy.jupiter.config.interfaces.ConfigMetaProvider;
import com.iafenvoy.jupiter.config.interfaces.TextFieldConfigEntry;
import com.iafenvoy.jupiter.render.widget.TextFieldWithErrorWidget;
import com.iafenvoy.jupiter.render.widget.WidgetBuilder;
import org.jetbrains.annotations.Nullable;

public class TextFieldWidgetBuilder<T> extends WidgetBuilder<T> {
    private final TextFieldConfigEntry textFieldConfig;
    @Nullable
    private TextFieldWithErrorWidget widget;

    public TextFieldWidgetBuilder(ConfigMetaProvider provider, ConfigEntry<T> config) {
        super(provider, config);
        if (config instanceof TextFieldConfigEntry t) this.textFieldConfig = t;
        else throw new IllegalArgumentException("TextFieldWidgetBuilder only accept TextFieldConfigEntry");
    }

    @Override
    public void addCustomElements(Context context, int x, int y, int width, int height) {
        this.widget = new TextFieldWithErrorWidget(this.minecraft.font, x, y, width, height);
        this.widget.setValue(this.textFieldConfig.valueAsString());
        this.widget.setResponder(s -> {
            try {
                this.textFieldConfig.setValueFromString(s);
                this.canSave = true;
                this.widget.setHasError(false);
            } catch (Exception ignored) {
                this.canSave = false;
                this.widget.setHasError(true);
                this.setCanReset(true);
            }
        });
        context.addWidget(this.widget);
    }

    @Override
    public void updateCustom(boolean visible, int y) {
        if (this.widget == null) return;
        this.widget.visible = visible;
        this.widget.setY(y);
    }

    @Override
    public void refresh() {
        if (this.widget == null) return;
        this.widget.setValue(this.textFieldConfig.valueAsString());
    }
}
