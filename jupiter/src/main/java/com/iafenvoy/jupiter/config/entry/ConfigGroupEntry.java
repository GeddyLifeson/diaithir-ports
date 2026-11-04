package com.iafenvoy.jupiter.config.entry;

import com.iafenvoy.jupiter.config.ConfigDataFixer;
import com.iafenvoy.jupiter.config.ConfigGroup;
import com.iafenvoy.jupiter.config.interfaces.ConfigEntry;
import com.iafenvoy.jupiter.config.type.ConfigType;
import com.iafenvoy.jupiter.config.type.ConfigTypes;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;

public class ConfigGroupEntry extends BaseEntry<ConfigGroup> {
    protected ConfigGroupEntry(Builder builder) {
        super(builder);
    }

    @Override
    public ConfigType<ConfigGroup> getType() {
        return ConfigTypes.CONFIG_GROUP;
    }

    @Override
    public ConfigEntry<ConfigGroup> newInstance() {
        return new Builder(this).build();
    }

    @Override
    public Codec<ConfigGroup> getCodec() {
        return this.value.getCodec(new ConfigDataFixer());
    }

    @Override
    public Component getName() {
        return super.getName().copy().append("...");
    }

    public static Builder builder(Component name, ConfigGroup defaultValue) {
        return new Builder(name, defaultValue);
    }

    public static Builder builder(String nameKey, ConfigGroup defaultValue) {
        return new Builder(nameKey, defaultValue);
    }

    public static class Builder extends BaseEntry.Builder<ConfigGroup, ConfigGroupEntry, Builder> {
        public Builder(Component name, ConfigGroup defaultValue) {
            super(name, defaultValue);
        }

        public Builder(String nameKey, ConfigGroup defaultValue) {
            super(nameKey, defaultValue);
        }

        public Builder(ConfigGroupEntry parent) {
            super(parent);
        }

        @Override
        public Builder self() {
            return this;
        }

        @Override
        protected ConfigGroupEntry buildInternal() {
            return new ConfigGroupEntry(this);
        }
    }
}
