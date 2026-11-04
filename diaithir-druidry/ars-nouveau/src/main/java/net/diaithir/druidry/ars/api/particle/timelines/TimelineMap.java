package net.diaithir.druidry.ars.api.particle.timelines;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/** Port stub: particle timelines are not ported; this is an empty, always-equal placeholder that serializes to nothing. */
public class TimelineMap {
    public static final Codec<TimelineMap> CODEC = com.mojang.serialization.MapCodec.unit(new TimelineMap()).codec();
    public static final StreamCodec<RegistryFriendlyByteBuf, TimelineMap> STREAM = StreamCodec.unit(new TimelineMap());

    @Override
    public boolean equals(Object o) {
        return o instanceof TimelineMap;
    }

    @Override
    public int hashCode() {
        return 1;
    }
}
