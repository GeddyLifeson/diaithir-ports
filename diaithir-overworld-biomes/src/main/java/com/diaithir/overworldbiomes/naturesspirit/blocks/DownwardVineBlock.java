package com.diaithir.overworldbiomes.naturesspirit.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;


public class DownwardVineBlock extends GrowingPlantHeadBlock {

  protected static final VoxelShape SHAPE = Block.box(4D, 9D, 4D, 12D, 16D, 12D);

  private final Supplier<Block> vinesPlantBlock;

  public DownwardVineBlock(Properties properties, Supplier<Block> vinesPlantBlock) {
    super(properties, Direction.DOWN, SHAPE, false, 0.1D);
    this.vinesPlantBlock = vinesPlantBlock;
  }

  @Override
  protected MapCodec<? extends GrowingPlantHeadBlock> codec() {
    return simpleCodec(properties -> new DownwardVineBlock(properties, vinesPlantBlock));
  }

  @Override
  protected int getBlocksToGrowWhenBonemealed(RandomSource randomSource) {
    return net.minecraft.world.level.block.NetherVines.getBlocksToGrowWhenBonemealed(randomSource);
  }

  @Override
  protected Block getBodyBlock() {
    return vinesPlantBlock.get();
  }

  @Override
  protected boolean canGrowInto(BlockState state) {
    return state.isAir();
  }

  @Override
  public boolean canSurvive(BlockState state, LevelReader levelReader, BlockPos pos) {
    BlockPos blockPos = pos.relative(this.growthDirection.getOpposite());
    BlockState blockState = levelReader.getBlockState(blockPos);
    if (!this.canAttachTo(blockState)) {
      return false;
    } else {
      return blockState.is(this.getHeadBlock()) || blockState.is(this.getBodyBlock()) || blockState.isFaceSturdy(levelReader,
          blockPos,
          this.growthDirection
      ) || blockState.is(BlockTags.LEAVES);
    }
  }
}
