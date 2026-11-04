package com.diaithir.overworldbiomes.naturesspirit.world.trunk;

import net.minecraft.core.RegistryCodecs;

import net.minecraft.util.valueproviders.IntProviders;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.diaithir.overworldbiomes.naturesspirit.registration.NSWorldGen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

import java.util.List;
import java.util.function.BiConsumer;

public class CoconutTrunkPlacer extends TrunkPlacer {

  public static final MapCodec<CoconutTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
    return trunkPlacerParts(instance).and(instance.group(IntProviders.POSITIVE_CODEC.fieldOf("trunk_steps").forGetter((trunkPlacer) -> {
      return trunkPlacer.trunkSteps;
    }), Codec.floatRange(0.0F, 1.0F).fieldOf("fork_probability").forGetter((trunkPlacer) -> {
      return trunkPlacer.forkProbability;
    }), RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("can_grow_through").forGetter((trunkPlacer) -> {
      return trunkPlacer.canGrowThrough;
    }))).apply(instance, CoconutTrunkPlacer::new);
  });
  private final IntProvider trunkSteps;
  private final float forkProbability;
  private final HolderSet<Block> canGrowThrough;

  public CoconutTrunkPlacer(int baseHeight, int firstRandomHeight, int secondRandomHeight, IntProvider trunkSteps, float forkProbability, HolderSet<Block> canGrowThrough) {
    super(baseHeight, firstRandomHeight, secondRandomHeight);
    this.trunkSteps = trunkSteps;
    this.forkProbability = forkProbability;
    this.canGrowThrough = canGrowThrough;
  }

  @Override
  protected TrunkPlacerType<?> type() {
    return NSWorldGen.COCONUT_TRUNK_PLACER.get();
  }

  @Override
  public List<FoliagePlacer.FoliageAttachment> placeTrunk(WorldGenLevel world, BiConsumer<BlockPos, BlockState> replacer, RandomSource random, int height, BlockPos startPos, TreeConfiguration config) {
    List<FoliagePlacer.FoliageAttachment> list = Lists.newArrayList();
    BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos().set(startPos.getX(), startPos.getY() - 1, startPos.getZ());
    BlockPos.MutableBlockPos forkedMutable = new BlockPos.MutableBlockPos().set(startPos.getX(), startPos.getY() + 1, startPos.getZ());
    boolean forked = random.nextFloat() < this.forkProbability;
    int steps = this.trunkSteps.sample(random);
    int increment = random.nextInt(0, 2);
    int forkedIncrement = random.nextInt(0, 2);
    Direction offsetDirection = Direction.Plane.HORIZONTAL.getRandomDirection(random);
    Direction offsetDirection2 = Direction.Plane.HORIZONTAL.getRandomDirection(random);
    int forkedOffset = random.nextInt(0, 3);
    int forkedHeight = height - forkedOffset;

    for (int i = 0; i < height; ++i) {
      BlockPos pos = mutable.move(0, 1, 0);
      this.placeLog(world, replacer, random, pos, config);
      if (i >= 2) {
        if (forked && i < forkedHeight) {
          BlockPos forkedPos = forkedMutable.move(0, 1, 0);
          this.placeLog(world, replacer, random, forkedPos, config);
          if (i == forkedHeight - 1) {
            list.add(new FoliagePlacer.FoliageAttachment(forkedPos.above(1), 0, false));
          }
        }
        if (i == height - 1) {
          list.add(new FoliagePlacer.FoliageAttachment(pos.above(1), 0, false));
        }
        if (increment == 0) {
          mutable.move(offsetDirection);
          if (offsetDirection2 != offsetDirection) {
            mutable.move(offsetDirection2);
          }
          increment = (height / steps) - random.nextInt(-1, 2);
        }
        if (forkedIncrement == 0) {
          forkedMutable.move(offsetDirection.getOpposite());
          if (offsetDirection2 != offsetDirection) {
            if (random.nextBoolean()) {
              forkedMutable.move(offsetDirection2.getOpposite());
            }
          }
          forkedIncrement = (forkedHeight / steps) - random.nextInt(-1, 2);
        }
        --increment;
        --forkedIncrement;
      }

    }

    return list;
  }

  @Override
  protected boolean validTreePos(WorldGenLevel world, BlockPos pos) {
    return super.validTreePos(world, pos) || world.isStateAtPosition(pos, (state) -> {
      return state.is(this.canGrowThrough);
    });
  }
}
