package com.diaithir.overworldbiomes.naturesspirit.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LargeSproutsBlock extends TallGrassBlock {

  public static final MapCodec<LargeSproutsBlock> CODEC = simpleCodec(LargeSproutsBlock::new);

  protected static final VoxelShape SHAPE = Block.box(2D, 0D, 2D, 14D, 4D, 14D);

  public LargeSproutsBlock(BlockBehaviour.Properties properties) {
    super(properties);
  }

  @Override
  @SuppressWarnings({"unchecked","rawtypes"})
  public MapCodec<TallGrassBlock> codec() {
    return (MapCodec) CODEC;
  }

  @Override
  public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state) {
    return false;
  }

  @Override
  public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
    return SHAPE;
  }

  @Override
  public boolean isBonemealSuccess(Level world, RandomSource random, BlockPos pos, BlockState state) {
    return false;
  }
}
