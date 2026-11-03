package org.diaithir.archimedes.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;

/** Floater, buffer, seat and engine have no behaviour of their own; the ship reads them by identity. */
public class PlainBlock extends Block {
    public static final MapCodec<PlainBlock> CODEC = simpleCodec(PlainBlock::new);

    public PlainBlock(Properties props) { super(props); }

    @Override
    protected MapCodec<? extends Block> codec() { return CODEC; }
}
