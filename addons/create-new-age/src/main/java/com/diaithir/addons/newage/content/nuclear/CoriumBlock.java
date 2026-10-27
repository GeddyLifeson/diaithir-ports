package com.diaithir.addons.newage.content.nuclear;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import com.diaithir.addons.newage.CNABlocks;
import com.diaithir.addons.newage.content.heat.HeatBlockEntity;

public class CoriumBlock extends FallingBlock {
    public static final MapCodec<CoriumBlock> CODEC = simpleCodec(CoriumBlock::new);

    public CoriumBlock(Properties properties) {
        super(properties.randomTicks());
    }

    @Override
    protected MapCodec<? extends FallingBlock> codec() {
        return CODEC;
    }

    @Override
    public int getDustColor(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos) {
        return 0xFF6A1A;
    }

    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        NuclearUtil.createRadiation(20, level, pos);
        for (Direction dir : Direction.values()) {
            var be = level.getBlockEntity(pos.relative(dir));
            if (be instanceof HeatBlockEntity heat) {
                heat.addHeat(random.nextFloat() * 3000f);
                level.sendBlockUpdated(pos.relative(dir), be.getBlockState(), be.getBlockState(), Block.UPDATE_CLIENTS);
            }
        }
        if (random.nextFloat() < 0.05) {
            level.setBlock(pos, CNABlocks.SOLID_CORIUM.get().defaultBlockState(), 3);
            return;
        }
        if (level.getBlockState(pos.relative(Direction.DOWN)).getBlock().getExplosionResistance() < random.nextInt(2, 12)) {
            level.setBlock(pos.relative(Direction.DOWN), Blocks.AIR.defaultBlockState(), 3);
        }
    }
}
