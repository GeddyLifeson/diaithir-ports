package net.diaithir.brewing;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** A canning pot only cooks while something hot sits directly under it: a lit campfire, furnace, smoker or blast furnace, fire, lava or a magma block. */
final class Heat {
    private static final int FULL = 4; // quarter-ticks per game tick, the same as a normal cellar

    static boolean hot(Level level, BlockPos pos) {
        BlockState s = level.getBlockState(pos.below());
        return s.is(BlockTags.FIRE) || s.is(Blocks.MAGMA_BLOCK) || s.getFluidState().is(FluidTags.LAVA)
            || s.hasProperty(BlockStateProperties.LIT) && s.getValue(BlockStateProperties.LIT);
    }

    static int rate(Level level, BlockPos pos) {
        return hot(level, pos) ? FULL : 0;
    }

    private Heat() {
    }
}
