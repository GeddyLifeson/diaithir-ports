package com.diaithir.overworldbiomes.naturesspirit.blocks;


import com.diaithir.overworldbiomes.naturesspirit.registration.NSTags;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class TallLargeDesertFernBlock extends DoublePlantBlock {

  public static final MapCodec<TallLargeDesertFernBlock> CODEC = simpleCodec(TallLargeDesertFernBlock::new);

  public TallLargeDesertFernBlock(BlockBehaviour.Properties properties) {
    super(properties);
  }

  @Override
  public MapCodec<? extends DoublePlantBlock> codec() {
    return CODEC;
  }

  @Override
  protected boolean mayPlaceOn(BlockState floor, BlockGetter world, BlockPos pos) {
    return floor.is(NSTags.Blocks.TURNIP_STEM_GROWS_ON) || floor.is(Blocks.FARMLAND);
  }
}
