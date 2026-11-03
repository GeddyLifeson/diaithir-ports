package com.diaithir.overworldbiomes.naturesspirit.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class SandySaplingBlock extends SaplingBlock {

  public static final MapCodec<SandySaplingBlock> CODEC = simpleCodec(
      properties -> new SandySaplingBlock(TreeGrower.OAK, properties));

  public SandySaplingBlock(TreeGrower generator, BlockBehaviour.Properties settings) {
    super(generator, settings);
  }

  @Override
  public MapCodec<? extends SaplingBlock> codec() {
    return CODEC;
  }

  @Override
  protected boolean mayPlaceOn(BlockState floor, BlockGetter world, BlockPos pos) {
    return floor.is(BlockTags.DIRT) || floor.is(Blocks.FARMLAND) || floor.is(BlockTags.SAND);
  }
}
