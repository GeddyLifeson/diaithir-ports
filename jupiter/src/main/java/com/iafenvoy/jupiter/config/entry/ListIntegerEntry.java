package com.iafenvoy.jupiter.config.entry;

import com.iafenvoy.jupiter.config.interfaces.ConfigEntry;
import com.iafenvoy.jupiter.config.type.ConfigType;
import com.iafenvoy.jupiter.config.type.ConfigTypes;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ListIntegerEntry extends ListBaseEntry<Integer> {
    protected ListIntegerEntry(Builder builder) {
        super(builder);
    }

    @Override
    public Codec<Integer> getValueCodec() {
        return Codec.INT;
    }

    @Override
    public ConfigEntry<Integer> newSingleInstance(Integer value, int index, Runnable reload) {
        return IntegerEntry.builder(this.name, value).callback((v, r, d) -> {
            if (r) {
                this.getValue().remove(index);
                reload.run();
            } else this.getValue().set(index, v);
            this.setValue(this.getValue());
        }).build();
    }

    @Override
    public Integer newValue() {
        return 0;
    }

    @Override
    public ConfigType<List<Integer>> getType() {
        return ConfigTypes.LIST_INTEGER;
    }

    @Override
    public ConfigEntry<List<Integer>> newInstance() {
        return new Builder(this).build();
    }

    public static Builder builder(Component name, List<Integer> defaultValue) {
        return new Builder(name, defaultValue);
    }

    public static Builder builder(String nameKey, List<Integer> defaultValue) {
        return new Builder(nameKey, defaultValue);
    }

    public static class Builder extends BaseEntry.Builder<List<Integer>, ListIntegerEntry, Builder> {
        public Builder(Component name, List<Integer> defaultValue) {
            super(name, defaultValue);
        }

        public Builder(String nameKey, List<Integer> defaultValue) {
            super(nameKey, defaultValue);
        }

        public Builder(ListIntegerEntry parent) {
            super(parent);
        }

        @Override
        public Builder self() {
            return this;
        }

        @Override
        protected ListIntegerEntry buildInternal() {
            return new ListIntegerEntry(this);
        }
    }
}
