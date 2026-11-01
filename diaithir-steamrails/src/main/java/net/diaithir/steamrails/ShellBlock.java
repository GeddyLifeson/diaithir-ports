package net.diaithir.steamrails;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.Block;

/** A plain boiler barrel segment. It adds water volume and sets the pressure rating (the weakest block of a boiler sets the rating of all). */
public final class ShellBlock extends Block {
    public static final MapCodec<ShellBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(propertiesCodec(),
            Codec.INT.fieldOf("rating").forGetter(b -> b.rating)).apply(i, ShellBlock::new));
    public final int rating;

    public ShellBlock(Properties properties, int rating) {
        super(properties);
        this.rating = rating;
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }
}
