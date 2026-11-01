package com.iafenvoy.jupiter.config.entry;

import com.iafenvoy.jupiter.config.interfaces.ConfigBuilder;
import com.iafenvoy.jupiter.config.interfaces.ConfigEntry;
import com.iafenvoy.jupiter.config.interfaces.ValueChangeCallback;
import com.iafenvoy.jupiter.config.type.ConfigType;
import com.iafenvoy.jupiter.config.type.ConfigTypes;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class SeparatorEntry implements ConfigEntry<Unit> {
    private final Component text, tooltip;

    protected SeparatorEntry(Builder builder) {
        this.text = builder.text;
        this.tooltip = builder.tooltip;
    }

    @Override
    public ConfigType<Unit> getType() {
        return ConfigTypes.SEPARATOR;
    }

    @Override
    public @Nullable String getKey() {
        return null;
    }

    @Override
    public Component getName() {
        return this.text;
    }

    @Override
    public Component getTooltip() {
        return this.tooltip;
    }

    @Override
    public ConfigEntry<Unit> newInstance() {
        return new Builder().build();
    }

    @Override
    public void registerCallback(ValueChangeCallback<Unit> callback) {
    }

    @Override
    public Unit getValue() {
        return Unit.INSTANCE;
    }

    @Override
    public Unit getDefaultValue() {
        return Unit.INSTANCE;
    }

    @Override
    public void setValue(Unit value) {
    }

    @Override
    public Codec<Unit> getCodec() {
        return Codec.EMPTY.codec();
    }

    @Override
    public void reset() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder implements ConfigBuilder<Unit, SeparatorEntry, Builder> {
        protected Component text;
        @Nullable
        protected Component tooltip;
        protected boolean visible = true;

        public Builder() {
        }

        public Builder text(String textKey) {
            return this.text(Component.translatable(textKey, new Object[]{}));
        }

        public Builder text(Component text) {
            this.text = text;
            return this;
        }

        @Override
        public Builder tooltip(String tooltipKey) {
            return this.tooltip(Component.translatable(tooltipKey, new Object[]{}));
        }

        @Override
        public Builder tooltip(Component tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        @Override
        public Builder callback(ValueChangeCallback<Unit> callback) {
            return this;
        }

        @Override
        public Builder value(Unit value) {
            return this;
        }

        @Override
        public SeparatorEntry build() {
            return new SeparatorEntry(this);
        }
    }
}
