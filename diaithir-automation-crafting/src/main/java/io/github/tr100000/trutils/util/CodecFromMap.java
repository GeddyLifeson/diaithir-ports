package io.github.tr100000.trutils.util;

import com.google.common.collect.BiMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import java.util.function.Function;

public record CodecFromMap<K, V>(BiMap<K, V> map, Codec<K> keyCodec, Function<K, String> errorSupplier) implements Codec<V> {
    @Override
    public <T> DataResult<Pair<V, T>> decode(DynamicOps<T> ops, T input) {
        return keyCodec.decode(ops, input).flatMap(p -> map.containsKey(p.getFirst()) ? DataResult.success(Pair.of(map.get(p.getFirst()), p.getSecond())) : DataResult.error(() -> errorSupplier.apply(p.getFirst())));
    }

    @Override
    public <T> DataResult<T> encode(V input, DynamicOps<T> ops, T prefix) {
        return keyCodec.encode(map.inverse().get(input), ops, prefix);
    }
}
