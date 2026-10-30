package com.iafenvoy.jupiter.config.entry;

import com.iafenvoy.jupiter.config.interfaces.ConfigEntry;
import com.iafenvoy.jupiter.config.type.ConfigType;
import com.iafenvoy.jupiter.config.type.ConfigTypes;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;

public class MapStringEntry extends MapBaseEntry<String> {
    protected MapStringEntry(Builder builder) {
        super(builder);
    }

    @Override
    public Codec<String> getValueCodec() {
        return Codec.STRING;
    }

    @Override
    public ConfigEntry<Map.Entry<String, String>> newSingleInstance(String value, String key, Runnable reload) {
        return EntryStringEntry.builder(this.name, new AbstractMap.SimpleEntry<>(key, value)).callback((v, r, d) -> {
            if (r) {
                this.getValue().remove(key);
                reload.run();
            } else if (!Objects.equals(key, v.getKey())) {
                this.getValue().remove(key);
                this.getValue().put(v.getKey(), v.getValue());
            } else this.getValue().put(key, v.getValue());
            this.setValue(this.getValue());
        }).build();
    }

    @Override
    public String newValue() {
        return "";
    }

    @Override
    public ConfigType<Map<String, String>> getType() {
        return ConfigTypes.MAP_STRING;
    }

    @Override
    public ConfigEntry<Map<String, String>> newInstance() {
        return new Builder(this).build();
    }

    public static Builder builder(Component name, Map<String, String> defaultValue) {
        return new Builder(name, defaultValue);
    }

    public static Builder builder(String nameKey, Map<String, String> defaultValue) {
        return new Builder(nameKey, defaultValue);
    }

    public static class Builder extends BaseEntry.Builder<Map<String, String>, MapStringEntry, Builder> {
        public Builder(Component name, Map<String, String> defaultValue) {
            super(name, defaultValue);
        }

        public Builder(String nameKey, Map<String, String> defaultValue) {
            super(nameKey, defaultValue);
        }

        public Builder(MapStringEntry parent) {
            super(parent);
        }

        @Override
        public Builder self() {
            return this;
        }

        @Override
        protected MapStringEntry buildInternal() {
            return new MapStringEntry(this);
        }
    }
}
