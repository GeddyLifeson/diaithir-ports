package net.diaithir.druidry.ars.common.crafting.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Lazy serialization that takes any codec and creates a stream codec for it.
 * Not performant, but find for recipe types that are not used often.
 */
public class CheatSerializer {

    public static <T> StreamCodec<RegistryFriendlyByteBuf, T> create(MapCodec<T> codec) {
        return create(codec.codec());
    }

    public static <T> StreamCodec<RegistryFriendlyByteBuf, T> create(Codec<T> codec) {
        return net.minecraft.network.codec.ByteBufCodecs.fromCodecWithRegistries(codec);
    }

}
