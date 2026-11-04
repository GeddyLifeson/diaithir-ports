package io.github.tr100000.trutils.api.utils;

import com.google.common.collect.BiMap;
import com.mojang.serialization.Codec;
import io.github.tr100000.trutils.util.CodecFromMap;
import org.jetbrains.annotations.Contract;

import java.util.function.Function;

public final class CodecUtils {
    private CodecUtils() {}

    @Contract(value = "_, _, _ -> new", pure = true)
    public static <K, V> Codec<V> ofMap(BiMap<K, V> map, Codec<K> keyCodec, Function<K, String> errorMessage) {
        return new CodecFromMap<>(map, keyCodec, errorMessage);
    }
}
