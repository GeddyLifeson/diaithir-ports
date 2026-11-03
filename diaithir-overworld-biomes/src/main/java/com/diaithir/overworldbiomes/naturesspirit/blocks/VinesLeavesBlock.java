package com.diaithir.overworldbiomes.naturesspirit.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class VinesLeavesBlock extends net.minecraft.world.level.block.TintedParticleLeavesBlock implements BonemealableBlock {

  public Block vinePlantBlock;
  public Block vineTipBlock;

  public VinesLeavesBlock(Properties properties, Block vinePlantBlockInput, Block vineTipBlockInput) {
    super(0.0F, properties);
    vinePlantBlock = vinePlantBlockInput;
    vineTipBlock = vineTipBlockInput;
  }

  @Override
  public MapCodec<? extends net.minecraft.world.level.block.TintedParticleLeavesBlock> codec() {
    throw new UnsupportedOperationException("VinesLeavesBlock does not provide a codec");
  }

  private Optional<BlockPos> findColumnEnd(BlockGetter level, BlockPos pos) {
    BlockPos.MutableBlockPos mutable = pos.mutable();
    BlockState state;
    do {
      mutable.move(Direction.DOWN);
      state = level.getBlockState(mutable);
    } while (state.is(vinePlantBlock));
    if (state.is(vineTipBlock)) {
      return Optional.of(mutable.immutable());
    }
    return Optional.empty();
  }

  @Override
  public boolean isValidBonemealTarget(@NotNull LevelReader levelReader, @NotNull BlockPos blockPos, @NotNull BlockState state) {
    Optional<BlockPos> optional = findColumnEnd(levelReader, blockPos);
    return (optional.isPresent() && levelReader.getBlockState(optional.get().relative(Direction.DOWN)).isAir())
        || levelReader.getBlockState(blockPos.relative(Direction.DOWN)).isAir();
  }

  @Override
  public boolean isBonemealSuccess(@NotNull Level level, @NotNull RandomSource randomSource, @NotNull BlockPos blockPos, @NotNull BlockState blockState) {
    return true;
  }

  @Override
  public void performBonemeal(@NotNull ServerLevel serverLevel, @NotNull RandomSource randomSource, @NotNull BlockPos blockPos, @NotNull BlockState blockState) {
    Optional<BlockPos> optional = findColumnEnd(serverLevel, blockPos);
    if (optional.isPresent()) {
      BlockState blockState2 = serverLevel.getBlockState(optional.get());
      ((DownwardVineBlock) blockState2.getBlock()).performBonemeal(serverLevel, randomSource, optional.get(), blockState2);
    }
    if (optional.isEmpty()) {
      serverLevel.setBlock(blockPos.below(), vineTipBlock.defaultBlockState(), Block.UPDATE_CLIENTS);
    }
  }
}
