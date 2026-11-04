package com.diaithir.overworldbiomes.naturesspirit.world.trunk;

import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.diaithir.overworldbiomes.naturesspirit.registration.NSWorldGen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

import java.util.List;
import java.util.function.BiConsumer;

public class MegaSugiTrunkPlacer extends TrunkPlacer {

  public static final MapCodec<MegaSugiTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec((instance) -> trunkPlacerParts(instance).apply(instance, MegaSugiTrunkPlacer::new));

  public MegaSugiTrunkPlacer(int i, int j, int k) {
    super(i, j, k);
  }

  @Override
  protected TrunkPlacerType<?> type() {
    return NSWorldGen.MEGA_SUGI_TRUNK_PLACER.get();
  }

  @Override
  public List<FoliagePlacer.FoliageAttachment> placeTrunk(WorldGenLevel world, BiConsumer<BlockPos, BlockState> replacer, RandomSource random, int height, BlockPos startPos,
      TreeConfiguration config) {
    BlockPos blockPos = startPos.below();
    placeBelowTrunkBlock(world, replacer, random, blockPos, config);
    placeBelowTrunkBlock(world, replacer, random, blockPos.east(), config);
    placeBelowTrunkBlock(world, replacer, random, blockPos.south(), config);
    placeBelowTrunkBlock(world, replacer, random, blockPos.south().east(), config);
    BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
    List<FoliagePlacer.FoliageAttachment> nodes = Lists.newArrayList();
    int m = 10;

    for (int i = 0; i < height; ++i) {
      this.setLog(world, replacer, random, mutable, config, startPos, 0, i, 0);
      int j = startPos.getY() + i;
      if (i < height - 1) {
        this.setLog(world, replacer, random, mutable, config, startPos, 1, i, 0);
        this.setLog(world, replacer, random, mutable, config, startPos, 1, i, 1);
        this.setLog(world, replacer, random, mutable, config, startPos, 0, i, 1);
        if (i > 4 && i % 2 == 0) {
          int randomInt = random.nextFloat() < .75f || j < height / 3 ? 1 : random.nextFloat() < .5 ? -1 : 0;
          m = m - randomInt;
          Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
          this.generateExtraBranch(world, replacer, random, height, config, nodes, startPos.relative(direction.getClockWise(), 1), j, direction, m, i);
          if (random.nextFloat() < .85) {
            this.generateExtraBranch(world, replacer, random, height, config, nodes, startPos, j, direction.getClockWise(), m, i);
          }
          if (random.nextFloat() < .85) {
            this.generateExtraBranch(world, replacer, random, height, config, nodes, startPos.relative(direction.getClockWise(), 1), j,
                direction.getClockWise().getClockWise(), m, i);
          }
          this.generateExtraBranch(world, replacer, random, height, config, nodes, startPos, j, direction.getClockWise().getClockWise().getClockWise(), m, i);
        }
      }
      if (i == height - 1) {
        nodes.add(new FoliagePlacer.FoliageAttachment(mutable.set(startPos.getX(), j + 1, startPos.getZ()), 0, false));
        nodes.add(new FoliagePlacer.FoliageAttachment(mutable.set(startPos.getX(), j, startPos.getZ()), 1, false));
      }
    }

    return nodes;
  }

  private void generateExtraBranch(WorldGenLevel world, BiConsumer<BlockPos, BlockState> replacer, RandomSource random, int height, TreeConfiguration config,
      List<FoliagePlacer.FoliageAttachment> nodes, BlockPos pos, int yOffset, Direction direction, int steps, int b) {
    int j = pos.getX();
    int k = pos.getZ();
    boolean bl = random.nextFloat() < .5F;
    Direction direction2 = bl ? direction.getClockWise() : direction.getCounterClockWise();
    BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos().set(pos);

    for (int l = 0; steps > 0 && (steps < (bl ? 7 : 10)); --steps) {
      if (l >= 1) {
        j += direction.getStepX();
        if (bl) {
          k += Math.max(0, direction2.getStepZ() - random.nextInt(2));
        } else {
          k += direction.getStepZ();
        }
        this.placeLog(world, replacer, random, mutable.set(j, yOffset, k), config, blockState -> blockState.trySetValue(RotatedPillarBlock.AXIS, direction.getAxis()));

        if (l == height - 1 || steps == 1) {
          BlockPos blockPos = new BlockPos(j, yOffset, k);
          nodes.add(new FoliagePlacer.FoliageAttachment(blockPos, b > height / 3 ? (b > height / 2 ? 0 : 1) : 2, false));
        }
      }
      ++l;
    }

  }

  private void setLog(WorldGenLevel world, BiConsumer<BlockPos, BlockState> replacer, RandomSource random, BlockPos.MutableBlockPos tmpPos, TreeConfiguration config, BlockPos startPos, int dx,
      int dy, int dz) {
    tmpPos.setWithOffset(startPos, dx, dy, dz);
    this.placeLogIfFree(world, replacer, random, tmpPos, config);
  }
}
