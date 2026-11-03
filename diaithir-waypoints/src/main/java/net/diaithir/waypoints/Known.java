package net.diaithir.waypoints;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

/** One lit waypoint as the player remembers it: where, and the name given by its position. */
public record Known(Identifier dimension, BlockPos pos, String name) {
    public static final Codec<Known> CODEC = RecordCodecBuilder.create(i -> i.group(
        Identifier.CODEC.fieldOf("dim").forGetter(Known::dimension),
        BlockPos.CODEC.fieldOf("pos").forGetter(Known::pos),
        Codec.STRING.fieldOf("name").forGetter(Known::name)).apply(i, Known::new));
    public static final Codec<List<Known>> LIST = CODEC.listOf();
}
