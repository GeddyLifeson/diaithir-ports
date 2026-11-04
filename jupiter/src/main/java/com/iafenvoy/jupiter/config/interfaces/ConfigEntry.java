package com.iafenvoy.jupiter.config.interfaces;

import com.iafenvoy.jupiter.Jupiter;
import com.iafenvoy.jupiter.config.ConfigDataFixer;
import com.iafenvoy.jupiter.config.type.ConfigType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface ConfigEntry<T> {
    ConfigType<T> getType();

    @Nullable
    String getKey();

    Component getName();

    @Nullable
    Component getTooltip();

    ConfigEntry<T> newInstance();

    void registerCallback(ValueChangeCallback<T> callback);

    T getValue();

    T getDefaultValue();

    void setValue(T value);

    Codec<T> getCodec();

    default <R> DataResult<R> encode(ConfigDataFixer dataFixer, DynamicOps<R> ops) {
        return this.getCodec().encodeStart(ops, this.getValue());
    }

    default <R> void decode(ConfigDataFixer dataFixer, DynamicOps<R> ops, R input) {
        this.getCodec().parse(ops, input).resultOrPartial(Jupiter.LOGGER::error).ifPresent(this::setValue);
    }

    void reset();

    default boolean canReset() {
        return !Objects.equals(this.getValue(), this.getDefaultValue());
    }
}
