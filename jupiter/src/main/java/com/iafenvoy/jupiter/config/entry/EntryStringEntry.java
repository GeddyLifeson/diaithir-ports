package com.iafenvoy.jupiter.config.entry;

import com.iafenvoy.jupiter.config.interfaces.ConfigEntry;
import com.iafenvoy.jupiter.config.type.ConfigType;
import com.iafenvoy.jupiter.config.type.ConfigTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;

import java.util.AbstractMap;
import java.util.Map;

@ApiStatus.Internal
public class EntryStringEntry extends EntryBaseEntry<String> {
    protected EntryStringEntry(Builder builder) {
        super(builder);
    }

    @Override
    public ConfigEntry<String> newValueInstance() {
        return StringEntry.builder(this.name, this.value.getValue()).build();
    }

    @Override
    public ConfigType<Map.Entry<String, String>> getType() {
        return ConfigTypes.ENTRY_STRING;
    }

    @Override
    public ConfigEntry<Map.Entry<String, String>> newInstance() {
        return new Builder(this).build();
    }

    @Override
    public Codec<Map.Entry<String, String>> getCodec() {
        return RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("key").forGetter(Map.Entry::getKey),
                Codec.STRING.fieldOf("value").forGetter(Map.Entry::getValue)
        ).apply(i, AbstractMap.SimpleEntry::new));
    }

    public static Builder builder(Component name, Map.Entry<String, String> defaultValue) {
        return new Builder(name, defaultValue);
    }

    public static Builder builder(String nameKey, Map.Entry<String, String> defaultValue) {
        return new Builder(nameKey, defaultValue);
    }

    public static class Builder extends BaseEntry.Builder<Map.Entry<String, String>, EntryStringEntry, Builder> {
        public Builder(Component name, Map.Entry<String, String> defaultValue) {
            super(name, defaultValue);
        }

        public Builder(String nameKey, Map.Entry<String, String> defaultValue) {
            super(nameKey, defaultValue);
        }

        public Builder(EntryStringEntry parent) {
            super(parent);
        }

        @Override
        public Builder self() {
            return this;
        }

        @Override
        protected EntryStringEntry buildInternal() {
            return new EntryStringEntry(this);
        }
    }
}
