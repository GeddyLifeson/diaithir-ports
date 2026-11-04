package immersive_melodies.resources;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class MelodyDescriptor {
    private final String name;

    public MelodyDescriptor(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static StreamCodec<FriendlyByteBuf, MelodyDescriptor> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, MelodyDescriptor::getName,
            MelodyDescriptor::new
    );
}