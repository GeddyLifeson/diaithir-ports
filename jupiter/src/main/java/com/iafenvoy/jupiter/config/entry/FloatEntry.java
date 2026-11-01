package com.iafenvoy.jupiter.config.entry;

import com.iafenvoy.jupiter.config.interfaces.ConfigEntry;
import com.iafenvoy.jupiter.config.interfaces.RangeConfigEntry;
import com.iafenvoy.jupiter.config.type.ConfigType;
import com.iafenvoy.jupiter.config.type.ConfigTypes;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;

public class FloatEntry extends BaseEntry<Float> implements RangeConfigEntry<Float> {
    private final float minValue, maxValue;
    private final boolean useSlider = false;

    protected FloatEntry(Builder builder) {
        super(builder);
        this.minValue = builder.minValue;
        this.maxValue = builder.maxValue;
    }

    @Override
    public ConfigType<Float> getType() {
        return ConfigTypes.FLOAT;
    }

    @Override
    public ConfigEntry<Float> newInstance() {
        return new Builder(this).build();
    }

    @Override
    public Codec<Float> getCodec() {
        return Codec.floatRange(this.minValue, this.maxValue);
    }

    @Override
    public Float getMinValue() {
        return this.minValue;
    }

    @Override
    public Float getMaxValue() {
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
        float d = Float.parseFloat(s);
        if (d < this.minValue || d > this.maxValue) throw new IllegalArgumentException();
        this.setValue(d);
    }

    public static Builder builder(Component name, float defaultValue) {
        return new Builder(name, defaultValue);
    }

    public static Builder builder(String nameKey, float defaultValue) {
        return new Builder(nameKey, defaultValue);
    }

    public static class Builder extends BaseEntry.Builder<Float, FloatEntry, Builder> {
        protected float minValue = Float.MIN_VALUE, maxValue = Float.MAX_VALUE;

        public Builder(Component name, float defaultValue) {
            super(name, defaultValue);
        }

        public Builder(String nameKey, float defaultValue) {
            super(nameKey, defaultValue);
        }

        public Builder(FloatEntry parent) {
            super(parent);
            this.minValue = parent.minValue;
            this.maxValue = parent.maxValue;
        }

        public Builder min(float minValue) {
            this.minValue = minValue;
            return this;
        }

        public Builder max(float maxValue) {
            this.maxValue = maxValue;
            return this;
        }

        public Builder range(float min, float max) {
            this.min(min);
            this.max(max);
            return this;
        }

        @Override
        public Builder self() {
            return this;
        }

        @Override
        protected FloatEntry buildInternal() {
            return new FloatEntry(this);
        }
    }
}
