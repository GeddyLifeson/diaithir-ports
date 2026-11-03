package com.diaithir.overworldbiomes.naturesspirit.world.feature;

import com.diaithir.overworldbiomes.naturesspirit.blocks.BranchingTrunkBlock;
import com.diaithir.overworldbiomes.naturesspirit.registration.NSWoods;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.jetbrains.annotations.Nullable;

public class JoshuaTreeFeature extends Feature<NoneFeatureConfiguration> {

  public JoshuaTreeFeature(Codec<NoneFeatureConfiguration> codec) {
    super(codec);
  }

  @Override
  public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
    WorldGenLevel structureWorldAccess = context.level();
    BlockPos blockPos = context.origin();
    RandomSource random = context.random();
    return generate2(structureWorldAccess, blockPos, random, 0);
  }

  private static boolean isSurroundedByAir(LevelReader world, BlockPos pos, @Nullable Direction exceptDirection) {
    for (Direction direction : Direction.Plane.HORIZONTAL) {
      if (direction == exceptDirection || world.isEmptyBlock(pos.relative(direction))) {
        continue;
      }
      return false;
    }
    return true;
  }

  private static int nextBetween(RandomSource random, int min, int max) {
    return random.nextInt(max - min + 1) + min;
  }

  private static Direction randomHorizontal(RandomSource random) {
    return Direction.Plane.HORIZONTAL.getRandomDirection(random);
  }

  private static boolean generate2(LevelAccessor world, BlockPos pos, RandomSource random, int layer) {
    BranchingTrunkBlock branchingTrunkBlock = (BranchingTrunkBlock) NSWoods.JOSHUA.getLog();
    int i = nextBetween(random, 1, 3);
    if (layer == 0) i += 2;


    for (int j = 0; j < i; ++j) {
      BlockPos blockPos = pos.above(j + 1);
      if (!isSurroundedByAir(world, blockPos, Direction.DOWN)) return false;
      world.setBlock(blockPos, branchingTrunkBlock.withConnectionProperties(world, blockPos), 2);
      world.setBlock(blockPos.below(), branchingTrunkBlock.withConnectionProperties(world, blockPos.below()), 2);
      world.setBlock(blockPos, branchingTrunkBlock.withConnectionProperties(world, blockPos), 2);
      if (layer > 0) {
        world.setBlock(blockPos.below(2), branchingTrunkBlock.withConnectionProperties(world, blockPos.below(2)), 2);
      }
    }

    if (layer < 2) {
      int k = nextBetween(random, 3, 5);
      if (layer == 0) ++k;

      for (int l = 0; l < k; ++l) {

        Direction direction = randomHorizontal(random);
        int m = nextBetween(random, 1, 2);
        int n = Math.max(1, i - m);
        BlockPos blockPos2 = pos.above(n).relative(direction, m);

        if (world.isEmptyBlock(blockPos2) && isSurroundedByAir(world, blockPos2, direction.getOpposite())) {

          world.setBlock(blockPos2, branchingTrunkBlock.withConnectionProperties(world, blockPos2), 2);
          world.setBlock(blockPos2.relative(direction.getOpposite()), branchingTrunkBlock.withConnectionProperties(world, blockPos2.relative(direction.getOpposite())), 2);
          for (int p = m; p > 0; --p) {
            world.setBlock(blockPos2.relative(direction.getOpposite(), p), branchingTrunkBlock.withConnectionProperties(world, blockPos2.relative(direction.getOpposite(), p)), 2);
          }

          generate2(world, blockPos2.above(), random, layer + 1);

          if (world.isEmptyBlock(blockPos2.above())) {

            world.setBlock(blockPos2.above(), NSWoods.JOSHUA.getLeaves().defaultBlockState().setValue(LeavesBlock.DISTANCE, 1), 2);
            world.setBlock(blockPos2, ((BranchingTrunkBlock) NSWoods.JOSHUA.getLog()).withConnectionProperties(world, blockPos2), 2);

            Direction direction2 = randomHorizontal(random);
            if (random.nextFloat() < .65F && world.isEmptyBlock(blockPos2.relative(direction, 1))) {
              world.setBlock(blockPos2.relative(direction2, 1), NSWoods.JOSHUA.getLeaves().defaultBlockState().setValue(LeavesBlock.DISTANCE, 1), 2);
              world.setBlock(blockPos2, ((BranchingTrunkBlock) NSWoods.JOSHUA.getLog()).withConnectionProperties(world, blockPos2), 2);
            }

          }
          else if (world.isEmptyBlock(blockPos2.above(2)) && !world.isEmptyBlock(blockPos2.above())) {

            world.setBlock(blockPos2.above(2), NSWoods.JOSHUA.getLeaves().defaultBlockState().setValue(LeavesBlock.DISTANCE, 1), 2);
            world.setBlock(blockPos2.above(1), ((BranchingTrunkBlock) NSWoods.JOSHUA.getLog()).withConnectionProperties(world, blockPos2.above()), 2);

            Direction direction2 = randomHorizontal(random);
            if (random.nextFloat() < .65F && world.isEmptyBlock(blockPos2.above().relative(direction, 1))) {
              world.setBlock(blockPos2.above().relative(direction2, 1), NSWoods.JOSHUA.getLeaves().defaultBlockState().setValue(LeavesBlock.DISTANCE, 1), 2);
              world.setBlock(blockPos2.above(), ((BranchingTrunkBlock) NSWoods.JOSHUA.getLog()).withConnectionProperties(world, blockPos2.above()), 2);
            }

          }
        }
      }
      return true;
    }
    world.setBlock(pos.above(i), NSWoods.JOSHUA.getLeaves().defaultBlockState().setValue(LeavesBlock.DISTANCE, 1), 2);
    world.setBlock(pos.above(i - 1), ((BranchingTrunkBlock) NSWoods.JOSHUA.getLog()).withConnectionProperties(world, pos.above(i - 1)), 2);
    Direction direction = randomHorizontal(random);
    if (world.isEmptyBlock(pos.above(i - 1).relative(direction, 1))) {
      world.setBlock(pos.above(i - 1).relative(direction, 1), NSWoods.JOSHUA.getLeaves().defaultBlockState().setValue(LeavesBlock.DISTANCE, 1), 2);
      world.setBlock(pos.above(i - 1), ((BranchingTrunkBlock) NSWoods.JOSHUA.getLog()).withConnectionProperties(world, pos.above(i - 1)), 2);
    }
    return true;
  }
}
