package net.diaithir.compat;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.TintedParticleLeavesBlock;

public class WildLeavesBlock extends TintedParticleLeavesBlock {
    public static final MapCodec<WildLeavesBlock> CODEC = simpleCodec(WildLeavesBlock::new);

    public WildLeavesBlock(Properties properties) {
        super(0.01F, properties);
    }

    @Override
    public MapCodec<? extends TintedParticleLeavesBlock> codec() {
        return CODEC;
    }
}
