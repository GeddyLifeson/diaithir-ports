package com.iafenvoy.jupiter.config.entry;

import com.iafenvoy.jupiter.config.interfaces.ConfigEntry;
import com.iafenvoy.jupiter.config.interfaces.RangeConfigEntry;
import com.iafenvoy.jupiter.config.type.ConfigType;
import com.iafenvoy.jupiter.config.type.ConfigTypes;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;

public class IntegerEntry extends BaseEntry<Integer> implements RangeConfigEntry<Integer> {
    private final int minValue, maxValue;
    private final boolean useSlider = false;

    protected IntegerEntry(Builder builder) {
        super(builder);
        this.minValue = builder.minValue;
        this.maxValue = builder.maxValue;
    }

    @Override
    public ConfigType<Integer> getType() {
        return ConfigTypes.INTEGER;
    }

    @Override
    public ConfigEntry<Integer> newInstance() {
        return new Builder(this).build();
    }

    @Override
    public Codec<Integer> getCodec() {
        return Codec.intRange(this.minValue, this.maxValue);
    }

    @Override
    public Integer getMinValue() {
        return this.minValue;
    }

    @Override
    public Integer getMaxValue() {
        return this.maxValue;
    }

    @Override
    public boolean useSlider() {
        return this.useSlider;
    }

    @Override
    public String valueAsString() {
        return String.valueOf(this.getValue());
    }

    @Override
    public void setValueFromString(String s) {
        int d = Integer.parseInt(s);
        if (d < this.minValue || d > this.maxValue) throw new IllegalArgumentException();
        this.setValue(d);
    }

    public static Builder builder(Component name, int defaultValue) {
        return new Builder(name, defaultValue);
    }

    public static Builder builder(String nameKey, int defaultValue) {
        return new Builder(nameKey, defaultValue);
    }

    public static class Builder extends BaseEntry.Builder<Integer, IntegerEntry, Builder> {
        protected int minValue = Integer.MIN_VALUE, maxValue = Integer.MAX_VALUE;

        public Builder(Component name, int defaultValue) {
            super(name, defaultValue);
        }

        public Builder(String nameKey, int defaultValue) {
            super(nameKey, defaultValue);
        }

        public Builder(IntegerEntry parent) {
            super(parent);
            this.minValue = parent.minValue;
            this.maxValue = parent.maxValue;
        }

        public Builder min(int minValue) {
            this.minValue = minValue;
            return this;
        }

        public Builder max(int maxValue) {
            this.maxValue = maxValue;
            return this;
        }

        public Builder range(int min, int max) {
            this.min(min);
            this.max(max);
            return this;
        }

        @Override
        public Builder self() {
            return this;
        }

        @Override
        protected IntegerEntry buildInternal() {
            return new IntegerEntry(this);
        }
    }
}
