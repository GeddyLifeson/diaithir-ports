package com.iafenvoy.jupiter.config.entry;

import com.iafenvoy.jupiter.config.interfaces.ConfigEntry;
import com.iafenvoy.jupiter.config.interfaces.RangeConfigEntry;
import com.iafenvoy.jupiter.config.type.ConfigType;
import com.iafenvoy.jupiter.config.type.ConfigTypes;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;

public class DoubleEntry extends BaseEntry<Double> implements RangeConfigEntry<Double> {
    private final double minValue, maxValue;
    private final boolean useSlider = false;

    protected DoubleEntry(Builder builder) {
        super(builder);
        this.minValue = builder.minValue;
        this.maxValue = builder.maxValue;
    }

    @Override
    public ConfigType<Double> getType() {
        return ConfigTypes.DOUBLE;
    }

    @Override
    public ConfigEntry<Double> newInstance() {
        return new Builder(this).build();
    }

    @Override
    public Codec<Double> getCodec() {
        return Codec.doubleRange(this.minValue, this.maxValue);
    }

    @Override
    public Double getMinValue() {
        return this.minValue;
    }

    @Override
    public Double getMaxValue() {
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
        double d = Double.parseDouble(s);
        if (d < this.minValue || d > this.maxValue) throw new IllegalArgumentException();
        this.setValue(d);
    }

    public static Builder builder(Component name, double defaultValue) {
        return new Builder(name, defaultValue);
    }

    public static Builder builder(String nameKey, double defaultValue) {
        return new Builder(nameKey, defaultValue);
    }

    public static class Builder extends BaseEntry.Builder<Double, DoubleEntry, Builder> {
        protected double minValue = Double.MIN_VALUE, maxValue = Double.MAX_VALUE;

        public Builder(Component name, double defaultValue) {
            super(name, defaultValue);
        }

        public Builder(String nameKey, double defaultValue) {
            super(nameKey, defaultValue);
        }

        public Builder(DoubleEntry parent) {
            super(parent);
            this.minValue = parent.minValue;
            this.maxValue = parent.maxValue;
        }

        public Builder min(double minValue) {
            this.minValue = minValue;
            return this;
        }

        public Builder max(double maxValue) {
            this.maxValue = maxValue;
            return this;
        }

        public Builder range(double min, double max) {
            this.min(min);
            this.max(max);
            return this;
        }

        @Override
        public Builder self() {
            return this;
        }

        @Override
        protected DoubleEntry buildInternal() {
            return new DoubleEntry(this);
        }
    }
}
