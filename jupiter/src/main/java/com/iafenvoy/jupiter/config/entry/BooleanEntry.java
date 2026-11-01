package com.iafenvoy.jupiter.config.entry;

import com.iafenvoy.jupiter.config.interfaces.ConfigEntry;
import com.iafenvoy.jupiter.config.type.ConfigType;
import com.iafenvoy.jupiter.config.type.ConfigTypes;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;

public class BooleanEntry extends BaseEntry<Boolean> {
    protected BooleanEntry(Builder builder) {
        super(builder);
    }

    @Override
    public ConfigType<Boolean> getType() {
        return ConfigTypes.BOOLEAN;
    }

    @Override
    public ConfigEntry<Boolean> newInstance() {
        return new Builder(this).build();
    }

    @Override
    public Codec<Boolean> getCodec() {
        return Codec.BOOL;
    }

    public static Builder builder(Component name, boolean defaultValue) {
        return new Builder(name, defaultValue);
    }

    public static Builder builder(String nameKey, boolean defaultValue) {
        return new Builder(nameKey, defaultValue);
    }

    public static class Builder extends BaseEntry.Builder<Boolean, BooleanEntry, Builder> {
        public Builder(Component name, boolean defaultValue) {
            super(name, defaultValue);
        }

        public Builder(String nameKey, boolean defaultValue) {
            super(nameKey, defaultValue);
        }

        public Builder(BooleanEntry parent) {
            super(parent);
        }

        @Override
        public Builder self() {
            return this;
        }

        @Override
        protected BooleanEntry buildInternal() {
            return new BooleanEntry(this);
        }
    }
}
