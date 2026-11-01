package com.iafenvoy.jupiter.config.entry;

import com.iafenvoy.jupiter.config.interfaces.ConfigEntry;
import com.iafenvoy.jupiter.config.type.ConfigType;
import com.iafenvoy.jupiter.config.type.ConfigTypes;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ListLongEntry extends ListBaseEntry<Long> {
    protected ListLongEntry(Builder builder) {
        super(builder);
    }

    @Override
    public Codec<Long> getValueCodec() {
        return Codec.LONG;
    }

    @Override
    public ConfigEntry<Long> newSingleInstance(Long value, int index, Runnable reload) {
        return LongEntry.builder(this.name, value).callback((v, r, d) -> {
            if (r) {
                this.getValue().remove(index);
                reload.run();
            } else this.getValue().set(index, v);
            this.setValue(this.getValue());
        }).build();
    }

    @Override
    public Long newValue() {
        return 0L;
    }

    @Override
    public ConfigType<List<Long>> getType() {
        return ConfigTypes.LIST_LONG;
    }

    @Override
    public ConfigEntry<List<Long>> newInstance() {
        return new Builder(this).build();
    }

    public static Builder builder(Component name, List<Long> defaultValue) {
        return new Builder(name, defaultValue);
    }

    public static Builder builder(String nameKey, List<Long> defaultValue) {
        return new Builder(nameKey, defaultValue);
    }

    public static class Builder extends BaseEntry.Builder<List<Long>, ListLongEntry, Builder> {
        public Builder(Component name, List<Long> defaultValue) {
            super(name, defaultValue);
        }

        public Builder(String nameKey, List<Long> defaultValue) {
            super(nameKey, defaultValue);
        }

        public Builder(ListLongEntry parent) {
            super(parent);
        }

        @Override
        public Builder self() {
            return this;
        }

        @Override
        protected ListLongEntry buildInternal() {
            return new ListLongEntry(this);
        }
    }
}
