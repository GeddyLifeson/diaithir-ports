package com.iafenvoy.jupiter.config.entry;

import com.iafenvoy.jupiter.config.interfaces.ConfigEntry;
import com.iafenvoy.jupiter.config.interfaces.TextFieldConfigEntry;
import com.iafenvoy.jupiter.config.type.ConfigType;
import com.iafenvoy.jupiter.config.type.ConfigTypes;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Objects;

public class IdentifierEntry extends BaseEntry<Identifier> implements TextFieldConfigEntry {
    protected IdentifierEntry(Builder builder) {
        super(builder);
    }

    @Override
    public ConfigType<Identifier> getType() {
        return ConfigTypes.RESOURCE_LOCATION;
    }

    @Override
    public ConfigEntry<Identifier> newInstance() {
        return new Builder(this).build();
    }

    @Override
    public Codec<Identifier> getCodec() {
        return Identifier.CODEC;
    }

    @Override
    public String valueAsString() {
        return this.getValue().toString();
    }

    @Override
    public void setValueFromString(String s) {
        this.setValue(Objects.requireNonNull(Identifier.tryParse(s)));
    }

    public static Builder builder(Component name, Identifier defaultValue) {
        return new Builder(name, defaultValue);
    }

    public static Builder builder(String nameKey, Identifier defaultValue) {
        return new Builder(nameKey, defaultValue);
    }

    public static class Builder extends BaseEntry.Builder<Identifier, IdentifierEntry, Builder> {
        public Builder(Component name, Identifier defaultValue) {
            super(name, defaultValue);
        }

        public Builder(String nameKey, Identifier defaultValue) {
            super(nameKey, defaultValue);
        }

        public Builder(IdentifierEntry parent) {
            super(parent);
        }

        @Override
        public Builder self() {
            return this;
        }

        @Override
        protected IdentifierEntry buildInternal() {
            return new IdentifierEntry(this);
        }
    }
}
