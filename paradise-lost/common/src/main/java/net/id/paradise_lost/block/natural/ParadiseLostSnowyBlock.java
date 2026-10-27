package net.id.paradise_lost.block.natural;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.SnowyBlock;

public class ParadiseLostSnowyBlock extends SnowyBlock {
    public static final MapCodec<ParadiseLostSnowyBlock> CODEC = simpleCodec(ParadiseLostSnowyBlock::new);

    public ParadiseLostSnowyBlock(Properties settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends SnowyBlock> codec() {
        return CODEC;
    }
}
