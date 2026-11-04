package com.diaithir.overworldbiomes.naturesspirit.world.foliage_placer;

import net.minecraft.util.valueproviders.IntProviders;
import com.diaithir.overworldbiomes.naturesspirit.registration.NSWorldGen;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

public class MapleFoliagePlacer extends FoliagePlacer {

  public static final MapCodec<MapleFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
    return foliagePlacerParts(instance).and(instance.group(IntProviders.codec(4, 16).fieldOf("height").forGetter((foliagePlacer) -> {
      return foliagePlacer.height;
    }), Codec.floatRange(0.0F, 1.0F).fieldOf("hanging_leaves_chance").forGetter((foliagePlacer) -> {
      return foliagePlacer.hangingLeavesChance;
    }), Codec.floatRange(0.0F, 1.0F).fieldOf("hanging_leaves_extension_chance").forGetter((foliagePlacer) -> {
      return foliagePlacer.hangingLeavesExtensionChance;
    }))).apply(instance, MapleFoliagePlacer::new);
  });
  private final IntProvider height;
  private final float hangingLeavesChance;
  private final float hangingLeavesExtensionChance;

  public MapleFoliagePlacer(IntProvider radius, IntProvider offset, IntProvider height, float hangingLeavesChance, float hangingLeavesExtensionChance) {
    super(radius, offset);
    this.height = height;
    this.hangingLeavesChance = hangingLeavesChance;
    this.hangingLeavesExtensionChance = hangingLeavesExtensionChance;
  }

  @Override
  protected FoliagePlacerType<?> type() {
    return NSWorldGen.MAPLE_FOLIAGE_PLACER_TYPE.get();
  }

  @Override
  protected void createFoliage(WorldGenLevel world, FoliageSetter placer, RandomSource random, TreeConfiguration config, int trunkHeight, FoliageAttachment treeNode,
      int foliageHeight, int radius, int offset) {
    boolean bl = treeNode.doubleTrunk();
    BlockPos blockPos = treeNode.pos().above(offset);
    int i = radius + treeNode.radiusOffset() - 1;
    this.placeLeavesRow(world, placer, random, config, blockPos, i - 2, foliageHeight - 3, bl);
    this.placeLeavesRow(world, placer, random, config, blockPos, i - 1, foliageHeight - 4, bl);

    for (int j = foliageHeight - 5; j >= 0; --j) {
      this.placeLeavesRow(world, placer, random, config, blockPos, i, j, bl);
    }

    this.placeLeavesRowWithHangingLeavesBelow(world, placer, random, config, blockPos, i, -1, bl, this.hangingLeavesChance, this.hangingLeavesExtensionChance);
    this.placeLeavesRowWithHangingLeavesBelow(world, placer, random, config, blockPos, i - 1, -2, bl, this.hangingLeavesChance, this.hangingLeavesExtensionChance);
  }

  @Override
  public int foliageHeight(RandomSource random, int trunkHeight, TreeConfiguration config) {
    return this.height.sample(random);
  }

  @Override
  protected boolean shouldSkipLocation(RandomSource random, int dx, int y, int dz, int radius, boolean giantTrunk) {
    if (y >= -1 && y <= 0 && (dx == radius || dz == radius || (dx == radius - y || dz == radius - y))) {
      return true;
    }
    boolean bl = dx == radius && dz == radius;
    boolean bl2 = radius > 2;
    if (bl2) {
      return bl || dx + dz > radius * 2 - 2;
    } else {
      return bl;
    }
  }
}
