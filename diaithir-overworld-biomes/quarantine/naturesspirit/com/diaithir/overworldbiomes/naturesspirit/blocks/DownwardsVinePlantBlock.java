package com.diaithir.overworldbiomes.naturesspirit.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GrowingPlantBlock;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DownwardsVinePlantBlock extends net.minecraft.world.level.block.GrowingPlantBodyBlock {

  public static final VoxelShape SHAPE = Block.box(1D, 0D, 1D, 15D, 16D, 15D);
  public Block headBlock;

  public DownwardsVinePlantBlock(BlockBehaviour.Properties properties, Block headBlock) {
    super(properties, Direction.DOWN, SHAPE, false);
    this.headBlock = headBlock;
  }

  @Override
  public String getDescriptionId() {
    return headBlock.getDescriptionId();
  }

  @Override
  protected GrowingPlantHeadBlock getHeadBlock() {
    return (GrowingPlantHeadBlock) headBlock;
  }

  @Override
  public boolean canSurvive(BlockState state, LevelReader levelReader, BlockPos pos) {
    BlockPos blockPos = pos.relative(this.growthDirection.getOpposite());
    BlockState blockState = levelReader.getBlockState(blockPos);
    if (!this.canAttachTo(blockState)) {
      return false;
    } else {
      return blockState.is(this.getBodyBlock()) || blockState.is(this.getHeadBlock()) || blockState.isFaceSturdy(levelReader,
          blockPos,
          this.growthDirection
      ) || blockState.is(BlockTags.LEAVES);
    }
  }

  @Override
  protected MapCodec<? extends net.minecraft.world.level.block.GrowingPlantBodyBlock> codec() {
    return null;
  }
}
