package org.diaithir.archimedes.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;

/** Lift block of airships. One block per dye colour. */
public class BalloonBlock extends Block {
    public static final MapCodec<BalloonBlock> CODEC = simpleCodec(BalloonBlock::new);

    public BalloonBlock(Properties props) { super(props); }

    @Override
    protected MapCodec<? extends Block> codec() { return CODEC; }
}
