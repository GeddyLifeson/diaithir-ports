package net.diaithir.diaithirtrees;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class SimpleLeaves extends LeavesBlock {
    public static final MapCodec<SimpleLeaves> CODEC = simpleCodec(SimpleLeaves::new);

    public SimpleLeaves(BlockBehaviour.Properties props) {
        super(0.01F, props);
    }

    @Override
    protected void spawnFallingLeavesParticle(Level level, BlockPos pos, RandomSource random) {
    }

    @Override
    public MapCodec<? extends LeavesBlock> codec() {
        return CODEC;
    }
}
