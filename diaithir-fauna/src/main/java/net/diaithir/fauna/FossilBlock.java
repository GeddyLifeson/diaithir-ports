package net.diaithir.fauna;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Fossil-bearing rock. Its loot table is empty; the fragment is made when it is broken with a tool that can mine it. */
public class FossilBlock extends Block {
    public final String rock;

    public FossilBlock(Properties props, String rock) {
        super(props);
        this.rock = rock;
    }

    @Override
    protected void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack tool, boolean dropExperience) {
        super.spawnAfterBreak(state, level, pos, tool, dropExperience);
        if (dropExperience && tool.isCorrectToolForDrops(state)) {
            popResource(level, pos, Fossils.stack(Fossils.FRAGMENT.get(), Fossils.pick(level, pos, rock, level.getRandom())));
        }
    }
}
