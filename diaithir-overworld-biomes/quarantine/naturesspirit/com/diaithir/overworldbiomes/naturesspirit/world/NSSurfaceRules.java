package com.diaithir.overworldbiomes.naturesspirit.world;

import com.diaithir.overworldbiomes.naturesspirit.registration.NSBiomes;
import com.diaithir.overworldbiomes.naturesspirit.registration.NSColoredBlocks;
import com.diaithir.overworldbiomes.naturesspirit.registration.NSMiscBlocks;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.CaveSurface;

import java.util.function.Supplier;

public class NSSurfaceRules {

  private static SurfaceRules.RuleSource GRAVEL() { return makeStateRule(Blocks.GRAVEL); }
  private static SurfaceRules.RuleSource STONE() { return makeStateRule(Blocks.STONE); }
  private static SurfaceRules.RuleSource GRASS() { return makeStateRule(Blocks.GRASS_BLOCK); }
  private static SurfaceRules.RuleSource DIRT() { return makeStateRule(Blocks.DIRT); }
  private static SurfaceRules.RuleSource ROOTED_DIRT() { return makeStateRule(Blocks.ROOTED_DIRT); }
  private static SurfaceRules.RuleSource GRANITE() { return makeStateRule(Blocks.GRANITE); }
  private static SurfaceRules.RuleSource PODZOL() { return makeStateRule(Blocks.PODZOL); }
  private static SurfaceRules.RuleSource CALCITE() { return makeStateRule(Blocks.CALCITE); }
  private static SurfaceRules.RuleSource SANDY_SOIL() { return makeStateRule(NSMiscBlocks.SANDY_SOIL.get()); }
  private static SurfaceRules.RuleSource RED_MOSS_BLOCK() { return makeStateRule(NSMiscBlocks.RED_MOSS_BLOCK.get()); }
  private static SurfaceRules.RuleSource COARSE_DIRT() { return makeStateRule(Blocks.COARSE_DIRT); }
  private static SurfaceRules.RuleSource WHITE_KAOLIN() { return makeStateRule(NSColoredBlocks.WHITE_KAOLIN.get()); }
  private static SurfaceRules.RuleSource PINK_SANDSTONE() { return makeStateRule(NSMiscBlocks.PINK_SANDSTONE.get()); }
  private static SurfaceRules.RuleSource RED_SANDSTONE() { return makeStateRule(Blocks.RED_SANDSTONE); }
  private static SurfaceRules.RuleSource RED_SAND() { return makeStateRule(Blocks.RED_SAND); }
  private static SurfaceRules.RuleSource TRAVERTINE() { return makeStateRule(NSMiscBlocks.TRAVERTINE.getBase()); }
  private static SurfaceRules.RuleSource WHITE_CHALK() { return makeStateRule(NSColoredBlocks.WHITE_CHALK.get()); }
  private static SurfaceRules.RuleSource PINK_SAND() { return makeStateRule(NSMiscBlocks.PINK_SAND.get()); }
  private static SurfaceRules.RuleSource CHERT() { return makeStateRule(NSMiscBlocks.CHERT.getBase()); }
  private static SurfaceRules.RuleSource SNOW_BLOCK() { return makeStateRule(Blocks.SNOW_BLOCK); }
  private static SurfaceRules.RuleSource WATER() { return makeStateRule(Blocks.WATER); }
  private static SurfaceRules.RuleSource POWDER_SNOW() { return makeStateRule(Blocks.POWDER_SNOW); }

  private static SurfaceRules.ConditionSource stoneDepthFloorDown1() {
    return SurfaceRules.stoneDepthCheck(1, false, CaveSurface.FLOOR);
  }

  private static SurfaceRules.ConditionSource stoneDepthFloorDown1WithDepth() {
    return SurfaceRules.stoneDepthCheck(1, true, CaveSurface.FLOOR);
  }

  @SafeVarargs
  private static SurfaceRules.ConditionSource biome(ResourceKey<Biome>... keys) {
    return SurfaceRules.isBiome(keys);
  }

  private static SurfaceRules.ConditionSource stoneDepthCeiling() {
    return SurfaceRules.ON_CEILING;
  }

  private static SurfaceRules.ConditionSource stoneDepthFloor() {
    return SurfaceRules.ON_FLOOR;
  }

