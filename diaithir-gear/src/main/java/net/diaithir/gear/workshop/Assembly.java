package net.diaithir.gear.workshop;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** Item component diaithirgear:assembly: which blueprint, the material in each slot, how often it was used, and its improvements. */
public record Assembly(String blueprint, Map<String, String> parts, int uses, List<String> improvements) {
    public static final Codec<Assembly> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("blueprint").forGetter(Assembly::blueprint),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("parts").forGetter(Assembly::parts),
            Codec.INT.optionalFieldOf("uses", 0).forGetter(Assembly::uses),
            Codec.STRING.listOf().optionalFieldOf("improvements", List.of()).forGetter(Assembly::improvements)).apply(i, Assembly::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, Assembly> STREAM = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public Assembly withUses(int n) { return new Assembly(blueprint, parts, n, improvements); }

    public int hone() { return Craft.honeLevel(uses); }
}
