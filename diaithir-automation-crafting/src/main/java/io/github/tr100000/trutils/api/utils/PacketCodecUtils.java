package io.github.tr100000.trutils.api.utils;

import com.google.common.collect.BiMap;
import com.mojang.datafixers.util.Function7;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Contract;

import java.util.function.Function;

public final class PacketCodecUtils {
    private PacketCodecUtils() {}

    @SuppressWarnings("unchecked")
    @Contract(pure = true)
    public static <B extends ByteBuf, K, V> StreamCodec<B, V> ofMap(BiMap<K, V> map, StreamCodec<? super B, K> keyCodec) {
        return (StreamCodec<B, V>)keyCodec.map(map::get, map.inverse()::get);
    }

    @Contract(pure = true)
    public static <B extends FriendlyByteBuf, T extends Enum<T>> StreamCodec<B,T> ofEnum(Class<T> enumClass) {
        return StreamCodec.of(FriendlyByteBuf::writeEnum, buf -> buf.readEnum(enumClass));
    }

    @Contract(pure = true)
    public static <B, C, T1, T2, T3, T4, T5, T6, T7> StreamCodec<B, C> bigTuple(
            StreamCodec<? super B, T1> codec1,
            Function<C, T1> from1,
            StreamCodec<? super B, T2> codec2,
            Function<C, T2> from2,
            StreamCodec<? super B, T3> codec3,
            Function<C, T3> from3,
            StreamCodec<? super B, T4> codec4,
            Function<C, T4> from4,
            StreamCodec<? super B, T5> codec5,
            Function<C, T5> from5,
            StreamCodec<? super B, T6> codec6,
            Function<C, T6> from6,
            StreamCodec<? super B, T7> codec7,
            Function<C, T7> from7,
            Function7<T1, T2, T3, T4, T5, T6, T7, C> to
    ) {
        return new StreamCodec<>() {
            @Override
            public C decode(B buf) {
                T1 object1 = codec1.decode(buf);
                T2 object2 = codec2.decode(buf);
                T3 object3 = codec3.decode(buf);
                T4 object4 = codec4.decode(buf);
                T5 object5 = codec5.decode(buf);
                T6 object6 = codec6.decode(buf);
                T7 object7 = codec7.decode(buf);
                return to.apply(object1, object2, object3, object4, object5, object6, object7);
            }

            @Override
            public void encode(B buf, C object) {
                codec1.encode(buf, from1.apply(object));
                codec2.encode(buf, from2.apply(object));
                codec3.encode(buf, from3.apply(object));
                codec4.encode(buf, from4.apply(object));
                codec5.encode(buf, from5.apply(object));
                codec6.encode(buf, from6.apply(object));
                codec7.encode(buf, from7.apply(object));
            }
        };
    }
}
