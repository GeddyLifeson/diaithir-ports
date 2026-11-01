package com.iafenvoy.jupiter.config.entry;

import com.iafenvoy.jupiter.config.interfaces.ConfigEntry;
import com.iafenvoy.jupiter.config.interfaces.RangeConfigEntry;
import com.iafenvoy.jupiter.config.type.ConfigType;
import com.iafenvoy.jupiter.config.type.ConfigTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.network.chat.Component;

import java.util.function.Function;

public class LongEntry extends BaseEntry<Long> implements RangeConfigEntry<Long> {
    private final long minValue, maxValue;
    private final boolean useSlider = false;

    protected LongEntry(Builder builder) {
        super(builder);
        this.minValue = builder.minValue;
        this.maxValue = builder.maxValue;
    }

    @Override
    public ConfigType<Long> getType() {
        return ConfigTypes.LONG;
    }

    @Override
    public ConfigEntry<Long> newInstance() {
        return new Builder(this).build();
    }

    @Override
    public Codec<Long> getCodec() {
        final Function<Long, DataResult<Long>> checker = Codec.checkRange(this.minValue, this.maxValue);
        return Codec.LONG.flatXmap(checker, checker);
    }

    @Override
    public Long getMinValue() {
        return this.minValue;
    }

    @Override
    public Long getMaxValue() {
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
        long d = Long.parseLong(s);
        if (d < this.minValue || d > this.maxValue) throw new IllegalArgumentException();
        this.setValue(d);
    }

    public static Builder builder(Component name, long defaultValue) {
        return new Builder(name, defaultValue);
    }

    public static Builder builder(String nameKey, long defaultValue) {
        return new Builder(nameKey, defaultValue);
    }

    public static class Builder extends BaseEntry.Builder<Long, LongEntry, Builder> {
        protected long minValue = Long.MIN_VALUE, maxValue = Long.MAX_VALUE;

        public Builder(Component name, long defaultValue) {
            super(name, defaultValue);
        }

        public Builder(String nameKey, long defaultValue) {
            super(nameKey, defaultValue);
        }

        public Builder(LongEntry parent) {
            super(parent);
            this.minValue = parent.minValue;
            this.maxValue = parent.maxValue;
        }

        public Builder min(long minValue) {
            this.minValue = minValue;
            return this;
        }

        public Builder max(long maxValue) {
            this.maxValue = maxValue;
            return this;
        }

        public Builder range(long min, long max) {
            this.min(min);
            this.max(max);
            return this;
        }

        @Override
        public Builder self() {
            return this;
        }

        @Override
        protected LongEntry buildInternal() {
            return new LongEntry(this);
        }
    }
}
