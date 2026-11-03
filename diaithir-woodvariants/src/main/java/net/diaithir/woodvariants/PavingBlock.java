package net.diaithir.woodvariants;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A paving: a full block that stands one sixteenth low, like a trodden path. */
public final class PavingBlock extends Block {
    public static final MapCodec<PavingBlock> CODEC = simpleCodec(PavingBlock::new);
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 15, 16);

    public PavingBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
