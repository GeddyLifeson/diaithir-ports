package com.diaithir.overworldbiomes.naturesspirit.blocks;

import com.diaithir.overworldbiomes.naturesspirit.datagen.NSConfiguredFeatures;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class RedMossBlock extends Block implements BonemealableBlock {

  public static final MapCodec<RedMossBlock> CODEC = simpleCodec(RedMossBlock::new);

  public RedMossBlock(Properties settings) {
    super(settings);
  }

  @Override
  protected MapCodec<? extends Block> codec() {
    return CODEC;
  }

  @Override
  public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state) {
    return world.getBlockState(pos.above()).isAir();
  }

  @Override
  public boolean isBonemealSuccess(net.minecraft.world.level.Level world, RandomSource random, BlockPos pos, BlockState state) {
    return true;
  }

  @Override
  public void performBonemeal(ServerLevel world, RandomSource random, BlockPos pos, BlockState state) {
    world.registryAccess().lookup(Registries.CONFIGURED_FEATURE).flatMap((registry) -> registry.get(NSConfiguredFeatures.RED_MOSS_PATCH_BONEMEAL))
        .ifPresent((reference) -> {
          reference.value().place(world, world.getChunkSource().getGenerator(), random, pos.above());
        });
  }
}
