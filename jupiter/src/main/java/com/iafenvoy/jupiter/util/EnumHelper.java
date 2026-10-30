package com.iafenvoy.jupiter.util;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;

public final class EnumHelper {
    public static <T extends Enum<T>> Codec<T> getCodec(T value) {
        return getCodec(value.getDeclaringClass());
    }

    public static <T extends Enum<T>> Codec<T> getCodec(Class<T> value) {
        return Codec.STRING.xmap(x -> Enum.valueOf(value, x), Enum::name);
    }

    public static <T extends Enum<?>> Enum<?> cycle(T value, boolean clockwise) {
        Enum<?>[] types = value.getDeclaringClass().getEnumConstants();
        return types[(value.ordinal() + (clockwise ? 1 : -1)) % types.length];
    }

    public static <T extends Enum<?>> Component getDisplayText(T value) {
        return Component.translatable(value.name(), new Object[]{});
    }
}
