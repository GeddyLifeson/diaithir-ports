package com.diaithir.overworldbiomes.naturesspirit.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.VegetationBlock;

public class BearberryBlock extends VegetationBlock implements BonemealableBlock {

  public static final MapCodec<BearberryBlock> CODEC = simpleCodec(BearberryBlock::new);

  public BearberryBlock(BlockBehaviour.Properties properties) {
    super(properties);
  }

  @Override
  protected MapCodec<? extends VegetationBlock> codec() {
    return CODEC;
  }

  @Override
  public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state) {
    return false;
  }

  @Override
  public boolean isBonemealSuccess(Level world, RandomSource random, BlockPos pos, BlockState state) {
    return false;
  }

  @Override
  public void performBonemeal(net.minecraft.server.level.ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
  }
}
