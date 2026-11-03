package com.diaithir.overworldbiomes.naturesspirit.registration;

import com.diaithir.overworldbiomes.naturesspirit.blocks.PaperLanternBlock;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class NSColoredBlocks {

  public static final String MOD_ID = "natures_spirit";

  public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
  public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);

  /** All registered blocks of this class in registration order, by registry name. */
  public static final Map<String, DeferredBlock<? extends Block>> ALL_BLOCKS = new LinkedHashMap<>();
  /** Registered block items in registration order, by registry name. */
  public static final List<String> BLOCK_ITEM_NAMES = new ArrayList<>();
  /** Blocks that should render with a cutout / transparent render type. */
  public static final List<DeferredBlock<? extends Block>> TRANSPARENT_BLOCKS = new ArrayList<>();

  private static <T extends Block> DeferredBlock<T> reg(String name, Function<BlockBehaviour.Properties, T> factory, BlockBehaviour.Properties props, boolean transparent) {
    DeferredBlock<T> holder = BLOCKS.registerBlock(NSRegistryHelper.name(name), factory, () -> props);
    ITEMS.registerSimpleBlockItem(name, holder);
    ALL_BLOCKS.put(name, holder);
    BLOCK_ITEM_NAMES.add(name);
    if (transparent) {
      TRANSPARENT_BLOCKS.add(holder);
    }
    return holder;
  }

  private static BlockBehaviour.Properties lantern() {
    return NSRegistryHelper.bp().mapColor(MapColor.TERRACOTTA_WHITE).forceSolidOn().strength(0.5F).sound(SoundType.LANTERN)
        .lightLevel(state -> 15).noOcclusion().pushReaction(PushReaction.DESTROY);
  }

  private static BlockBehaviour.Properties kaolinProps(MapColor color) {
    return NSRegistryHelper.bp().mapColor(color).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.25F, 4.2F);
  }

  private static BlockBehaviour.Properties chalkProps(MapColor color) {
    return NSRegistryHelper.bp().mapColor(color).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.5F);
  }

  private static DeferredBlock<Block> lanternBlock(String name) {
    return reg(name, p -> (Block) new PaperLanternBlock(p), NSRegistryHelper.bp().mapColor(MapColor.TERRACOTTA_WHITE).forceSolidOn().strength(0.5F)
        .sound(SoundType.LANTERN).lightLevel(state -> 15).noOcclusion().pushReaction(PushReaction.DESTROY), true);
  }

  private static DeferredBlock<Block> plain(String name, BlockBehaviour.Properties props) {
    return reg(name, Block::new, props, false);
  }

  private static DeferredBlock<Block> stairs(String name, Supplier<? extends Block> base, BlockBehaviour.Properties props) {
    return reg(name, p -> (Block) new StairBlock(base.get().defaultBlockState(), p), props, false);
  }

  private static DeferredBlock<Block> slab(String name, BlockBehaviour.Properties props) {
    return reg(name, p -> (Block) new SlabBlock(p), props, false);
  }

  // ---------------------------------------------------------------- paper lanterns
  public static final DeferredBlock<Block> PAPER_LANTERN = lanternBlock("paper_lantern");
  public static final DeferredBlock<Block> WHITE_PAPER_LANTERN = lanternBlock("white_paper_lantern");
  public static final DeferredBlock<Block> LIGHT_GRAY_PAPER_LANTERN = lanternBlock("light_gray_paper_lantern");
  public static final DeferredBlock<Block> GRAY_PAPER_LANTERN = lanternBlock("gray_paper_lantern");
  public static final DeferredBlock<Block> BLACK_PAPER_LANTERN = lanternBlock("black_paper_lantern");
  public static final DeferredBlock<Block> BROWN_PAPER_LANTERN = lanternBlock("brown_paper_lantern");
  public static final DeferredBlock<Block> RED_PAPER_LANTERN = lanternBlock("red_paper_lantern");
  public static final DeferredBlock<Block> ORANGE_PAPER_LANTERN = lanternBlock("orange_paper_lantern");
  public static final DeferredBlock<Block> YELLOW_PAPER_LANTERN = lanternBlock("yellow_paper_lantern");
  public static final DeferredBlock<Block> LIME_PAPER_LANTERN = lanternBlock("lime_paper_lantern");
  public static final DeferredBlock<Block> GREEN_PAPER_LANTERN = lanternBlock("green_paper_lantern");
  public static final DeferredBlock<Block> BLUE_PAPER_LANTERN = lanternBlock("blue_paper_lantern");
  public static final DeferredBlock<Block> LIGHT_BLUE_PAPER_LANTERN = lanternBlock("light_blue_paper_lantern");
  public static final DeferredBlock<Block> CYAN_PAPER_LANTERN = lanternBlock("cyan_paper_lantern");
  public static final DeferredBlock<Block> PURPLE_PAPER_LANTERN = lanternBlock("purple_paper_lantern");
  public static final DeferredBlock<Block> MAGENTA_PAPER_LANTERN = lanternBlock("magenta_paper_lantern");
  public static final DeferredBlock<Block> PINK_PAPER_LANTERN = lanternBlock("pink_paper_lantern");

  // ---------------------------------------------------------------- kaolin
  public static final DeferredBlock<Block> KAOLIN = plain("kaolin", kaolinProps(MapColor.COLOR_ORANGE));
  public static final DeferredBlock<Block> WHITE_KAOLIN = plain("white_kaolin", kaolinProps(MapColor.TERRACOTTA_WHITE));
  public static final DeferredBlock<Block> LIGHT_GRAY_KAOLIN = plain("light_gray_kaolin", kaolinProps(MapColor.TERRACOTTA_LIGHT_GRAY));
  public static final DeferredBlock<Block> GRAY_KAOLIN = plain("gray_kaolin", kaolinProps(MapColor.TERRACOTTA_GRAY));
  public static final DeferredBlock<Block> BLACK_KAOLIN = plain("black_kaolin", kaolinProps(MapColor.TERRACOTTA_BLACK));
  public static final DeferredBlock<Block> BROWN_KAOLIN = plain("brown_kaolin", kaolinProps(MapColor.TERRACOTTA_BROWN));
  public static final DeferredBlock<Block> RED_KAOLIN = plain("red_kaolin", kaolinProps(MapColor.TERRACOTTA_RED));
  public static final DeferredBlock<Block> ORANGE_KAOLIN = plain("orange_kaolin", kaolinProps(MapColor.TERRACOTTA_ORANGE));
  public static final DeferredBlock<Block> YELLOW_KAOLIN = plain("yellow_kaolin", kaolinProps(MapColor.TERRACOTTA_YELLOW));
  public static final DeferredBlock<Block> LIME_KAOLIN = plain("lime_kaolin", kaolinProps(MapColor.TERRACOTTA_LIGHT_GREEN));
  public static final DeferredBlock<Block> GREEN_KAOLIN = plain("green_kaolin", kaolinProps(MapColor.TERRACOTTA_GREEN));
  public static final DeferredBlock<Block> CYAN_KAOLIN = plain("cyan_kaolin", kaolinProps(MapColor.TERRACOTTA_CYAN));
  public static final DeferredBlock<Block> LIGHT_BLUE_KAOLIN = plain("light_blue_kaolin", kaolinProps(MapColor.TERRACOTTA_LIGHT_BLUE));
  public static final DeferredBlock<Block> BLUE_KAOLIN = plain("blue_kaolin", kaolinProps(MapColor.TERRACOTTA_BLUE));
  public static final DeferredBlock<Block> PURPLE_KAOLIN = plain("purple_kaolin", kaolinProps(MapColor.TERRACOTTA_PURPLE));
  public static final DeferredBlock<Block> MAGENTA_KAOLIN = plain("magenta_kaolin", kaolinProps(MapColor.TERRACOTTA_MAGENTA));
  public static final DeferredBlock<Block> PINK_KAOLIN = plain("pink_kaolin", kaolinProps(MapColor.TERRACOTTA_PINK));

  public static final DeferredBlock<Block> KAOLIN_STAIRS = stairs("kaolin_stairs", KAOLIN, kaolinProps(MapColor.COLOR_ORANGE));
  public static final DeferredBlock<Block> WHITE_KAOLIN_STAIRS = stairs("white_kaolin_stairs", WHITE_KAOLIN, kaolinProps(MapColor.TERRACOTTA_WHITE));
  public static final DeferredBlock<Block> LIGHT_GRAY_KAOLIN_STAIRS = stairs("light_gray_kaolin_stairs", LIGHT_GRAY_KAOLIN, kaolinProps(MapColor.TERRACOTTA_LIGHT_GRAY));
  public static final DeferredBlock<Block> GRAY_KAOLIN_STAIRS = stairs("gray_kaolin_stairs", GRAY_KAOLIN, kaolinProps(MapColor.TERRACOTTA_GRAY));
  public static final DeferredBlock<Block> BLACK_KAOLIN_STAIRS = stairs("black_kaolin_stairs", BLACK_KAOLIN, kaolinProps(MapColor.TERRACOTTA_BLACK));
  public static final DeferredBlock<Block> BROWN_KAOLIN_STAIRS = stairs("brown_kaolin_stairs", BROWN_KAOLIN, kaolinProps(MapColor.TERRACOTTA_BROWN));
  public static final DeferredBlock<Block> RED_KAOLIN_STAIRS = stairs("red_kaolin_stairs", RED_KAOLIN, kaolinProps(MapColor.TERRACOTTA_RED));
  public static final DeferredBlock<Block> ORANGE_KAOLIN_STAIRS = stairs("orange_kaolin_stairs", ORANGE_KAOLIN, kaolinProps(MapColor.TERRACOTTA_ORANGE));
  public static final DeferredBlock<Block> YELLOW_KAOLIN_STAIRS = stairs("yellow_kaolin_stairs", YELLOW_KAOLIN, kaolinProps(MapColor.TERRACOTTA_YELLOW));
  public static final DeferredBlock<Block> LIME_KAOLIN_STAIRS = stairs("lime_kaolin_stairs", LIME_KAOLIN, kaolinProps(MapColor.TERRACOTTA_LIGHT_GREEN));
  public static final DeferredBlock<Block> GREEN_KAOLIN_STAIRS = stairs("green_kaolin_stairs", GREEN_KAOLIN, kaolinProps(MapColor.TERRACOTTA_GREEN));
  public static final DeferredBlock<Block> CYAN_KAOLIN_STAIRS = stairs("cyan_kaolin_stairs", CYAN_KAOLIN, kaolinProps(MapColor.TERRACOTTA_CYAN));
  public static final DeferredBlock<Block> LIGHT_BLUE_KAOLIN_STAIRS = stairs("light_blue_kaolin_stairs", LIGHT_BLUE_KAOLIN, kaolinProps(MapColor.TERRACOTTA_LIGHT_BLUE));
  public static final DeferredBlock<Block> BLUE_KAOLIN_STAIRS = stairs("blue_kaolin_stairs", BLUE_KAOLIN, kaolinProps(MapColor.TERRACOTTA_BLUE));
  public static final DeferredBlock<Block> PURPLE_KAOLIN_STAIRS = stairs("purple_kaolin_stairs", PURPLE_KAOLIN, kaolinProps(MapColor.TERRACOTTA_PURPLE));
  public static final DeferredBlock<Block> MAGENTA_KAOLIN_STAIRS = stairs("magenta_kaolin_stairs", MAGENTA_KAOLIN, kaolinProps(MapColor.TERRACOTTA_MAGENTA));
  public static final DeferredBlock<Block> PINK_KAOLIN_STAIRS = stairs("pink_kaolin_stairs", PINK_KAOLIN, kaolinProps(MapColor.TERRACOTTA_PINK));

  public static final DeferredBlock<Block> KAOLIN_SLAB = slab("kaolin_slab", kaolinProps(MapColor.COLOR_ORANGE));
  public static final DeferredBlock<Block> WHITE_KAOLIN_SLAB = slab("white_kaolin_slab", kaolinProps(MapColor.TERRACOTTA_WHITE));
  public static final DeferredBlock<Block> LIGHT_GRAY_KAOLIN_SLAB = slab("light_gray_kaolin_slab", kaolinProps(MapColor.TERRACOTTA_LIGHT_GRAY));
  public static final DeferredBlock<Block> GRAY_KAOLIN_SLAB = slab("gray_kaolin_slab", kaolinProps(MapColor.TERRACOTTA_GRAY));
  public static final DeferredBlock<Block> BLACK_KAOLIN_SLAB = slab("black_kaolin_slab", kaolinProps(MapColor.TERRACOTTA_BLACK));
  public static final DeferredBlock<Block> BROWN_KAOLIN_SLAB = slab("brown_kaolin_slab", kaolinProps(MapColor.TERRACOTTA_BROWN));
  public static final DeferredBlock<Block> RED_KAOLIN_SLAB = slab("red_kaolin_slab", kaolinProps(MapColor.TERRACOTTA_RED));
  public static final DeferredBlock<Block> ORANGE_KAOLIN_SLAB = slab("orange_kaolin_slab", kaolinProps(MapColor.TERRACOTTA_ORANGE));
  public static final DeferredBlock<Block> YELLOW_KAOLIN_SLAB = slab("yellow_kaolin_slab", kaolinProps(MapColor.TERRACOTTA_YELLOW));
  public static final DeferredBlock<Block> LIME_KAOLIN_SLAB = slab("lime_kaolin_slab", kaolinProps(MapColor.TERRACOTTA_LIGHT_GREEN));
  public static final DeferredBlock<Block> GREEN_KAOLIN_SLAB = slab("green_kaolin_slab", kaolinProps(MapColor.TERRACOTTA_GREEN));
  public static final DeferredBlock<Block> CYAN_KAOLIN_SLAB = slab("cyan_kaolin_slab", kaolinProps(MapColor.TERRACOTTA_CYAN));
  public static final DeferredBlock<Block> LIGHT_BLUE_KAOLIN_SLAB = slab("light_blue_kaolin_slab", kaolinProps(MapColor.TERRACOTTA_LIGHT_BLUE));
  public static final DeferredBlock<Block> BLUE_KAOLIN_SLAB = slab("blue_kaolin_slab", kaolinProps(MapColor.TERRACOTTA_BLUE));
  public static final DeferredBlock<Block> PURPLE_KAOLIN_SLAB = slab("purple_kaolin_slab", kaolinProps(MapColor.TERRACOTTA_PURPLE));
  public static final DeferredBlock<Block> MAGENTA_KAOLIN_SLAB = slab("magenta_kaolin_slab", kaolinProps(MapColor.TERRACOTTA_MAGENTA));
  public static final DeferredBlock<Block> PINK_KAOLIN_SLAB = slab("pink_kaolin_slab", kaolinProps(MapColor.TERRACOTTA_PINK));

  public static final DeferredBlock<Block> KAOLIN_BRICKS = plain("kaolin_bricks", kaolinProps(MapColor.COLOR_ORANGE));
  public static final DeferredBlock<Block> WHITE_KAOLIN_BRICKS = plain("white_kaolin_bricks", kaolinProps(MapColor.TERRACOTTA_WHITE));
  public static final DeferredBlock<Block> LIGHT_GRAY_KAOLIN_BRICKS = plain("light_gray_kaolin_bricks", kaolinProps(MapColor.TERRACOTTA_LIGHT_GRAY));
  public static final DeferredBlock<Block> GRAY_KAOLIN_BRICKS = plain("gray_kaolin_bricks", kaolinProps(MapColor.TERRACOTTA_GRAY));
  public static final DeferredBlock<Block> BLACK_KAOLIN_BRICKS = plain("black_kaolin_bricks", kaolinProps(MapColor.TERRACOTTA_BLACK));
  public static final DeferredBlock<Block> BROWN_KAOLIN_BRICKS = plain("brown_kaolin_bricks", kaolinProps(MapColor.TERRACOTTA_BROWN));
  public static final DeferredBlock<Block> RED_KAOLIN_BRICKS = plain("red_kaolin_bricks", kaolinProps(MapColor.TERRACOTTA_RED));
  public static final DeferredBlock<Block> ORANGE_KAOLIN_BRICKS = plain("orange_kaolin_bricks", kaolinProps(MapColor.TERRACOTTA_ORANGE));
  public static final DeferredBlock<Block> YELLOW_KAOLIN_BRICKS = plain("yellow_kaolin_bricks", kaolinProps(MapColor.TERRACOTTA_YELLOW));
  public static final DeferredBlock<Block> LIME_KAOLIN_BRICKS = plain("lime_kaolin_bricks", kaolinProps(MapColor.TERRACOTTA_LIGHT_GREEN));
  public static final DeferredBlock<Block> GREEN_KAOLIN_BRICKS = plain("green_kaolin_bricks", kaolinProps(MapColor.TERRACOTTA_GREEN));
  public static final DeferredBlock<Block> CYAN_KAOLIN_BRICKS = plain("cyan_kaolin_bricks", kaolinProps(MapColor.TERRACOTTA_CYAN));
  public static final DeferredBlock<Block> LIGHT_BLUE_KAOLIN_BRICKS = plain("light_blue_kaolin_bricks", kaolinProps(MapColor.TERRACOTTA_LIGHT_BLUE));
  public static final DeferredBlock<Block> BLUE_KAOLIN_BRICKS = plain("blue_kaolin_bricks", kaolinProps(MapColor.TERRACOTTA_BLUE));
  public static final DeferredBlock<Block> PURPLE_KAOLIN_BRICKS = plain("purple_kaolin_bricks", kaolinProps(MapColor.TERRACOTTA_PURPLE));
  public static final DeferredBlock<Block> MAGENTA_KAOLIN_BRICKS = plain("magenta_kaolin_bricks", kaolinProps(MapColor.TERRACOTTA_MAGENTA));
  public static final DeferredBlock<Block> PINK_KAOLIN_BRICKS = plain("pink_kaolin_bricks", kaolinProps(MapColor.TERRACOTTA_PINK));

  public static final DeferredBlock<Block> KAOLIN_BRICK_STAIRS = stairs("kaolin_brick_stairs", KAOLIN_BRICKS, kaolinProps(MapColor.COLOR_ORANGE));
  public static final DeferredBlock<Block> WHITE_KAOLIN_BRICK_STAIRS = stairs("white_kaolin_brick_stairs", WHITE_KAOLIN_BRICKS, kaolinProps(MapColor.TERRACOTTA_WHITE));
  public static final DeferredBlock<Block> LIGHT_GRAY_KAOLIN_BRICK_STAIRS = stairs("light_gray_kaolin_brick_stairs", LIGHT_GRAY_KAOLIN_BRICKS, kaolinProps(MapColor.TERRACOTTA_LIGHT_GRAY));
  public static final DeferredBlock<Block> GRAY_KAOLIN_BRICK_STAIRS = stairs("gray_kaolin_brick_stairs", GRAY_KAOLIN_BRICKS, kaolinProps(MapColor.TERRACOTTA_GRAY));
  public static final DeferredBlock<Block> BLACK_KAOLIN_BRICK_STAIRS = stairs("black_kaolin_brick_stairs", BLACK_KAOLIN_BRICKS, kaolinProps(MapColor.TERRACOTTA_BLACK));
  public static final DeferredBlock<Block> BROWN_KAOLIN_BRICK_STAIRS = stairs("brown_kaolin_brick_stairs", BROWN_KAOLIN_BRICKS, kaolinProps(MapColor.TERRACOTTA_BROWN));
  public static final DeferredBlock<Block> RED_KAOLIN_BRICK_STAIRS = stairs("red_kaolin_brick_stairs", RED_KAOLIN_BRICKS, kaolinProps(MapColor.TERRACOTTA_RED));
  public static final DeferredBlock<Block> ORANGE_KAOLIN_BRICK_STAIRS = stairs("orange_kaolin_brick_stairs", ORANGE_KAOLIN_BRICKS, kaolinProps(MapColor.TERRACOTTA_ORANGE));
  public static final DeferredBlock<Block> YELLOW_KAOLIN_BRICK_STAIRS = stairs("yellow_kaolin_brick_stairs", YELLOW_KAOLIN_BRICKS, kaolinProps(MapColor.TERRACOTTA_YELLOW));
  public static final DeferredBlock<Block> LIME_KAOLIN_BRICK_STAIRS = stairs("lime_kaolin_brick_stairs", LIME_KAOLIN_BRICKS, kaolinProps(MapColor.TERRACOTTA_LIGHT_GREEN));
  public static final DeferredBlock<Block> GREEN_KAOLIN_BRICK_STAIRS = stairs("green_kaolin_brick_stairs", GREEN_KAOLIN_BRICKS, kaolinProps(MapColor.TERRACOTTA_GREEN));
  public static final DeferredBlock<Block> CYAN_KAOLIN_BRICK_STAIRS = stairs("cyan_kaolin_brick_stairs", CYAN_KAOLIN_BRICKS, kaolinProps(MapColor.TERRACOTTA_CYAN));
  public static final DeferredBlock<Block> LIGHT_BLUE_KAOLIN_BRICK_STAIRS = stairs("light_blue_kaolin_brick_stairs", LIGHT_BLUE_KAOLIN_BRICKS, kaolinProps(MapColor.TERRACOTTA_LIGHT_BLUE));
  public static final DeferredBlock<Block> BLUE_KAOLIN_BRICK_STAIRS = stairs("blue_kaolin_brick_stairs", BLUE_KAOLIN_BRICKS, kaolinProps(MapColor.TERRACOTTA_BLUE));
  public static final DeferredBlock<Block> PURPLE_KAOLIN_BRICK_STAIRS = stairs("purple_kaolin_brick_stairs", PURPLE_KAOLIN_BRICKS, kaolinProps(MapColor.TERRACOTTA_PURPLE));
  public static final DeferredBlock<Block> MAGENTA_KAOLIN_BRICK_STAIRS = stairs("magenta_kaolin_brick_stairs", MAGENTA_KAOLIN_BRICKS, kaolinProps(MapColor.TERRACOTTA_MAGENTA));
  public static final DeferredBlock<Block> PINK_KAOLIN_BRICK_STAIRS = stairs("pink_kaolin_brick_stairs", PINK_KAOLIN_BRICKS, kaolinProps(MapColor.TERRACOTTA_PINK));

  public static final DeferredBlock<Block> KAOLIN_BRICK_SLAB = slab("kaolin_brick_slab", kaolinProps(MapColor.COLOR_ORANGE));
  public static final DeferredBlock<Block> WHITE_KAOLIN_BRICK_SLAB = slab("white_kaolin_brick_slab", kaolinProps(MapColor.TERRACOTTA_WHITE));
  public static final DeferredBlock<Block> LIGHT_GRAY_KAOLIN_BRICK_SLAB = slab("light_gray_kaolin_brick_slab", kaolinProps(MapColor.TERRACOTTA_LIGHT_GRAY));
  public static final DeferredBlock<Block> GRAY_KAOLIN_BRICK_SLAB = slab("gray_kaolin_brick_slab", kaolinProps(MapColor.TERRACOTTA_GRAY));
  public static final DeferredBlock<Block> BLACK_KAOLIN_BRICK_SLAB = slab("black_kaolin_brick_slab", kaolinProps(MapColor.TERRACOTTA_BLACK));
  public static final DeferredBlock<Block> BROWN_KAOLIN_BRICK_SLAB = slab("brown_kaolin_brick_slab", kaolinProps(MapColor.TERRACOTTA_BROWN));
  public static final DeferredBlock<Block> RED_KAOLIN_BRICK_SLAB = slab("red_kaolin_brick_slab", kaolinProps(MapColor.TERRACOTTA_RED));
  public static final DeferredBlock<Block> ORANGE_KAOLIN_BRICK_SLAB = slab("orange_kaolin_brick_slab", kaolinProps(MapColor.TERRACOTTA_ORANGE));
  public static final DeferredBlock<Block> YELLOW_KAOLIN_BRICK_SLAB = slab("yellow_kaolin_brick_slab", kaolinProps(MapColor.TERRACOTTA_YELLOW));
  public static final DeferredBlock<Block> LIME_KAOLIN_BRICK_SLAB = slab("lime_kaolin_brick_slab", kaolinProps(MapColor.TERRACOTTA_LIGHT_GREEN));
  public static final DeferredBlock<Block> GREEN_KAOLIN_BRICK_SLAB = slab("green_kaolin_brick_slab", kaolinProps(MapColor.TERRACOTTA_GREEN));
  public static final DeferredBlock<Block> CYAN_KAOLIN_BRICK_SLAB = slab("cyan_kaolin_brick_slab", kaolinProps(MapColor.TERRACOTTA_CYAN));
  public static final DeferredBlock<Block> LIGHT_BLUE_KAOLIN_BRICK_SLAB = slab("light_blue_kaolin_brick_slab", kaolinProps(MapColor.TERRACOTTA_LIGHT_BLUE));
  public static final DeferredBlock<Block> BLUE_KAOLIN_BRICK_SLAB = slab("blue_kaolin_brick_slab", kaolinProps(MapColor.TERRACOTTA_BLUE));
  public static final DeferredBlock<Block> PURPLE_KAOLIN_BRICK_SLAB = slab("purple_kaolin_brick_slab", kaolinProps(MapColor.TERRACOTTA_PURPLE));
  public static final DeferredBlock<Block> MAGENTA_KAOLIN_BRICK_SLAB = slab("magenta_kaolin_brick_slab", kaolinProps(MapColor.TERRACOTTA_MAGENTA));
  public static final DeferredBlock<Block> PINK_KAOLIN_BRICK_SLAB = slab("pink_kaolin_brick_slab", kaolinProps(MapColor.TERRACOTTA_PINK));

  // ---------------------------------------------------------------- chalk
  public static final DeferredBlock<Block> WHITE_CHALK = plain("white_chalk", chalkProps(MapColor.TERRACOTTA_WHITE));
  public static final DeferredBlock<Block> LIGHT_GRAY_CHALK = plain("light_gray_chalk", chalkProps(MapColor.TERRACOTTA_LIGHT_GRAY));
  public static final DeferredBlock<Block> GRAY_CHALK = plain("gray_chalk", chalkProps(MapColor.TERRACOTTA_GRAY));
  public static final DeferredBlock<Block> BLACK_CHALK = plain("black_chalk", chalkProps(MapColor.TERRACOTTA_BLACK));
  public static final DeferredBlock<Block> BROWN_CHALK = plain("brown_chalk", chalkProps(MapColor.TERRACOTTA_BROWN));
  public static final DeferredBlock<Block> RED_CHALK = plain("red_chalk", chalkProps(MapColor.TERRACOTTA_RED));
  public static final DeferredBlock<Block> ORANGE_CHALK = plain("orange_chalk", chalkProps(MapColor.TERRACOTTA_ORANGE));
  public static final DeferredBlock<Block> YELLOW_CHALK = plain("yellow_chalk", chalkProps(MapColor.TERRACOTTA_YELLOW));
  public static final DeferredBlock<Block> LIME_CHALK = plain("lime_chalk", chalkProps(MapColor.TERRACOTTA_LIGHT_GREEN));
  public static final DeferredBlock<Block> GREEN_CHALK = plain("green_chalk", chalkProps(MapColor.TERRACOTTA_GREEN));
  public static final DeferredBlock<Block> CYAN_CHALK = plain("cyan_chalk", chalkProps(MapColor.TERRACOTTA_CYAN));
  public static final DeferredBlock<Block> LIGHT_BLUE_CHALK = plain("light_blue_chalk", chalkProps(MapColor.TERRACOTTA_LIGHT_BLUE));
  public static final DeferredBlock<Block> BLUE_CHALK = plain("blue_chalk", chalkProps(MapColor.TERRACOTTA_BLUE));
  public static final DeferredBlock<Block> PURPLE_CHALK = plain("purple_chalk", chalkProps(MapColor.TERRACOTTA_PURPLE));
  public static final DeferredBlock<Block> MAGENTA_CHALK = plain("magenta_chalk", chalkProps(MapColor.TERRACOTTA_MAGENTA));
  public static final DeferredBlock<Block> PINK_CHALK = plain("pink_chalk", chalkProps(MapColor.TERRACOTTA_PINK));

  public static final DeferredBlock<Block> WHITE_CHALK_STAIRS = stairs("white_chalk_stairs", WHITE_CHALK, chalkProps(MapColor.TERRACOTTA_WHITE));
  public static final DeferredBlock<Block> LIGHT_GRAY_CHALK_STAIRS = stairs("light_gray_chalk_stairs", LIGHT_GRAY_CHALK, chalkProps(MapColor.TERRACOTTA_LIGHT_GRAY));
  public static final DeferredBlock<Block> GRAY_CHALK_STAIRS = stairs("gray_chalk_stairs", GRAY_CHALK, chalkProps(MapColor.TERRACOTTA_GRAY));
  public static final DeferredBlock<Block> BLACK_CHALK_STAIRS = stairs("black_chalk_stairs", BLACK_CHALK, chalkProps(MapColor.TERRACOTTA_BLACK));
  public static final DeferredBlock<Block> BROWN_CHALK_STAIRS = stairs("brown_chalk_stairs", BROWN_CHALK, chalkProps(MapColor.TERRACOTTA_BROWN));
  public static final DeferredBlock<Block> RED_CHALK_STAIRS = stairs("red_chalk_stairs", RED_CHALK, chalkProps(MapColor.TERRACOTTA_RED));
  public static final DeferredBlock<Block> ORANGE_CHALK_STAIRS = stairs("orange_chalk_stairs", ORANGE_CHALK, chalkProps(MapColor.TERRACOTTA_ORANGE));
  public static final DeferredBlock<Block> YELLOW_CHALK_STAIRS = stairs("yellow_chalk_stairs", YELLOW_CHALK, chalkProps(MapColor.TERRACOTTA_YELLOW));
  public static final DeferredBlock<Block> LIME_CHALK_STAIRS = stairs("lime_chalk_stairs", LIME_CHALK, chalkProps(MapColor.TERRACOTTA_LIGHT_GREEN));
  public static final DeferredBlock<Block> GREEN_CHALK_STAIRS = stairs("green_chalk_stairs", GREEN_CHALK, chalkProps(MapColor.TERRACOTTA_GREEN));
  public static final DeferredBlock<Block> CYAN_CHALK_STAIRS = stairs("cyan_chalk_stairs", CYAN_CHALK, chalkProps(MapColor.TERRACOTTA_CYAN));
  public static final DeferredBlock<Block> LIGHT_BLUE_CHALK_STAIRS = stairs("light_blue_chalk_stairs", LIGHT_BLUE_CHALK, chalkProps(MapColor.TERRACOTTA_LIGHT_BLUE));
  public static final DeferredBlock<Block> BLUE_CHALK_STAIRS = stairs("blue_chalk_stairs", BLUE_CHALK, chalkProps(MapColor.TERRACOTTA_BLUE));
  public static final DeferredBlock<Block> PURPLE_CHALK_STAIRS = stairs("purple_chalk_stairs", PURPLE_CHALK, chalkProps(MapColor.TERRACOTTA_PURPLE));
  public static final DeferredBlock<Block> MAGENTA_CHALK_STAIRS = stairs("magenta_chalk_stairs", MAGENTA_CHALK, chalkProps(MapColor.TERRACOTTA_MAGENTA));
  public static final DeferredBlock<Block> PINK_CHALK_STAIRS = stairs("pink_chalk_stairs", PINK_CHALK, chalkProps(MapColor.TERRACOTTA_PINK));

  public static final DeferredBlock<Block> WHITE_CHALK_SLAB = slab("white_chalk_slab", chalkProps(MapColor.TERRACOTTA_WHITE));
  public static final DeferredBlock<Block> LIGHT_GRAY_CHALK_SLAB = slab("light_gray_chalk_slab", chalkProps(MapColor.TERRACOTTA_LIGHT_GRAY));
  public static final DeferredBlock<Block> GRAY_CHALK_SLAB = slab("gray_chalk_slab", chalkProps(MapColor.TERRACOTTA_GRAY));
  public static final DeferredBlock<Block> BLACK_CHALK_SLAB = slab("black_chalk_slab", chalkProps(MapColor.TERRACOTTA_BLACK));
  public static final DeferredBlock<Block> BROWN_CHALK_SLAB = slab("brown_chalk_slab", chalkProps(MapColor.TERRACOTTA_BROWN));
  public static final DeferredBlock<Block> RED_CHALK_SLAB = slab("red_chalk_slab", chalkProps(MapColor.TERRACOTTA_RED));
  public static final DeferredBlock<Block> ORANGE_CHALK_SLAB = slab("orange_chalk_slab", chalkProps(MapColor.TERRACOTTA_ORANGE));
  public static final DeferredBlock<Block> YELLOW_CHALK_SLAB = slab("yellow_chalk_slab", chalkProps(MapColor.TERRACOTTA_YELLOW));
  public static final DeferredBlock<Block> LIME_CHALK_SLAB = slab("lime_chalk_slab", chalkProps(MapColor.TERRACOTTA_LIGHT_GREEN));
  public static final DeferredBlock<Block> GREEN_CHALK_SLAB = slab("green_chalk_slab", chalkProps(MapColor.TERRACOTTA_GREEN));
  public static final DeferredBlock<Block> CYAN_CHALK_SLAB = slab("cyan_chalk_slab", chalkProps(MapColor.TERRACOTTA_CYAN));
  public static final DeferredBlock<Block> LIGHT_BLUE_CHALK_SLAB = slab("light_blue_chalk_slab", chalkProps(MapColor.TERRACOTTA_LIGHT_BLUE));
  public static final DeferredBlock<Block> BLUE_CHALK_SLAB = slab("blue_chalk_slab", chalkProps(MapColor.TERRACOTTA_BLUE));
  public static final DeferredBlock<Block> PURPLE_CHALK_SLAB = slab("purple_chalk_slab", chalkProps(MapColor.TERRACOTTA_PURPLE));
  public static final DeferredBlock<Block> MAGENTA_CHALK_SLAB = slab("magenta_chalk_slab", chalkProps(MapColor.TERRACOTTA_MAGENTA));
  public static final DeferredBlock<Block> PINK_CHALK_SLAB = slab("pink_chalk_slab", chalkProps(MapColor.TERRACOTTA_PINK));

  public static void init(IEventBus modBus) {
    BLOCKS.register(modBus);
    ITEMS.register(modBus);
  }

  public static void registerColoredBlocks() {
  }
}
