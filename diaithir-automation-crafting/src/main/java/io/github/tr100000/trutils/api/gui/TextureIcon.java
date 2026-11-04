package io.github.tr100000.trutils.api.gui;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;

public record TextureIcon(Identifier texture, int textureSize) implements Icon {
    public static final MapCodec<TextureIcon> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Identifier.CODEC.fieldOf("texture").forGetter(TextureIcon::texture),
                    ExtraCodecs.POSITIVE_INT.optionalFieldOf("textureSize", 256).forGetter(TextureIcon::textureSize)
            ).apply(instance, TextureIcon::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, TextureIcon> PACKET_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, TextureIcon::texture,
            ByteBufCodecs.VAR_INT, TextureIcon::textureSize,
            TextureIcon::new
    );
    public static final IconType<TextureIcon> TYPE = new IconType<>("texture", CODEC, PACKET_CODEC);

    public TextureIcon(Identifier texture) {
        this(texture, 256);
    }

    @Override
    public IconType<TextureIcon> getType() {
        return TYPE;
    }
}
