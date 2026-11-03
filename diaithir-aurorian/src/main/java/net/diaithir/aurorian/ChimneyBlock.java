package net.diaithir.aurorian;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ChimneyBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 16, 12);

    public ChimneyBlock(Properties p) {
        super(p);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return simpleCodec(ChimneyBlock::new);
    }

    @Override
    protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) {
        return SHAPE;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState s) {
        return true;
    }
}
