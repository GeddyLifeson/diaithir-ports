package com.iafenvoy.jupiter.config.entry;

import com.iafenvoy.jupiter.config.interfaces.ConfigEntry;
import com.iafenvoy.jupiter.config.interfaces.TextFieldConfigEntry;
import com.iafenvoy.jupiter.config.type.ConfigType;
import com.iafenvoy.jupiter.config.type.ConfigTypes;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;

public class StringEntry extends BaseEntry<String> implements TextFieldConfigEntry {
    protected StringEntry(Builder builder) {
        super(builder);
    }

    @Override
    public ConfigType<String> getType() {
        return ConfigTypes.STRING;
    }

    @Override
    public ConfigEntry<String> newInstance() {
        return new Builder(this).build();
    }

    @Override
    public Codec<String> getCodec() {
        return Codec.STRING;
    }

    @Override
    public String valueAsString() {
        return this.getValue();
    }

    @Override
    public void setValueFromString(String s) {
        this.setValue(s);
    }

    public static Builder builder(Component name, String defaultValue) {
        return new Builder(name, defaultValue);
    }

    public static Builder builder(String nameKey, String defaultValue) {
        return new Builder(nameKey, defaultValue);
    }

    public static class Builder extends BaseEntry.Builder<String, StringEntry, Builder> {
        public Builder(Component name, String defaultValue) {
            super(name, defaultValue);
        }

        public Builder(String nameKey, String defaultValue) {
            super(nameKey, defaultValue);
        }

        public Builder(StringEntry parent) {
            super(parent);
        }

        @Override
        public Builder self() {
            return this;
        }

        @Override
        protected StringEntry buildInternal() {
            return new StringEntry(this);
        }
    }
}
