package net.diaithir.brewing;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** Cellar conditions around a vessel, one point each: dark (block light 4 or less), cool (no flame, lava or lit furnace within 2), sheltered (no open sky above). */
final class Cellar {
    static final int BEST = 3;
    private static final int[] RATE = {2, 3, 4, 6}; // quarter-ticks of fermenting per game tick, by score

    static int score(Level level, BlockPos pos) {
        return (dark(level, pos) ? 1 : 0) + (cool(level, pos) ? 1 : 0) + (sheltered(level, pos) ? 1 : 0);
    }

    static boolean dark(Level level, BlockPos pos) {
        return level.getBrightness(LightLayer.BLOCK, pos) <= 4;
    }

    static boolean cool(Level level, BlockPos pos) {
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-2, -2, -2), pos.offset(2, 2, 2))) {
            BlockState s = level.getBlockState(p);
            if (s.is(BlockTags.FIRE) || s.getFluidState().is(FluidTags.LAVA) || s.hasProperty(BlockStateProperties.LIT) && s.getValue(BlockStateProperties.LIT)) {
                return false;
            }
        }
        return true;
    }

    static boolean sheltered(Level level, BlockPos pos) {
        return !level.canSeeSky(pos.above());
    }

    static int rate(int score) {
        return RATE[Math.max(0, Math.min(BEST, score))];
    }

    /** A recipe with a spoiled item can sour in a poor cellar (score 0) with its spoil chance. */
    static boolean spoils(BrewRecipe r, int score, RandomSource random) {
        return score == 0 && r.spoiled() != net.minecraft.world.item.Items.AIR && random.nextFloat() < r.spoilChance();
    }

    private Cellar() {
    }
}
