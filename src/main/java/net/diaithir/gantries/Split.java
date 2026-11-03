package net.diaithir.gantries;

import com.zurrtum.create.AllBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Helpers the mixins share: a gantry split counts as a gantry shaft wherever Create asks "is this a gantry shaft". */
public final class Split {
    private Split() {}

    /** The block Create should see: the plain gantry shaft for a split, the block itself otherwise. */
    public static Block asShaft(Block b) {
        return b instanceof GantrySplitBlock ? AllBlocks.GANTRY_SHAFT : b;
    }

    /** {@code state.is(block)} that also answers true for a split when the question is about the gantry shaft. */
    public static boolean isShaft(boolean original, BlockState state, Object asked) {
        return original || (asked == AllBlocks.GANTRY_SHAFT && state.getBlock() instanceof GantrySplitBlock);
    }
}
