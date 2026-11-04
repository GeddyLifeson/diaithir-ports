package com.diaithir.overworldbiomes.naturesspirit.world.feature;

import com.diaithir.overworldbiomes.naturesspirit.blocks.GrowingBranchingTrunkBlock;
import com.diaithir.overworldbiomes.naturesspirit.registration.NSMiscBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class AlluaudiaFeature extends Feature<NoneFeatureConfiguration> {

  public AlluaudiaFeature(Codec<NoneFeatureConfiguration> codec) {
    super(codec);
  }

  @Override
  public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
    WorldGenLevel structureWorldAccess = context.level();
    BlockPos blockPos = context.origin();
    RandomSource random = context.random();
    generate2(structureWorldAccess, blockPos, random, blockPos, 6, 0);
    return true;
  }

  private static boolean isSurroundedByAir(LevelReader world, BlockPos pos, @Nullable Direction exceptDirection) {
    Iterator<Direction> var3 = Direction.Plane.HORIZONTAL.iterator();

    Direction direction;
    do {
      if (!var3.hasNext()) {
        return true;
      }

      direction = var3.next();
    } while (direction == exceptDirection || world.isEmptyBlock(pos.relative(direction)));

    return false;
  }

  private static Direction randomDirection(RandomSource random) {
    return Direction.getRandom(random);
  }

  private static void generate2(LevelAccessor world, BlockPos pos, RandomSource random, BlockPos rootPos, int size, int layer) {
    GrowingBranchingTrunkBlock branchingTrunkBlock = (GrowingBranchingTrunkBlock) NSMiscBlocks.ALLUAUDIA;
    int i = random.nextInt(1) + 2;
    if (layer == 0) {
      ++i;
    }

    for (int j = 0; j < i; ++j) {
      BlockPos blockPos = pos.above(j + 1);
      if (layer > 0) {
        blockPos = pos.above(j == 0 ? 1 : (int) (j / .93));
      }
      if (!isSurroundedByAir(world, blockPos, randomDirection(random))) {
        return;
      }
      world.setBlock(blockPos, branchingTrunkBlock.withConnectionProperties(world, blockPos), Block.UPDATE_CLIENTS);
      world.setBlock(blockPos.below(), branchingTrunkBlock.withConnectionProperties(world, blockPos.below()), Block.UPDATE_CLIENTS);
    }

    if (layer < 2) {
      int k = random.nextInt(2) + 4;
      if (layer == 0) {
        ++k;
      }

      for (int l = 0; l < k; ++l) {
        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int m = random.nextInt(2);
        int n = i - m == 0 ? 1 : i - m;
        BlockPos blockPos2 = pos.above(n).relative(direction, 1);
        if (Math.abs(blockPos2.getX() - rootPos.getX()) < size && Math.abs(blockPos2.getZ() - rootPos.getZ()) < size && world.isEmptyBlock(blockPos2) && world.isEmptyBlock(blockPos2.below())) {

          world.setBlock(blockPos2, branchingTrunkBlock.withHorizontalConnectingProperties(world, blockPos2), Block.UPDATE_CLIENTS);

          world.setBlock(blockPos2.relative(direction.getClockWise(), 1).above(),
              branchingTrunkBlock.withConnectionPropertiesVertical(world, blockPos2.relative(direction.getClockWise(), 1).above()), Block.UPDATE_CLIENTS);
          world.setBlock(blockPos2.relative(direction.getClockWise(), 1),
              branchingTrunkBlock.withConnectionPropertiesVertical(world, blockPos2.relative(direction.getClockWise(), 1)), Block.UPDATE_CLIENTS);
          world.setBlock(blockPos2.relative(direction.getClockWise(), 1).above(2),
              branchingTrunkBlock.withConnectionPropertiesVertical(world, blockPos2.relative(direction.getClockWise(), 1).above(2)), Block.UPDATE_CLIENTS);
          world.setBlock(blockPos2.relative(direction.getClockWise(), 1).above(),
              branchingTrunkBlock.withConnectionPropertiesVertical(world, blockPos2.relative(direction.getClockWise(), 1).above()), Block.UPDATE_CLIENTS);
          world.setBlock(blockPos2, branchingTrunkBlock.withConnectionProperties(world, blockPos2), Block.UPDATE_CLIENTS);
          world.setBlock(blockPos2.relative(direction.getOpposite()), branchingTrunkBlock.withConnectionProperties(world, blockPos2.relative(direction.getOpposite())),
              Block.UPDATE_CLIENTS);
          world.setBlock(blockPos2.relative(direction.getClockWise(), 1),
              branchingTrunkBlock.withConnectionProperties(world, blockPos2.relative(direction.getClockWise(), 1)), Block.UPDATE_CLIENTS);
          world.setBlock(blockPos2, branchingTrunkBlock.withHorizontalConnectingProperties(world, blockPos2), Block.UPDATE_CLIENTS);

          generate2(world, pos.above(n + 1), random, rootPos, size, layer + 1);

          if (random.nextBoolean()) {
            world.setBlock(blockPos2.relative(direction.getClockWise()).relative(direction).above(),
                branchingTrunkBlock.withConnectionProperties(world, blockPos2.relative(direction.getClockWise()).relative(direction).above()), Block.UPDATE_CLIENTS);
            world.setBlock(blockPos2.relative(direction.getClockWise(), 1).above(),
                branchingTrunkBlock.withConnectionProperties(world, blockPos2.relative(direction.getClockWise(), 1).above()), Block.UPDATE_CLIENTS);
          } else {
            world.setBlock(blockPos2.relative(direction.getClockWise(), 2).above(),
                branchingTrunkBlock.withConnectionProperties(world, blockPos2.relative(direction.getClockWise(), 2).above()), Block.UPDATE_CLIENTS);
            world.setBlock(blockPos2.relative(direction.getClockWise(), 1).above(),
                branchingTrunkBlock.withConnectionProperties(world, blockPos2.relative(direction.getClockWise(), 1).above()), Block.UPDATE_CLIENTS);
          }
        }
      }
    }
  }
}
