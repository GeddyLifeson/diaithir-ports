package com.iafenvoy.jupiter.config.entry;

import com.iafenvoy.jupiter.config.interfaces.ConfigEntry;
import com.iafenvoy.jupiter.config.type.ConfigType;
import com.iafenvoy.jupiter.config.type.ConfigTypes;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;

public class MapIntegerEntry extends MapBaseEntry<Integer> {
    protected MapIntegerEntry(Builder builder) {
        super(builder);
    }

    @Override
    public Codec<Integer> getValueCodec() {
        return Codec.INT;
    }

    @Override
    public ConfigEntry<Map.Entry<String, Integer>> newSingleInstance(Integer value, String key, Runnable reload) {
        return EntryIntegerEntry.builder(this.name, new AbstractMap.SimpleEntry<>(key, value)).callback((v, r, d) -> {
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
    public Integer newValue() {
        return 0;
    }

    @Override
    public ConfigType<Map<String, Integer>> getType() {
        return ConfigTypes.MAP_INTEGER;
    }

    @Override
    public ConfigEntry<Map<String, Integer>> newInstance() {
        return new Builder(this).build();
    }

    public static Builder builder(Component name, Map<String, Integer> defaultValue) {
        return new Builder(name, defaultValue);
    }

    public static Builder builder(String nameKey, Map<String, Integer> defaultValue) {
        return new Builder(nameKey, defaultValue);
    }

    public static class Builder extends BaseEntry.Builder<Map<String, Integer>, MapIntegerEntry, Builder> {
        public Builder(Component name, Map<String, Integer> defaultValue) {
            super(name, defaultValue);
        }

        public Builder(String nameKey, Map<String, Integer> defaultValue) {
            super(nameKey, defaultValue);
        }

        public Builder(MapIntegerEntry parent) {
            super(parent);
        }

        @Override
        public Builder self() {
            return this;
        }

        @Override
        protected MapIntegerEntry buildInternal() {
            return new MapIntegerEntry(this);
        }
    }
}
