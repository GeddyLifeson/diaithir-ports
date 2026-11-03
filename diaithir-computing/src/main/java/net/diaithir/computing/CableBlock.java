package net.diaithir.computing;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Joins computers into one wired network: a message sent by any computer touching a connected run of cable reaches every listening computer touching it, at any distance. */
public final class CableBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(4, 4, 4, 12, 12, 12);

    public CableBlock(Properties props) { super(props); }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) { return SHAPE; }
}
