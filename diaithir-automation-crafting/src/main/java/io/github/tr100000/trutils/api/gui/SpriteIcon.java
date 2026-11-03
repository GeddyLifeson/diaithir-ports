package io.github.tr100000.trutils.api.gui;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record SpriteIcon(Identifier location) implements Icon {
    public static final MapCodec<SpriteIcon> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Identifier.CODEC.fieldOf("texture").forGetter(SpriteIcon::location)
            ).apply(instance, SpriteIcon::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, SpriteIcon> PACKET_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, SpriteIcon::location,
            SpriteIcon::new
    );
    public static final Icon.IconType<SpriteIcon> TYPE = new IconType<>("sprite", CODEC, PACKET_CODEC);

    @Override
    public IconType<?> getType() {
        return TYPE;
    }
}
