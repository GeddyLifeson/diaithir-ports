package com.iafenvoy.jupiter.config.entry;

import com.iafenvoy.jupiter.config.interfaces.ConfigEntry;
import com.iafenvoy.jupiter.config.type.ConfigType;
import com.iafenvoy.jupiter.config.type.ConfigTypes;
import com.iafenvoy.jupiter.util.EnumHelper;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class EnumEntry<T extends Enum<T>> extends BaseEntry<T> {
    @Nullable
    protected final Function<T, Component> nameProvider;

    protected EnumEntry(Builder<T> builder) {
        super(builder);
        this.nameProvider = builder.nameProvider;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ConfigType<T> getType() {
        return (ConfigType<T>) ConfigTypes.ENUM;
    }

    @Override
    public ConfigEntry<T> newInstance() {
        return new Builder<>(this).build();
    }

    @Override
    public Codec<T> getCodec() {
        return EnumHelper.getCodec(this.value);
    }

    public static <T extends Enum<T>> Builder<T> builder(Component name, T defaultValue) {
        return new Builder<>(name, defaultValue);
    }

    public static <T extends Enum<T>> Builder<T> builder(String nameKey, T defaultValue) {
        return new Builder<>(nameKey, defaultValue);
    }

    public static class Builder<T extends Enum<T>> extends BaseEntry.Builder<T, EnumEntry<T>, Builder<T>> {
        protected Function<T, Component> nameProvider;

        public Builder(Component name, T defaultValue) {
            super(name, defaultValue);
        }

        public Builder(String nameKey, T defaultValue) {
            super(nameKey, defaultValue);
        }

        public Builder(EnumEntry<T> parent) {
            super(parent);
        }

        public Builder<T> nameProvider(Function<T, Component> nameProvider) {
            this.nameProvider = nameProvider;
            return this;
        }

        @Override
        public Builder<T> self() {
            return this;
        }

        @Override
        protected EnumEntry<T> buildInternal() {
            return new EnumEntry<>(this);
        }
    }
}
