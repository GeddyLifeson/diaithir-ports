package net.diaithir.diaithirfarmkitchen;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class SprinklerBlock extends Block {
    private final int radius;

    public SprinklerBlock(BlockBehaviour.Properties props, int radius) {
        super(props);
        this.radius = radius;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        if (!level.isClientSide()) level.scheduleTick(pos, this, 40);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-radius, -1, -radius), pos.offset(radius, 1, radius))) {
            BlockState s = level.getBlockState(p);
            if (s.getBlock() instanceof CropBlock crop && !crop.isMaxAge(s) && random.nextInt(3) == 0) {
                level.setBlock(p.immutable(), crop.getStateForAge(crop.getAge(s) + 1), 2);
            }
        }
        level.scheduleTick(pos, this, 40);
    }
}
