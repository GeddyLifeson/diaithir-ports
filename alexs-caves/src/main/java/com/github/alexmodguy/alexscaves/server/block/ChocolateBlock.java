package com.github.alexmodguy.alexscaves.server.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class ChocolateBlock extends RotatedPillarBlock {
    public ChocolateBlock(Properties properties) {
        super(properties);
    }

    public BlockState updateShape(BlockState blockState, net.minecraft.world.level.LevelReader levelAccessor, net.minecraft.world.level.ScheduledTickAccess ticks, BlockPos blockPos, Direction direction, BlockPos blockPos1, BlockState blockState1, net.minecraft.util.RandomSource randomSource) {
        if(direction == Direction.UP && levelAccessor.getBlockState(blockPos.above()).is(ACBlockRegistry.BLOCK_OF_FROSTING.get()) && blockState.getValue(AXIS) == Direction.Axis.Y){
            return  ACBlockRegistry.BLOCK_OF_FROSTED_CHOCOLATE.get().defaultBlockState();
        }
        return super.updateShape(blockState, levelAccessor, ticks, blockPos, direction, blockPos1, blockState1, randomSource);
    }

}
