package com.diaithir.overworldbiomes.naturesspirit.blocks;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class NSFernBlock extends TallGrassBlock {

  public static final MapCodec<NSFernBlock> CODEC = RecordCodecBuilder.mapCodec(
      instance -> instance.group(
          propertiesCodec(),
          BuiltInRegistries.BLOCK.byNameCodec().fieldOf("tall_plant_block").forGetter(block -> block.tallPlantBlock)
      ).apply(instance, (properties, tall) -> new NSFernBlock(properties, (DoublePlantBlock) tall))
  );

  DoublePlantBlock tallPlantBlock;

  public NSFernBlock(BlockBehaviour.Properties properties, DoublePlantBlock tallPlantBlock) {
    super(properties);
    this.tallPlantBlock = tallPlantBlock;
  }

  @Override
  @SuppressWarnings({"unchecked","rawtypes"})
  public MapCodec<TallGrassBlock> codec() {
    return (MapCodec) CODEC;
  }

  @Override
  public void performBonemeal(ServerLevel world, RandomSource random, BlockPos pos, BlockState state) {
    if (tallPlantBlock.defaultBlockState().canSurvive(world, pos) && world.isEmptyBlock(pos.above())) {
      DoublePlantBlock.placeAt(world, tallPlantBlock.defaultBlockState(), pos, Block.UPDATE_CLIENTS);
    }

  }
}
