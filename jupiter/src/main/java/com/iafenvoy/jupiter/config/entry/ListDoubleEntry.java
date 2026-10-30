package com.iafenvoy.jupiter.config.entry;

import com.iafenvoy.jupiter.config.interfaces.ConfigEntry;
import com.iafenvoy.jupiter.config.type.ConfigType;
import com.iafenvoy.jupiter.config.type.ConfigTypes;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ListDoubleEntry extends ListBaseEntry<Double> {
    protected ListDoubleEntry(Builder builder) {
        super(builder);
    }

    @Override
    public Codec<Double> getValueCodec() {
        return Codec.DOUBLE;
    }

    @Override
    public ConfigEntry<Double> newSingleInstance(Double value, int index, Runnable reload) {
        return DoubleEntry.builder(this.name, value).callback((v, r, d) -> {
            if (r) {
                this.getValue().remove(index);
                reload.run();
            } else this.getValue().set(index, v);
            this.setValue(this.getValue());
        }).build();
    }

    @Override
    public Double newValue() {
        return (double) 0;
    }

    @Override
    public ConfigType<List<Double>> getType() {
        return ConfigTypes.LIST_DOUBLE;
    }

    @Override
    public ConfigEntry<List<Double>> newInstance() {
        return new Builder(this).build();
    }

    public static Builder builder(Component name, List<Double> defaultValue) {
        return new Builder(name, defaultValue);
    }

    public static Builder builder(String nameKey, List<Double> defaultValue) {
        return new Builder(nameKey, defaultValue);
    }

    public static class Builder extends BaseEntry.Builder<List<Double>, ListDoubleEntry, Builder> {
        public Builder(Component name, List<Double> defaultValue) {
            super(name, defaultValue);
        }

        public Builder(String nameKey, List<Double> defaultValue) {
            super(nameKey, defaultValue);
        }

        public Builder(ListDoubleEntry parent) {
            super(parent);
        }

        @Override
        public Builder self() {
            return this;
        }

        @Override
        protected ListDoubleEntry buildInternal() {
            return new ListDoubleEntry(this);
        }
    }
}
