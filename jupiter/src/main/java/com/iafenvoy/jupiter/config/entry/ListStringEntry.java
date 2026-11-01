package com.iafenvoy.jupiter.config.entry;

import com.iafenvoy.jupiter.config.interfaces.ConfigEntry;
import com.iafenvoy.jupiter.config.type.ConfigType;
import com.iafenvoy.jupiter.config.type.ConfigTypes;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ListStringEntry extends ListBaseEntry<String> {
    protected ListStringEntry(Builder builder) {
        super(builder);
    }

    @Override
    public Codec<String> getValueCodec() {
        return Codec.STRING;
    }

    @Override
    public ConfigEntry<String> newSingleInstance(String value, int index, Runnable reload) {
        return StringEntry.builder(this.name, value).callback((v, r, d) -> {
            if (r) {
                this.getValue().remove(index);
                reload.run();
            } else this.getValue().set(index, v);
            this.setValue(this.getValue());
        }).build();
    }

    @Override
    public String newValue() {
        return "";
    }

    @Override
    public ConfigType<List<String>> getType() {
        return ConfigTypes.LIST_STRING;
    }

    @Override
    public ConfigEntry<List<String>> newInstance() {
        return new Builder(this).build();
    }

    public static Builder builder(Component name, List<String> defaultValue) {
        return new Builder(name, defaultValue);
    }

    public static Builder builder(String nameKey, List<String> defaultValue) {
        return new Builder(nameKey, defaultValue);
    }

    public static class Builder extends BaseEntry.Builder<List<String>, ListStringEntry, Builder> {
        public Builder(Component name, List<String> defaultValue) {
            super(name, defaultValue);
        }

        public Builder(String nameKey, List<String> defaultValue) {
            super(nameKey, defaultValue);
        }

        public Builder(ListStringEntry parent) {
            super(parent);
        }

        @Override
        public Builder self() {
            return this;
        }

        @Override
        protected ListStringEntry buildInternal() {
            return new ListStringEntry(this);
        }
    }
}
