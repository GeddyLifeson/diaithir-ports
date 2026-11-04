package io.github.tr100000.trutils.api.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.tr100000.trutils.api.utils.CodecUtils;
import io.github.tr100000.trutils.api.utils.PacketCodecUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;

public interface Icon {
    Icon BLANK = BlankIcon.INSTANCE;
    Icon ERROR = new ItemIcon(Items.BARRIER);

    Codec<Icon> CODEC = CodecUtils.ofMap(IconTypes.REGISTRY, Codec.STRING, id -> "Invalid id " + id)
            .dispatch(Icon::getType, IconType::codec);

    @SuppressWarnings("unchecked")
    StreamCodec<FriendlyByteBuf, Icon> PACKET_CODEC = PacketCodecUtils.<FriendlyByteBuf, String, IconType<? extends Icon>>ofMap(IconTypes.REGISTRY, ByteBufCodecs.STRING_UTF8)
            .dispatch(Icon::getType, type -> (StreamCodec<@NonNull FriendlyByteBuf, ? extends @NonNull Icon>)type.packetCodec());

    IconType<?> getType();

    record IconType<T extends Icon>(String id, MapCodec<T> codec, StreamCodec<? extends FriendlyByteBuf, T> packetCodec) {}
}