  private static SurfaceRules.ConditionSource stoneDepthFloorWithSurfaceDepth() {
    return SurfaceRules.UNDER_FLOOR;
  }

  private static SurfaceRules.ConditionSource stoneDepthFloorWithSurfaceDepthRange6() {
    return SurfaceRules.stoneDepthCheck(0, true, 6, CaveSurface.FLOOR);
  }

  private static SurfaceRules.ConditionSource stoneDepthFloorWithSurfaceDepthRange30() {
    return SurfaceRules.stoneDepthCheck(0, true, 30, CaveSurface.FLOOR);
  }

  public static SurfaceRules.RuleSource makeRules() {
    SurfaceRules.ConditionSource above25 = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(25), -1);
    SurfaceRules.ConditionSource above60 = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(60), 0);
    SurfaceRules.ConditionSource above63 = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(63), 0);
    SurfaceRules.ConditionSource above65 = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(65), 0);
    SurfaceRules.ConditionSource above70 = SurfaceRules.yStartCheck(VerticalAnchor.absolute(70), 1);
    SurfaceRules.ConditionSource above76 = SurfaceRules.yStartCheck(VerticalAnchor.absolute(76), 1);
    SurfaceRules.ConditionSource above80 = SurfaceRules.yStartCheck(VerticalAnchor.absolute(80), 1);
    SurfaceRules.ConditionSource above256 = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(256), 0);
    SurfaceRules.ConditionSource materialCondition5 = SurfaceRules.waterBlockCheck(-1, 0);
    SurfaceRules.ConditionSource materialCondition7 = SurfaceRules.waterBlockCheck(0, 0);
    SurfaceRules.ConditionSource holeCondition = SurfaceRules.hole();
    SurfaceRules.ConditionSource noiseCondition1 = SurfaceRules.noiseCondition(Noises.SURFACE, -0.909D, -0.5454D);
    SurfaceRules.ConditionSource noiseCondition2 = SurfaceRules.noiseCondition(Noises.SURFACE, -0.5454D, -0.3818D);
    SurfaceRules.ConditionSource noiseCondition3 = SurfaceRules.noiseCondition(Noises.SURFACE, 0.5454D, 0.909D);
    SurfaceRules.ConditionSource noiseCondition4 = SurfaceRules.noiseCondition(Noises.SURFACE, -0.5454D, 0.0454D);
    SurfaceRules.ConditionSource noiseCondition5 = SurfaceRules.noiseCondition(Noises.SURFACE, 0.2454D, 6D);
    SurfaceRules.ConditionSource noiseCondition6 = SurfaceRules.noiseCondition(Noises.SURFACE, -0.0454D, 6D);

    SurfaceRules.ConditionSource belowWater = SurfaceRules.waterStartCheck(-6, -1);

    SurfaceRules.RuleSource stoneOrGravel = SurfaceRules.sequence(SurfaceRules.ifTrue(stoneDepthCeiling(), STONE()), GRAVEL());
    SurfaceRules.RuleSource pinkSandstoneOrPinkSand = SurfaceRules.sequence(SurfaceRules.ifTrue(stoneDepthCeiling(), PINK_SANDSTONE()), PINK_SAND());
    SurfaceRules.RuleSource redSandstoneOrRedSand = SurfaceRules.sequence(SurfaceRules.ifTrue(stoneDepthCeiling(), RED_SANDSTONE()), RED_SAND());
    SurfaceRules.RuleSource pinkSandstoneOrSoil = SurfaceRules.sequence(SurfaceRules.ifTrue(stoneDepthCeiling(), PINK_SANDSTONE()), SANDY_SOIL());
    SurfaceRules.RuleSource travertineOrSoil = SurfaceRules.sequence(SurfaceRules.ifTrue(stoneDepthCeiling(), TRAVERTINE()), SANDY_SOIL());
    SurfaceRules.RuleSource chertOrSoil = SurfaceRules.sequence(SurfaceRules.ifTrue(stoneDepthCeiling(), CHERT()), SANDY_SOIL());
    SurfaceRules.RuleSource stoneOrMoss = SurfaceRules.sequence(SurfaceRules.ifTrue(stoneDepthCeiling(), STONE()), RED_MOSS_BLOCK());
    SurfaceRules.RuleSource stoneOrSnow = SurfaceRules.sequence(SurfaceRules.ifTrue(stoneDepthCeiling(), STONE()), SNOW_BLOCK());
    SurfaceRules.RuleSource powderSnow = SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.POWDER_SNOW, 0.35, 0.6), SurfaceRules.ifTrue(materialCondition7, POWDER_SNOW()));

    SurfaceRules.RuleSource stratifiedDesertRule = SurfaceRules.ifTrue(stoneDepthFloor(),
        SurfaceRules.ifTrue(biome(NSBiomes.STRATIFIED_DESERT), SurfaceRules.sequence(
            SurfaceRules.ifTrue(above256, CHERT()),
            SurfaceRules.ifTrue(above70, SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.not(above76),
                    SurfaceRules.sequence(
                        SurfaceRules.ifTrue(noiseCondition1, GRASS()),
                        SurfaceRules.ifTrue(noiseCondition2, COARSE_DIRT())
                    )),
                SurfaceRules.ifTrue(noiseCondition3, SANDY_SOIL()),
                SurfaceRules.bandlands()
            )),
            SurfaceRules.ifTrue(materialCondition5, SurfaceRules.sequence(
                SurfaceRules.ifTrue(stoneDepthCeiling(), CHERT()),
                SANDY_SOIL()
            )),
            SurfaceRules.ifTrue(SurfaceRules.not(holeCondition), CHERT()),
            SurfaceRules.ifTrue(belowWater, WHITE_KAOLIN()),
            stoneOrGravel
        ))
    );
    SurfaceRules.RuleSource steppeUndergroundRule = SurfaceRules.ifTrue(
        biome(NSBiomes.SLEETED_SLOPES, NSBiomes.BLOOMING_HIGHLANDS, NSBiomes.SNOWCAPPED_RED_PEAKS, NSBiomes.SHRUBBY_HIGHLANDS, NSBiomes.WOODY_HIGHLANDS,
            NSBiomes.ARID_HIGHLANDS, NSBiomes.STRATIFIED_DESERT),
        SurfaceRules.ifTrue(above25, SurfaceRules.bandlands())
    );

    SurfaceRules.RuleSource desertSteppeRule = SurfaceRules.sequence(
        SurfaceRules.ifTrue(stoneDepthFloor(), SurfaceRules.ifTrue(biome(NSBiomes.ARID_HIGHLANDS, NSBiomes.SHRUBBY_HIGHLANDS, NSBiomes.WOODY_HIGHLANDS, NSBiomes.BLOOMING_HIGHLANDS), SurfaceRules.sequence(
            SurfaceRules.ifTrue(above256, CHERT()),
            SurfaceRules.ifTrue(above70, SurfaceRules.sequence(
                SurfaceRules.ifTrue(biome(NSBiomes.SHRUBBY_HIGHLANDS, NSBiomes.ARID_HIGHLANDS), SurfaceRules.ifTrue(noiseCondition3, SANDY_SOIL())),
                SurfaceRules.ifTrue(biome(NSBiomes.WOODY_HIGHLANDS, NSBiomes.BLOOMING_HIGHLANDS), SurfaceRules.ifTrue(noiseCondition3, GRASS())),
                SurfaceRules.bandlands(),
                SurfaceRules.ifTrue(SurfaceRules.not(above80), SurfaceRules.sequence(
                    SurfaceRules.ifTrue(biome(NSBiomes.SHRUBBY_HIGHLANDS), SurfaceRules.ifTrue(noiseCondition6, GRASS())),
                    SurfaceRules.ifTrue(biome(NSBiomes.WOODY_HIGHLANDS, NSBiomes.BLOOMING_HIGHLANDS), SurfaceRules.ifTrue(noiseCondition6, SANDY_SOIL()))
                ))
            )),
            SurfaceRules.ifTrue(materialCondition5, SurfaceRules.sequence(
                SurfaceRules.ifTrue(stoneDepthCeiling(), CHERT()),
                SurfaceRules.ifTrue(biome(NSBiomes.SHRUBBY_HIGHLANDS), SANDY_SOIL()),
                SurfaceRules.ifTrue(biome(NSBiomes.ARID_HIGHLANDS), PINK_SAND())
            )),
            SurfaceRules.ifTrue(SurfaceRules.not(holeCondition), CHERT())
        ))),
        SurfaceRules.ifTrue(stoneDepthFloorWithSurfaceDepth(), SurfaceRules.ifTrue(biome(NSBiomes.ARID_HIGHLANDS, NSBiomes.SHRUBBY_HIGHLANDS, NSBiomes.WOODY_HIGHLANDS, NSBiomes.BLOOMING_HIGHLANDS), SurfaceRules.ifTrue(belowWater, CHERT())))
    );

    SurfaceRules.RuleSource aspenRule = SurfaceRules.sequence(
        SurfaceRules.ifTrue(SurfaceRules.stoneDepthCheck(4, false, CaveSurface.FLOOR),
            SurfaceRules.ifTrue(biome(NSBiomes.ASPEN_FOREST),
                SurfaceRules.sequence(
                    SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SURFACE, 2.4 / 8.25, Double.MAX_VALUE), COARSE_DIRT()),
                    SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SURFACE, 1.6 / 8.25, Double.MAX_VALUE), ROOTED_DIRT()),
                    SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SURFACE, -1.4 / 8.25, Double.MAX_VALUE), ROOTED_DIRT()),
                    SurfaceRules.ifTrue(stoneDepthFloor(), SurfaceRules.ifTrue(materialCondition7, GRASS())),
                    DIRT()
                ))),
        SurfaceRules.ifTrue(SurfaceRules.stoneDepthCheck(36, false, CaveSurface.FLOOR), SurfaceRules.ifTrue(biome(NSBiomes.ASPEN_FOREST), GRANITE())));

    SurfaceRules.RuleSource xericRule =
        SurfaceRules.ifTrue(belowWater,
            SurfaceRules.sequence(
                SurfaceRules.ifTrue(stoneDepthFloorWithSurfaceDepth(),
                    SurfaceRules.sequence(
                        SurfaceRules.ifTrue(biome(NSBiomes.XERIC_PLAINS, NSBiomes.CEDAR_THICKET), SurfaceRules.ifTrue(noiseCondition4, travertineOrSoil)),
                        SurfaceRules.ifTrue(biome(NSBiomes.ARID_SAVANNA), SurfaceRules.ifTrue(noiseCondition4, chertOrSoil)),
                        SurfaceRules.ifTrue(biome(NSBiomes.WOODED_DRYLANDS), SurfaceRules.ifTrue(noiseCondition4, pinkSandstoneOrSoil)),
                        SurfaceRules.ifTrue(biome(NSBiomes.TUNDRA, NSBiomes.BOREAL_TAIGA), SurfaceRules.ifTrue(noiseCondition4, stoneOrMoss)),
                        SurfaceRules.ifTrue(biome(NSBiomes.SCORCHED_DUNES), SurfaceRules.sequence(SurfaceRules.ifTrue(noiseCondition4, redSandstoneOrRedSand), chertOrSoil)),
                        SurfaceRules.ifTrue(biome(NSBiomes.TROPICAL_SHORES, NSBiomes.DRYLANDS), pinkSandstoneOrPinkSand),
                        SurfaceRules.ifTrue(biome(NSBiomes.SNOWY_FIR_FOREST, NSBiomes.TUNDRA), SurfaceRules.ifTrue(noiseCondition5, stoneOrSnow)),
                        SurfaceRules.ifTrue(biome(NSBiomes.CHAPARRAL), SANDY_SOIL()),
                        SurfaceRules.ifTrue(biome(NSBiomes.SLEETED_SLOPES, NSBiomes.SNOWCAPPED_RED_PEAKS), SurfaceRules.ifTrue(materialCondition7, SNOW_BLOCK())),
                        SurfaceRules.ifTrue(biome(NSBiomes.SLEETED_SLOPES, NSBiomes.SNOWCAPPED_RED_PEAKS, NSBiomes.DUSTY_SLOPES, NSBiomes.RED_PEAKS),
                            SurfaceRules.sequence(
                                SurfaceRules.ifTrue(above256, CHERT()),
                                SurfaceRules.ifTrue(above70, SurfaceRules.bandlands()),
                                SurfaceRules.ifTrue(materialCondition5, CHERT()),
                                SurfaceRules.ifTrue(SurfaceRules.not(holeCondition), CHERT())
                            ))
                    )),
                SurfaceRules.ifTrue(SurfaceRules.stoneDepthCheck(8, true, CaveSurface.FLOOR),
                    SurfaceRules.sequence(
                        SurfaceRules.ifTrue(biome(NSBiomes.LIVELY_DUNES, NSBiomes.BLOOMING_DUNES, NSBiomes.CHAPARRAL), SurfaceRules.bandlands()),
                        SurfaceRules.ifTrue(biome(NSBiomes.DUSTY_SLOPES, NSBiomes.RED_PEAKS), SurfaceRules.ifTrue(SurfaceRules.yBlockCheck(VerticalAnchor.absolute(256), 1), SNOW_BLOCK())),
                        SurfaceRules.ifTrue(biome(NSBiomes.SLEETED_SLOPES, NSBiomes.SNOWCAPPED_RED_PEAKS, NSBiomes.DUSTY_SLOPES, NSBiomes.RED_PEAKS),
                            SurfaceRules.sequence(
                                SurfaceRules.ifTrue(SurfaceRules.steep(), CHERT()),
                                SurfaceRules.ifTrue(belowWater, CHERT()),
                                SurfaceRules.ifTrue(biome(NSBiomes.SLEETED_SLOPES), powderSnow)
                            ))
                    )),
                SurfaceRules.ifTrue(stoneDepthFloorWithSurfaceDepthRange6(), SurfaceRules.sequence(
                    SurfaceRules.ifTrue(biome(NSBiomes.TROPICAL_SHORES), PINK_SANDSTONE()),
                    SurfaceRules.ifTrue(biome(NSBiomes.LIVELY_DUNES, NSBiomes.BLOOMING_DUNES), SANDY_SOIL())
                )),
                SurfaceRules.ifTrue(stoneDepthFloorWithSurfaceDepthRange30(), SurfaceRules.sequence(
                    SurfaceRules.ifTrue(biome(NSBiomes.SCORCHED_DUNES), RED_SANDSTONE()),
                    SurfaceRules.ifTrue(biome(NSBiomes.DRYLANDS), PINK_SANDSTONE())
                ))
            )
        );
    SurfaceRules.RuleSource chaparralRule = SurfaceRules.ifTrue(belowWater, SurfaceRules.sequence(
        SurfaceRules.ifTrue(stoneDepthFloor(), SurfaceRules.ifTrue(biome(NSBiomes.CHAPARRAL), SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.PATCH, 0.0), GRASS()))),
        SurfaceRules.ifTrue(stoneDepthFloorDown1(), SurfaceRules.ifTrue(biome(NSBiomes.CHAPARRAL), SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.PATCH, 0.0), DIRT())))
    ));

    SurfaceRules.RuleSource tropicalBasinRule = SurfaceRules.ifTrue(stoneDepthFloor(),
        SurfaceRules.ifTrue(above60, SurfaceRules.ifTrue(SurfaceRules.not(above63),
            SurfaceRules.ifTrue(biome(NSBiomes.MARSH, NSBiomes.TROPICAL_BASIN, NSBiomes.BAMBOO_WETLANDS), SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SURFACE_SWAMP, 0.0), WATER()))))
    );

    SurfaceRules.RuleSource chalkCliffsRule = SurfaceRules.ifTrue(above65,
        SurfaceRules.sequence(
            SurfaceRules.ifTrue(stoneDepthFloor(), SurfaceRules.ifTrue(biome(NSBiomes.WHITE_CLIFFS), GRASS())),
            SurfaceRules.ifTrue(stoneDepthFloorDown1(), SurfaceRules.ifTrue(biome(NSBiomes.WHITE_CLIFFS), DIRT()))
        )
    );
    SurfaceRules.RuleSource chalkUndergroundRule =
        SurfaceRules.sequence(
            SurfaceRules.ifTrue(SurfaceRules.yBlockCheck(VerticalAnchor.absolute(60), -1), SurfaceRules.ifTrue(biome(NSBiomes.WHITE_CLIFFS), WHITE_CHALK())),
            SurfaceRules.ifTrue(SurfaceRules.yBlockCheck(VerticalAnchor.absolute(45), -1), SurfaceRules.ifTrue(biome(NSBiomes.WHITE_CLIFFS), CALCITE()))
        );
    SurfaceRules.RuleSource redwoodForestRule =
        SurfaceRules.sequence(
            SurfaceRules.ifTrue(stoneDepthFloorDown1WithDepth(),
                SurfaceRules.ifTrue(biome(NSBiomes.REDWOOD_FOREST, NSBiomes.MAPLE_WOODLANDS),
                    SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SURFACE, 1.75 / 8.25, Double.MAX_VALUE), COARSE_DIRT()))),
            SurfaceRules.ifTrue(stoneDepthFloor(),
                SurfaceRules.ifTrue(materialCondition7,
                    SurfaceRules.ifTrue(biome(NSBiomes.REDWOOD_FOREST, NSBiomes.MAPLE_WOODLANDS), SurfaceRules.sequence(
                        SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SURFACE, -.95 / 8.25, Double.MAX_VALUE), PODZOL()),
                        GRASS()
                    )))),
            SurfaceRules.ifTrue(stoneDepthFloorDown1WithDepth(), SurfaceRules.ifTrue(biome(NSBiomes.REDWOOD_FOREST, NSBiomes.MAPLE_WOODLANDS), DIRT()))
        );

    SurfaceRules.RuleSource alpineRule = SurfaceRules.sequence(
        SurfaceRules.ifTrue(stoneDepthFloorDown1WithDepth(), SurfaceRules.ifTrue(biome(NSBiomes.ALPINE_CLEARINGS, NSBiomes.ALPINE_HIGHLANDS, NSBiomes.CONIFEROUS_COVERT, NSBiomes.HEATHER_FIELDS, NSBiomes.GOLDEN_WILDS, NSBiomes.SUGI_FOREST),
            SurfaceRules.ifTrue(noiseCondition2, COARSE_DIRT()))),
        SurfaceRules.ifTrue(stoneDepthFloor(), SurfaceRules.ifTrue(materialCondition7,
            SurfaceRules.ifTrue(biome(NSBiomes.ALPINE_CLEARINGS, NSBiomes.ALPINE_HIGHLANDS, NSBiomes.CONIFEROUS_COVERT, NSBiomes.HEATHER_FIELDS, NSBiomes.GOLDEN_WILDS, NSBiomes.SUGI_FOREST), GRASS()))));

    ImmutableList.Builder<SurfaceRules.RuleSource> builder = ImmutableList.builder();
    SurfaceRules.RuleSource stratifiedDesertSurfaceRule = SurfaceRules.ifTrue(SurfaceRules.abovePreliminarySurface(), stratifiedDesertRule);
    SurfaceRules.RuleSource chaparralSurfaceRule = SurfaceRules.ifTrue(SurfaceRules.abovePreliminarySurface(), chaparralRule);
    SurfaceRules.RuleSource chalkCliffsSurfaceRule = SurfaceRules.ifTrue(SurfaceRules.abovePreliminarySurface(), chalkCliffsRule);
    SurfaceRules.RuleSource xericSurfaceRule = SurfaceRules.ifTrue(SurfaceRules.abovePreliminarySurface(), xericRule);
    SurfaceRules.RuleSource tropicalBasinSurfaceRule = SurfaceRules.ifTrue(SurfaceRules.abovePreliminarySurface(), tropicalBasinRule);
    SurfaceRules.RuleSource aspenSurfaceRule = SurfaceRules.ifTrue(SurfaceRules.abovePreliminarySurface(), aspenRule);
    SurfaceRules.RuleSource desertSteppeSurfaceRule = SurfaceRules.ifTrue(SurfaceRules.abovePreliminarySurface(), desertSteppeRule);
    SurfaceRules.RuleSource redwoodForestSurfaceRule = SurfaceRules.ifTrue(SurfaceRules.abovePreliminarySurface(), redwoodForestRule);
    SurfaceRules.RuleSource alpineSurfaceRule = SurfaceRules.ifTrue(SurfaceRules.abovePreliminarySurface(), alpineRule);
    builder.add(stratifiedDesertSurfaceRule);
    builder.add(chaparralSurfaceRule);
    builder.add(chalkCliffsSurfaceRule);
    builder.add(chalkUndergroundRule);
    builder.add(xericSurfaceRule);
    builder.add(tropicalBasinSurfaceRule);
    builder.add(aspenSurfaceRule);
    builder.add(desertSteppeSurfaceRule);
    builder.add(steppeUndergroundRule);
    builder.add(redwoodForestSurfaceRule);
    builder.add(alpineSurfaceRule);
    return SurfaceRules.sequence(builder
        .build()
        .toArray(SurfaceRules.RuleSource[]::new));
  }

  private static SurfaceRules.RuleSource makeStateRule(Block block) {
    return SurfaceRules.state(block.defaultBlockState());
  }
}
