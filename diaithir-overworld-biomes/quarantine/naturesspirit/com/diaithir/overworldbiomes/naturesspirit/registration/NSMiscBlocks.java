package com.diaithir.overworldbiomes.naturesspirit.registration;

import com.diaithir.overworldbiomes.naturesspirit.NatureSpirit;
import com.diaithir.overworldbiomes.naturesspirit.blocks.*;
import com.diaithir.overworldbiomes.naturesspirit.blocks.block_entities.PizzaBlockEntity;
import com.diaithir.overworldbiomes.naturesspirit.datagen.NSConfiguredFeatures;
import com.diaithir.overworldbiomes.naturesspirit.items.AzollaItem;
import com.diaithir.overworldbiomes.naturesspirit.items.CheeseArrowItem;
import com.diaithir.overworldbiomes.naturesspirit.items.DesertTurnipItem;
import com.diaithir.overworldbiomes.naturesspirit.items.PizzaItem;
import com.diaithir.overworldbiomes.naturesspirit.registration.sets.FlowerSet;
import com.diaithir.overworldbiomes.naturesspirit.registration.sets.StoneSet;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.util.ColorRGBA;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.diaithir.overworldbiomes.naturesspirit.NatureSpirit.MOD_ID;
import static com.diaithir.overworldbiomes.naturesspirit.registration.NSRegistryHelper.*;

public class NSMiscBlocks {

  public static final Block RED_MOSS_BLOCK = registerBlock("red_moss_block",
      new RedMossBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(0.1F).sound(SoundType.MOSS).pushReaction(PushReaction.DESTROY)),
      Items.MOSS_BLOCK, CreativeModeTabs.NATURAL_BLOCKS);

  public static final Block RED_MOSS_CARPET = registerBlock("red_moss_carpet",
      new CarpetBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(0.1F).sound(SoundType.MOSS_CARPET).pushReaction(PushReaction.DESTROY)),
      Items.MOSS_CARPET, CreativeModeTabs.NATURAL_BLOCKS);

  public static final Block SANDY_SOIL = registerBlock("sandy_soil",
      new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).instrument(NoteBlockInstrument.BASEDRUM).strength(0.5F).sound(SoundType.GRAVEL)),
      Blocks.FARMLAND, CreativeModeTabs.NATURAL_BLOCKS);

  public static final Block PINK_SAND = registerBlock("pink_sand", new ColoredFallingBlock(new ColorRGBA(14331784),
      BlockBehaviour.Properties.of().mapColor(MapColor.RAW_IRON).instrument(NoteBlockInstrument.SNARE).strength(0.5F).sound(SoundType.SAND)),
      Blocks.RED_SANDSTONE, CreativeModeTabs.NATURAL_BLOCKS);

  public static final Block PINK_SANDSTONE = registerBlock("pink_sandstone",
      new Block(BlockBehaviour.Properties.of().mapColor(MapColor.SAND).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.8F)),
      PINK_SAND, CreativeModeTabs.NATURAL_BLOCKS, Blocks.CUT_RED_SANDSTONE_SLAB, CreativeModeTabs.BUILDING_BLOCKS);

  public static final Block PINK_SANDSTONE_STAIRS = registerBlock("pink_sandstone_stairs",
      new StairBlock(PINK_SANDSTONE.defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(PINK_SANDSTONE)),
      PINK_SANDSTONE, CreativeModeTabs.BUILDING_BLOCKS);

  public static final Block PINK_SANDSTONE_SLAB = registerBlock("pink_sandstone_slab",
      new SlabBlock(BlockBehaviour.Properties.of().mapColor(MapColor.SAND).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)),
      PINK_SANDSTONE_STAIRS, CreativeModeTabs.BUILDING_BLOCKS);

  public static final Block PINK_SANDSTONE_WALL = registerBlock("pink_sandstone_wall", new WallBlock(BlockBehaviour.Properties.ofFullCopy(PINK_SANDSTONE).forceSolidOn()),
      PINK_SANDSTONE_SLAB, CreativeModeTabs.BUILDING_BLOCKS);

  public static final Block CHISELED_PINK_SANDSTONE = registerBlock("chiseled_pink_sandstone",
      new Block(BlockBehaviour.Properties.of().mapColor(MapColor.SAND).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.8F)),
      PINK_SANDSTONE_WALL, CreativeModeTabs.BUILDING_BLOCKS);

  public static final Block SMOOTH_PINK_SANDSTONE = registerBlock("smooth_pink_sandstone",
      new Block(BlockBehaviour.Properties.of().mapColor(MapColor.SAND).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)),
      CHISELED_PINK_SANDSTONE, CreativeModeTabs.BUILDING_BLOCKS);

  public static final Block SMOOTH_PINK_SANDSTONE_STAIRS = registerBlock("smooth_pink_sandstone_stairs",
      new StairBlock(SMOOTH_PINK_SANDSTONE.defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(SMOOTH_PINK_SANDSTONE)),
      SMOOTH_PINK_SANDSTONE, CreativeModeTabs.BUILDING_BLOCKS);

  public static final Block SMOOTH_PINK_SANDSTONE_SLAB = registerBlock("smooth_pink_sandstone_slab", new SlabBlock(BlockBehaviour.Properties.ofFullCopy(SMOOTH_PINK_SANDSTONE)), SMOOTH_PINK_SANDSTONE_STAIRS, CreativeModeTabs.BUILDING_BLOCKS);

  public static final Block CUT_PINK_SANDSTONE = registerBlock("cut_pink_sandstone",
      new Block(BlockBehaviour.Properties.of().mapColor(MapColor.SAND).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.8F)),
      SMOOTH_PINK_SANDSTONE_SLAB, CreativeModeTabs.BUILDING_BLOCKS);

  public static final Block CUT_PINK_SANDSTONE_SLAB = registerBlock("cut_pink_sandstone_slab",
      new SlabBlock(BlockBehaviour.Properties.of().mapColor(MapColor.SAND).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)),
      CUT_PINK_SANDSTONE, CreativeModeTabs.BUILDING_BLOCKS);

  public static final Block TALL_FRIGID_GRASS = registerTallPlantBlock("tall_frigid_grass", new SemiTallGrassBlock(
          BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollision().replaceable().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ)
              .pushReaction(PushReaction.DESTROY)),
      Blocks.LARGE_FERN, 0.3f);

  public static final Block FRIGID_GRASS = registerPlantBlock("frigid_grass", new NSFernBlock(
          BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollision().replaceable().instabreak().sound(SoundType.GRASS)
              .offsetType(BlockBehaviour.OffsetType.XYZ).pushReaction(PushReaction.DESTROY), (DoublePlantBlock) TALL_FRIGID_GRASS),
      Blocks.FERN, 0.3f);

  public static final Block TALL_SCORCHED_GRASS = registerTallPlantBlock("tall_scorched_grass", new TallLargeDesertFernBlock(
          BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollision().replaceable().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ)
              .pushReaction(PushReaction.DESTROY)),
      Blocks.LARGE_FERN, 0.3f);

  public static final Block SCORCHED_GRASS = registerPlantBlock("scorched_grass", new LargeDesertFernBlock(
          BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollision().replaceable().instabreak().sound(SoundType.GRASS)
              .offsetType(BlockBehaviour.OffsetType.XYZ).pushReaction(PushReaction.DESTROY), (DoublePlantBlock) TALL_SCORCHED_GRASS),
      Blocks.FERN, 0.3f);

  public static final Block TALL_BEACH_GRASS = registerTallPlantBlock("tall_beach_grass", new TallLargeDesertFernBlock(
          BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).noCollision().replaceable().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ)
              .pushReaction(PushReaction.DESTROY)),
      Blocks.LARGE_FERN, 0.3f);

  public static final Block BEACH_GRASS = registerPlantBlock("beach_grass", new LargeDesertFernBlock(
          BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).noCollision().replaceable().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XYZ)
              .pushReaction(PushReaction.DESTROY), (DoublePlantBlock) TALL_BEACH_GRASS),
      Blocks.FERN, 0.3f);

  public static final Block TALL_SEDGE_GRASS = registerTallPlantBlock("tall_sedge_grass", new TallSedgeGrassBlock(
          BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).noCollision().replaceable().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ)
              .pushReaction(PushReaction.DESTROY)),
      TALL_SCORCHED_GRASS, 0.3f);

  public static final Block SEDGE_GRASS = registerPlantBlock("sedge_grass", new SedgeGrassBlock(
          BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).noCollision().replaceable().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XYZ)
              .pushReaction(PushReaction.DESTROY)),
      SCORCHED_GRASS, 0.3f);

  public static final Block LARGE_FLAXEN_FERN = registerTallPlantBlock("large_flaxen_fern", new DoublePlantBlock(
          BlockBehaviour.Properties.of().noCollision().replaceable().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ)
              .pushReaction(PushReaction.DESTROY)),
      TALL_SEDGE_GRASS, 0.3f);

  public static final Block FLAXEN_FERN = registerPlantBlock("flaxen_fern", new NSFernBlock(
          BlockBehaviour.Properties.of().noCollision().replaceable().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XYZ)
              .pushReaction(PushReaction.DESTROY), (DoublePlantBlock) LARGE_FLAXEN_FERN),
      SEDGE_GRASS, 0.3f);

  public static final Block TALL_OAT_GRASS = registerTallPlantBlock("tall_oat_grass", new SemiTallGrassBlock(
      BlockBehaviour.Properties.of().mapColor(MapColor.SAND).noCollision().replaceable().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ)
          .pushReaction(PushReaction.DESTROY)), LARGE_FLAXEN_FERN, 0.3f);

  public static final Block OAT_GRASS = registerPlantBlock("oat_grass", new NSFernBlock(
          BlockBehaviour.Properties.of().mapColor(MapColor.SAND).noCollision().replaceable().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XYZ)
              .pushReaction(PushReaction.DESTROY), (DoublePlantBlock) TALL_OAT_GRASS),
      FLAXEN_FERN, 0.3f);

  public static final Block LARGE_LUSH_FERN = registerTallPlantBlock("large_lush_fern", new DoublePlantBlock(
      BlockBehaviour.Properties.of().noCollision().replaceable().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ)
          .pushReaction(PushReaction.DESTROY)), TALL_OAT_GRASS, 0.3f);

  public static final Block LUSH_FERN = registerPlantBlock("lush_fern", new NSFernBlock(
      BlockBehaviour.Properties.of().noCollision().replaceable().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XYZ)
          .pushReaction(PushReaction.DESTROY), (DoublePlantBlock) LARGE_LUSH_FERN), OAT_GRASS, 0.3f);

  public static final Block TALL_MELIC_GRASS = registerTallPlantBlock("tall_melic_grass", new DoublePlantBlock(
      BlockBehaviour.Properties.of().mapColor(MapColor.GRASS).noCollision().replaceable().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ)
          .pushReaction(PushReaction.DESTROY)), LARGE_LUSH_FERN, 0.3f);

  public static final Block MELIC_GRASS = registerPlantBlock("melic_grass", new NSFernBlock(
      BlockBehaviour.Properties.of().mapColor(MapColor.GRASS).noCollision().replaceable().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XYZ)
          .pushReaction(PushReaction.DESTROY), (DoublePlantBlock) TALL_MELIC_GRASS), LUSH_FERN, 0.3f);

  public static final Block GREEN_BEARBERRIES = registerPlantBlock("green_bearberries", new BearberryBlock(
          BlockBehaviour.Properties.of().noCollision().replaceable().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XYZ)
              .pushReaction(PushReaction.DESTROY)),
      MELIC_GRASS, 0.3f);

  public static final Block RED_BEARBERRIES = registerPlantBlock("red_bearberries", new BearberryBlock(
          BlockBehaviour.Properties.of().noCollision().replaceable().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XYZ)
              .pushReaction(PushReaction.DESTROY)),
      GREEN_BEARBERRIES, 0.3f);

  public static final Block PURPLE_BEARBERRIES = registerPlantBlock("purple_bearberries", new BearberryBlock(
          BlockBehaviour.Properties.of().noCollision().replaceable().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XYZ)
              .pushReaction(PushReaction.DESTROY)),
      RED_BEARBERRIES, 0.3f);

  public static final Block GREEN_BITTER_SPROUTS = registerPlantBlock("green_bitter_sprouts", new LargeSproutsBlock(
          BlockBehaviour.Properties.of().noCollision().replaceable().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ)
              .pushReaction(PushReaction.DESTROY)),
      PURPLE_BEARBERRIES, 0.3f);

  public static final Block RED_BITTER_SPROUTS = registerPlantBlock("red_bitter_sprouts", new LargeSproutsBlock(
          BlockBehaviour.Properties.of().noCollision().replaceable().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ)
              .pushReaction(PushReaction.DESTROY)),
      GREEN_BITTER_SPROUTS, 0.3f);

  public static final Block PURPLE_BITTER_SPROUTS = registerPlantBlock("purple_bitter_sprouts", new LargeSproutsBlock(
      BlockBehaviour.Properties.of().noCollision().replaceable().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ)
          .pushReaction(PushReaction.DESTROY)), RED_BITTER_SPROUTS, 0.3f);

  public static final Block CATTAIL = registerPlantBlock("cattail", new CattailBlock(
          BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).noCollision().instabreak().sound(SoundType.GRASS).ignitedByLava().pushReaction(PushReaction.DESTROY)),
      Blocks.LARGE_FERN, 0.4f);

  public static final Block AZOLLA = NSRegistryHelper.registerTransparentBlockWithoutTab("azolla", new AzollaBlock(
      BlockBehaviour.Properties.of().mapColor(MapColor.GRASS).pushReaction(PushReaction.DESTROY).randomTicks().noOcclusion().instabreak().noCollision()
          .sound(SoundType.LILY_PAD)));

  public static final Item AZOLLA_ITEM = registerItem("azolla", new AzollaItem(AZOLLA, new Item.Properties()), Items.LILY_PAD, CreativeModeTabs.NATURAL_BLOCKS);

  public static final Block ORNATE_SUCCULENT = registerTransparentBlockWithoutTab("ornate_succulent",
      new SucculentBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).sound(SoundType.GRASS).noCollision().instabreak()));

  public static final Block DROWSY_SUCCULENT = registerTransparentBlockWithoutTab("drowsy_succulent",
      new SucculentBlock(BlockBehaviour.Properties.of().mapColor(MapColor.GRASS).sound(SoundType.GRASS).noCollision().instabreak()));

  public static final Block AUREATE_SUCCULENT = registerTransparentBlockWithoutTab("aureate_succulent",
      new SucculentBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).sound(SoundType.GRASS).noCollision().instabreak()));

  public static final Block SAGE_SUCCULENT = registerTransparentBlockWithoutTab("sage_succulent",
      new SucculentBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).sound(SoundType.GRASS).noCollision().instabreak()));

  public static final Block FOAMY_SUCCULENT = registerTransparentBlockWithoutTab("foamy_succulent",
      new SucculentBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).sound(SoundType.GRASS).noCollision().instabreak()));

  public static final Block IMPERIAL_SUCCULENT = registerTransparentBlockWithoutTab("imperial_succulent",
      new SucculentBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).sound(SoundType.GRASS).noCollision().instabreak()));

  public static final Block REGAL_SUCCULENT = registerTransparentBlockWithoutTab("regal_succulent",
      new SucculentBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).sound(SoundType.GRASS).noCollision().instabreak()));

  public static final Block ORNATE_WALL_SUCCULENT = registerTransparentBlockWithoutTab("ornate_wall_succulent",
      new SucculentWallBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).sound(SoundType.GRASS).noCollision().instabreak().overrideLootTable(ORNATE_SUCCULENT.getLootTable())));

  public static final Block DROWSY_WALL_SUCCULENT = registerTransparentBlockWithoutTab("drowsy_wall_succulent",
      new SucculentWallBlock(BlockBehaviour.Properties.of().mapColor(MapColor.GRASS).sound(SoundType.GRASS).noCollision().instabreak().overrideLootTable(DROWSY_SUCCULENT.getLootTable())));

  public static final Block AUREATE_WALL_SUCCULENT = registerTransparentBlockWithoutTab("aureate_wall_succulent",
      new SucculentWallBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).sound(SoundType.GRASS).noCollision().instabreak().overrideLootTable(AUREATE_SUCCULENT.getLootTable())));

  public static final Block SAGE_WALL_SUCCULENT = registerTransparentBlockWithoutTab("sage_wall_succulent",
      new SucculentWallBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).sound(SoundType.GRASS).noCollision().instabreak().overrideLootTable(SAGE_SUCCULENT.getLootTable())));

  public static final Block FOAMY_WALL_SUCCULENT = registerTransparentBlockWithoutTab("foamy_wall_succulent", new SucculentWallBlock(
      BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).sound(SoundType.GRASS).noCollision().instabreak().overrideLootTable(FOAMY_SUCCULENT.getLootTable())));

  public static final Block IMPERIAL_WALL_SUCCULENT = registerTransparentBlockWithoutTab("imperial_wall_succulent",
      new SucculentWallBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).sound(SoundType.GRASS).noCollision().instabreak().overrideLootTable(IMPERIAL_SUCCULENT.getLootTable())));

  public static final Block REGAL_WALL_SUCCULENT = registerTransparentBlockWithoutTab("regal_wall_succulent",
      new SucculentWallBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).sound(SoundType.GRASS).noCollision().instabreak().overrideLootTable(REGAL_SUCCULENT.getLootTable())));

  public static final Item ORNATE_SUCCULENT_ITEM = registerItem("ornate_succulent",
      new StandingAndWallBlockItem(ORNATE_SUCCULENT, ORNATE_WALL_SUCCULENT, Direction.DOWN, new Item.Properties()), Items.PITCHER_PLANT, CreativeModeTabs.NATURAL_BLOCKS);

  public static final Item DROWSY_SUCCULENT_ITEM = registerItem("drowsy_succulent",
      new StandingAndWallBlockItem(DROWSY_SUCCULENT, DROWSY_WALL_SUCCULENT, Direction.DOWN, new Item.Properties()), ORNATE_SUCCULENT_ITEM, CreativeModeTabs.NATURAL_BLOCKS);

  public static final Item AUREATE_SUCCULENT_ITEM = registerItem("aureate_succulent",
      new StandingAndWallBlockItem(AUREATE_SUCCULENT, AUREATE_WALL_SUCCULENT, Direction.DOWN, new Item.Properties()), DROWSY_SUCCULENT_ITEM, CreativeModeTabs.NATURAL_BLOCKS);

  public static final Item SAGE_SUCCULENT_ITEM = registerItem("sage_succulent",
      new StandingAndWallBlockItem(SAGE_SUCCULENT, SAGE_WALL_SUCCULENT, Direction.DOWN, new Item.Properties()), AUREATE_SUCCULENT_ITEM, CreativeModeTabs.NATURAL_BLOCKS);

  public static final Item FOAMY_SUCCULENT_ITEM = registerItem("foamy_succulent",
      new StandingAndWallBlockItem(FOAMY_SUCCULENT, FOAMY_WALL_SUCCULENT, Direction.DOWN, new Item.Properties()), SAGE_SUCCULENT_ITEM, CreativeModeTabs.NATURAL_BLOCKS);

  public static final Item IMPERIAL_SUCCULENT_ITEM = registerItem("imperial_succulent",
      new StandingAndWallBlockItem(IMPERIAL_SUCCULENT, IMPERIAL_WALL_SUCCULENT, Direction.DOWN, new Item.Properties()), FOAMY_SUCCULENT_ITEM, CreativeModeTabs.NATURAL_BLOCKS);

  public static final Item REGAL_SUCCULENT_ITEM = registerItem("regal_succulent",
      new StandingAndWallBlockItem(REGAL_SUCCULENT, REGAL_WALL_SUCCULENT, Direction.DOWN, new Item.Properties()), IMPERIAL_SUCCULENT_ITEM, CreativeModeTabs.NATURAL_BLOCKS);

  public static final Block SHIITAKE_MUSHROOM = registerPlantBlock("shiitake_mushroom", new ShiitakeMushroomPlantBlock(
      BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).noCollision().randomTicks().instabreak().sound(SoundType.GRASS).lightLevel((state) -> 1)
          .hasPostProcess(Blocks::always).pushReaction(PushReaction.DESTROY), NSConfiguredFeatures.HUGE_SHIITAKE_MUSHROOM), Blocks.RED_MUSHROOM, 0.1F);

  public static final Block SHIITAKE_MUSHROOM_BLOCK = registerBlock("shiitake_mushroom_block",
      new HugeMushroomBlock(BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).instrument(NoteBlockInstrument.BASS).strength(0.2F).sound(SoundType.WOOD).ignitedByLava()),

      Blocks.RED_MUSHROOM_BLOCK, CreativeModeTabs.NATURAL_BLOCKS);

  public static final Block GRAY_POLYPORE = registerPlantBlock("gray_polypore", new PolyporeBlock(
      BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).noCollision().randomTicks().instabreak().sound(SoundType.GRASS).lightLevel((state) -> 1)
          .hasPostProcess(Blocks::always).pushReaction(PushReaction.DESTROY), NSConfiguredFeatures.GRAY_POLYPORE), SHIITAKE_MUSHROOM, 0.1F);

  public static final Block GRAY_POLYPORE_BLOCK = registerBlock("gray_polypore_block", new HugeMushroomBlock(
          BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_LIGHT_GRAY).instrument(NoteBlockInstrument.BASS).strength(0.2F).sound(SoundType.WOOD).ignitedByLava()),

      SHIITAKE_MUSHROOM_BLOCK, CreativeModeTabs.NATURAL_BLOCKS);


  public static final Block POTTED_SCORCHED_GRASS = registerTransparentBlockWithoutTab("potted_scorched_grass",
      new FlowerPotBlock(SCORCHED_GRASS, BlockBehaviour.Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY)));

  public static final Block POTTED_BEACH_GRASS = registerTransparentBlockWithoutTab("potted_beach_grass",
      new FlowerPotBlock(BEACH_GRASS, BlockBehaviour.Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY)));

  public static final Block POTTED_SEDGE_GRASS = registerTransparentBlockWithoutTab("potted_sedge_grass",
      new FlowerPotBlock(SEDGE_GRASS, BlockBehaviour.Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY)));

  public static final Block POTTED_FLAXEN_FERN = registerTransparentBlockWithoutTab("potted_flaxen_fern",
      new FlowerPotBlock(FLAXEN_FERN, BlockBehaviour.Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY)));

  public static final Block POTTED_OAT_GRASS = registerTransparentBlockWithoutTab("potted_oat_grass",
      new FlowerPotBlock(OAT_GRASS, BlockBehaviour.Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY)));

  public static final Block POTTED_MELIC_GRASS = registerTransparentBlockWithoutTab("potted_melic_grass",
      new FlowerPotBlock(MELIC_GRASS, BlockBehaviour.Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY)));

  public static final Block POTTED_LUSH_FERN = registerTransparentBlockWithoutTab("potted_lush_fern",
      new FlowerPotBlock(LUSH_FERN, BlockBehaviour.Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY)));

  public static final Block POTTED_FRIGID_GRASS = registerTransparentBlockWithoutTab("potted_frigid_grass",
      new FlowerPotBlock(FRIGID_GRASS, BlockBehaviour.Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY)));

  public static final Block POTTED_GREEN_BEARBERRIES = registerTransparentBlockWithoutTab("potted_green_bearberries",
      new FlowerPotBlock(GREEN_BEARBERRIES, BlockBehaviour.Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY)));

  public static final Block POTTED_RED_BEARBERRIES = registerTransparentBlockWithoutTab("potted_red_bearberries",
      new FlowerPotBlock(RED_BEARBERRIES, BlockBehaviour.Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY)));

  public static final Block POTTED_PURPLE_BEARBERRIES = registerTransparentBlockWithoutTab("potted_purple_bearberries",
      new FlowerPotBlock(PURPLE_BEARBERRIES, BlockBehaviour.Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY)));

  public static final Block POTTED_ORNATE_SUCCULENT = registerTransparentBlockWithoutTab("potted_ornate_succulent",
      new FlowerPotBlock(ORNATE_SUCCULENT, BlockBehaviour.Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY)));

  public static final Block POTTED_DROWSY_SUCCULENT = registerTransparentBlockWithoutTab("potted_drowsy_succulent",
      new FlowerPotBlock(DROWSY_SUCCULENT, BlockBehaviour.Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY)));

  public static final Block POTTED_AUREATE_SUCCULENT = registerTransparentBlockWithoutTab("potted_aureate_succulent",
      new FlowerPotBlock(AUREATE_SUCCULENT, BlockBehaviour.Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY)));

  public static final Block POTTED_SAGE_SUCCULENT = registerTransparentBlockWithoutTab("potted_sage_succulent",
      new FlowerPotBlock(SAGE_SUCCULENT, BlockBehaviour.Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY)));

  public static final Block POTTED_FOAMY_SUCCULENT = registerTransparentBlockWithoutTab("potted_foamy_succulent",
      new FlowerPotBlock(FOAMY_SUCCULENT, BlockBehaviour.Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY)));

  public static final Block POTTED_IMPERIAL_SUCCULENT = registerTransparentBlockWithoutTab("potted_imperial_succulent",
      new FlowerPotBlock(IMPERIAL_SUCCULENT, BlockBehaviour.Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY)));

  public static final Block POTTED_REGAL_SUCCULENT = registerTransparentBlockWithoutTab("potted_regal_succulent",
      new FlowerPotBlock(REGAL_SUCCULENT, BlockBehaviour.Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY)));

  public static final Block POTTED_SHIITAKE_MUSHROOM = registerTransparentBlockWithoutTab("potted_shiitake_mushroom",
      new FlowerPotBlock(SHIITAKE_MUSHROOM, BlockBehaviour.Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY)));


  public static final FlowerSet LAVENDER = new FlowerSet("lavender", Items.PURPLE_DYE, Items.PEONY, FlowerSet.FlowerPreset.BIG_TALL);

  public static final FlowerSet BLEEDING_HEART = new FlowerSet("bleeding_heart", Items.PINK_DYE, LAVENDER.getFlowerBlock().asItem(), FlowerSet.FlowerPreset.BIG_TALL);

  public static final FlowerSet BLUE_BULBS = new FlowerSet("blue_bulbs", Items.BLUE_DYE, BLEEDING_HEART.getFlowerBlock().asItem(), FlowerSet.FlowerPreset.BIG_TALL);

  public static final FlowerSet CARNATION = new FlowerSet("carnation", Items.RED_DYE, BLUE_BULBS.getFlowerBlock().asItem(), FlowerSet.FlowerPreset.BIG_TALL);

  public static final FlowerSet GARDENIA = new FlowerSet("gardenia", Items.WHITE_DYE, CARNATION.getFlowerBlock().asItem(), FlowerSet.FlowerPreset.TALL);

  public static final FlowerSet SNAPDRAGON = new FlowerSet("snapdragon", Items.PINK_DYE, GARDENIA.getFlowerBlock().asItem(), FlowerSet.FlowerPreset.TALL);

  public static final FlowerSet FOXGLOVE = new FlowerSet("foxglove", Items.PURPLE_DYE, SNAPDRAGON.getFlowerBlock().asItem(), FlowerSet.FlowerPreset.TALL);
  public static final FlowerSet BEGONIA = new FlowerSet("begonia", Items.ORANGE_DYE, FOXGLOVE.getFlowerBlock().asItem(), FlowerSet.FlowerPreset.TALL);

  public static final FlowerSet MARIGOLD = new FlowerSet("marigold", Items.ORANGE_DYE, MobEffects.FIRE_RESISTANCE, Items.LILY_OF_THE_VALLEY, FlowerSet.FlowerPreset.BIG_SMALL);

  public static final FlowerSet BLUEBELL = new FlowerSet("bluebell", Items.BLUE_DYE, MobEffects.HASTE, MARIGOLD.getFlowerBlock().asItem(), FlowerSet.FlowerPreset.BIG_SMALL);

  public static final FlowerSet TIGER_LILY = new FlowerSet("tiger_lily", Items.ORANGE_DYE, MobEffects.FIRE_RESISTANCE, BLUEBELL.getFlowerBlock().asItem(), FlowerSet.FlowerPreset.BIG_SMALL);

  public static final FlowerSet PURPLE_WILDFLOWER = new FlowerSet("purple_wildflower", Items.PURPLE_DYE, MobEffects.SLOW_FALLING, TIGER_LILY.getFlowerBlock().asItem(), FlowerSet.FlowerPreset.BIG_SMALL);

  public static final FlowerSet YELLOW_WILDFLOWER = new FlowerSet("yellow_wildflower", Items.YELLOW_DYE, MobEffects.SLOW_FALLING, PURPLE_WILDFLOWER.getFlowerBlock().asItem(), FlowerSet.FlowerPreset.BIG_SMALL);

  public static final FlowerSet RED_HEATHER = new FlowerSet("red_heather", Items.RED_DYE, MobEffects.FIRE_RESISTANCE, YELLOW_WILDFLOWER.getFlowerBlock().asItem(), FlowerSet.FlowerPreset.BIG_SMALL);

  public static final FlowerSet WHITE_HEATHER = new FlowerSet("white_heather", Items.WHITE_DYE, MobEffects.FIRE_RESISTANCE, RED_HEATHER.getFlowerBlock().asItem(), FlowerSet.FlowerPreset.BIG_SMALL);

  public static final FlowerSet PURPLE_HEATHER = new FlowerSet("purple_heather", Items.PURPLE_DYE, MobEffects.FIRE_RESISTANCE, WHITE_HEATHER.getFlowerBlock().asItem(), FlowerSet.FlowerPreset.BIG_SMALL);

  public static final FlowerSet ANEMONE = new FlowerSet("anemone", Items.MAGENTA_DYE, MobEffects.RESISTANCE, PURPLE_HEATHER.getFlowerBlock().asItem(), FlowerSet.FlowerPreset.MID_SMALL);

  public static final FlowerSet DWARF_BLOSSOMS = new FlowerSet("dwarf_blossoms", Items.PINK_DYE, MobEffects.RESISTANCE, ANEMONE.getFlowerBlock().asItem(), FlowerSet.FlowerPreset.MID_SMALL);

  public static final FlowerSet PROTEA = new FlowerSet("protea", Items.PINK_DYE, MobEffects.WATER_BREATHING, DWARF_BLOSSOMS.getFlowerBlock().asItem(), FlowerSet.FlowerPreset.MID_SMALL);

  public static final FlowerSet HIBISCUS = new FlowerSet("hibiscus", Items.RED_DYE, MobEffects.LUCK, PROTEA.getFlowerBlock().asItem(), FlowerSet.FlowerPreset.SMALL);

  public static final FlowerSet BLUE_IRIS = new FlowerSet("blue_iris", Items.LIGHT_BLUE_DYE, MobEffects.STRENGTH, HIBISCUS.getFlowerBlock().asItem(), FlowerSet.FlowerPreset.SMALL);

  public static final FlowerSet BLACK_IRIS = new FlowerSet("black_iris", Items.BLACK_DYE, MobEffects.STRENGTH, BLUE_IRIS.getFlowerBlock().asItem(), FlowerSet.FlowerPreset.SMALL);

  public static final FlowerSet RUBY_BLOSSOMS = new FlowerSet("ruby_blossoms", Items.RED_DYE, MobEffects.JUMP_BOOST, BLACK_IRIS.getFlowerBlock().asItem(), FlowerSet.FlowerPreset.BIG_SMALL);

  public static final FlowerSet SILVERBUSH = new FlowerSet("silverbush", RUBY_BLOSSOMS.getFlowerBlock().asItem(), FlowerSet.FlowerPreset.BIG_TALL);


  public static final Block HELVOLA = registerTransparentBlockWithoutTab("helvola", new WaterFlowerbedBlock(
      BlockBehaviour.Properties.of().pushReaction(PushReaction.DESTROY).randomTicks().noOcclusion().instabreak().friction(0.8F).sound(SoundType.LILY_PAD)));

  public static final Item HELVOLA_PAD_ITEM = registerItem("helvola_pad", new PlaceOnWaterBlockItem(HELVOLA, new Item.Properties()),
      Items.LILY_PAD, CreativeModeTabs.NATURAL_BLOCKS);

  public static final Item HELVOLA_FLOWER_ITEM = registerItem("helvola_flower", new Item(new Item.Properties()),
      HELVOLA_PAD_ITEM, CreativeModeTabs.NATURAL_BLOCKS);

  public static final Block LOTUS_FLOWER = registerTransparentBlockWithoutTab("lotus_flower", new LotusFlowerBlock(
      BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).pushReaction(PushReaction.DESTROY).randomTicks().noOcclusion().instabreak().sound(SoundType.LILY_PAD)));

  public static final Item LOTUS_FLOWER_ITEM = registerItem("lotus_flower", new PlaceOnWaterBlockItem(LOTUS_FLOWER, new Item.Properties()),
      HELVOLA_FLOWER_ITEM, CreativeModeTabs.NATURAL_BLOCKS);

  public static final Block LOTUS_STEM = registerPlantBlock("lotus_stem", new LotusStemBlock(
      BlockBehaviour.Properties.of().mapColor(MapColor.GRASS).pushReaction(PushReaction.DESTROY).noCollision().noOcclusion().instabreak().sound(SoundType.LILY_PAD), LOTUS_FLOWER), LOTUS_FLOWER, 0.2F);

  public static final Block ALLUAUDIA = registerPlantBlock("alluaudia", new GrowingBranchingTrunkBlock(
      BlockBehaviour.Properties.of().pushReaction(PushReaction.DESTROY).mapColor(MapColor.GRASS).noOcclusion().sound(SoundType.VINE).strength(.5f).forceSolidOff()), Items.CACTUS, .2f);

  public static final Block STRIPPED_ALLUAUDIA = registerPlantBlock("stripped_alluaudia", new GrowingBranchingTrunkBlock(
      BlockBehaviour.Properties.of().pushReaction(PushReaction.DESTROY).mapColor(MapColor.WOOD).noOcclusion().sound(SoundType.VINE).strength(.5f).forceSolidOff()), NSMiscBlocks.ALLUAUDIA, .2f);

  public static final Block ALLUAUDIA_BUNDLE = registerPlantBlock("alluaudia_bundle",
      new RotatedPillarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.GRASS).noOcclusion().sound(SoundType.VINE).strength(.6f)), STRIPPED_ALLUAUDIA, .2f);

  public static final Block STRIPPED_ALLUAUDIA_BUNDLE = registerPlantBlock("stripped_alluaudia_bundle",
      new RotatedPillarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).noOcclusion().sound(SoundType.VINE).strength(.6f)), ALLUAUDIA_BUNDLE, .2f);

  public static final FoodProperties OLIVE_COMPONENT = (new FoodProperties.Builder()).nutrition(2).saturationModifier(0.4F).build();

  public static final Item OLIVES = registerPlantItem("olives", new Item(new Item.Properties().food(OLIVE_COMPONENT)), Items.BEETROOT, CreativeModeTabs.FOOD_AND_DRINKS, 0.3F);

  public static final Block DESERT_TURNIP_ROOT_BLOCK = registerBlock("desert_turnip_root_block",
      new RotatedPillarBlock(BlockBehaviour.Properties.of().ignitedByLava().instrument(NoteBlockInstrument.BASS).mapColor(MapColor.PODZOL).strength(1.0F).sound(SoundType.ROOTS)),
      Blocks.SHROOMLIGHT, CreativeModeTabs.NATURAL_BLOCKS);

  public static final Block DESERT_TURNIP_BLOCK = registerBlock("desert_turnip_block", new DesertTurnipBlock(
      BlockBehaviour.Properties.of().ignitedByLava().instrument(NoteBlockInstrument.BASS).mapColor(MapColor.COLOR_LIGHT_GRAY).strength(1.0F).sound(SoundType.ROOTS)),
      NSMiscBlocks.DESERT_TURNIP_ROOT_BLOCK, CreativeModeTabs.NATURAL_BLOCKS);

  public static final Block DESERT_TURNIP_STEM = registerTransparentBlockWithoutTab("desert_turnip_stem",
      new DesertTurnipStemBlock((DesertTurnipBlock) DESERT_TURNIP_BLOCK, DESERT_TURNIP_ROOT_BLOCK,
          BlockBehaviour.Properties.of().noCollision().instabreak().randomTicks().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)));

  public static final FoodProperties DESERT_TURNIP_FOOD_COMPONENT = (new FoodProperties.Builder()).nutrition(2).saturationModifier(0.3F).build();

  public static final Item DESERT_TURNIP = registerItem("desert_turnip", new DesertTurnipItem(DESERT_TURNIP_STEM, (new Item.Properties()).food(DESERT_TURNIP_FOOD_COMPONENT)), Items.BEETROOT, CreativeModeTabs.FOOD_AND_DRINKS);

  public static final Item CHALK_POWDER = registerItem("chalk_powder", new Item((new Item.Properties())), Items.HONEYCOMB, CreativeModeTabs.INGREDIENTS);

  public static final Block CHEESE_BLOCK = registerBlockWithoutTab("cheese_block", new CheeseBlock(BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(2.0F, 1.0F).sound(SoundType.AZALEA_LEAVES)));

  public static final Item CHEESE_BUCKET = registerItem("cheese_bucket", new SolidBucketItem(CHEESE_BLOCK, SoundEvents.BUCKET_EMPTY, (new Item.Properties()).stacksTo(1).craftRemainder(Items.BUCKET)), Items.MILK_BUCKET, CreativeModeTabs.FOOD_AND_DRINKS);

  public static final Item CHEESE_ARROW = registerItem("cheese_arrow", new CheeseArrowItem(new Item.Properties()), Items.SPECTRAL_ARROW, CreativeModeTabs.COMBAT);

  public static final Block MILK_CAULDRON = registerBlockWithoutTab("milk_cauldron", new MilkCauldronBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAULDRON).overrideLootTable(Blocks.CAULDRON.getLootTable())));

  public static final Block CHEESE_CAULDRON = registerBlockWithoutTab("cheese_cauldron", new CheeseCauldronBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAULDRON).overrideLootTable(Blocks.CAULDRON.getLootTable())));

  public static final FoodProperties STANDARD_PIZZA_COMPONENT = (new FoodProperties.Builder()).nutrition(2).saturationModifier(0.2F).build();

  public static final Block PIZZA_BLOCK = registerBlockWithoutTab("pizza_block", new PizzaBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE)));

  public static final BlockEntityType<PizzaBlockEntity> PIZZA_BLOCK_ENTITY_TYPE = registerBlockEntity("pizza_block_entity", BlockEntityType.Builder.of(PizzaBlockEntity::new, PIZZA_BLOCK));

  public static final Item WHOLE_PIZZA = registerItem("whole_pizza", new PizzaItem(PIZZA_BLOCK, new Item.Properties().stacksTo(1).food(STANDARD_PIZZA_COMPONENT)), Items.BREAD, CreativeModeTabs.FOOD_AND_DRINKS);

  public static final Item THREE_QUARTERS_PIZZA = registerItem("three_quarters_pizza", new PizzaItem(PIZZA_BLOCK, new Item.Properties().stacksTo(1).food(STANDARD_PIZZA_COMPONENT)));

  public static final Item HALF_PIZZA = registerItem("half_pizza", new PizzaItem(PIZZA_BLOCK, new Item.Properties().stacksTo(1).food(STANDARD_PIZZA_COMPONENT)));

  public static final Item QUARTER_PIZZA = registerItem("quarter_pizza", new PizzaItem(PIZZA_BLOCK, new Item.Properties().stacksTo(1).food(STANDARD_PIZZA_COMPONENT)));

  public static final Item CALCITE_SHARD = registerItem("calcite_shard", new Item(new Item.Properties()), Items.AMETHYST_SHARD, CreativeModeTabs.INGREDIENTS);

  public static final Block CALCITE_CLUSTER = registerBlock("calcite_cluster", new AmethystClusterBlock(7, 3,
      BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).forceSolidOn().noOcclusion().randomTicks().sound(SoundType.CALCITE).strength(1.5F)
          .pushReaction(PushReaction.DESTROY)), Blocks.AMETHYST_CLUSTER, CreativeModeTabs.NATURAL_BLOCKS);

  public static final Block LARGE_CALCITE_BUD = registerBlock("large_calcite_bud",
      new AmethystClusterBlock(4, 3, BlockBehaviour.Properties.ofFullCopy(CALCITE_CLUSTER).sound(SoundType.CALCITE).forceSolidOn().pushReaction(PushReaction.DESTROY)),
      CALCITE_CLUSTER, CreativeModeTabs.NATURAL_BLOCKS);

  public static final Block SMALL_CALCITE_BUD = registerBlock("small_calcite_bud",
      new AmethystClusterBlock(3, 4, BlockBehaviour.Properties.ofFullCopy(CALCITE_CLUSTER).sound(SoundType.CALCITE).forceSolidOn().pushReaction(PushReaction.DESTROY)),
      LARGE_CALCITE_BUD, CreativeModeTabs.NATURAL_BLOCKS);

  public static final StoneSet TRAVERTINE = new StoneSet(Identifier.fromNamespaceAndPath(MOD_ID, "travertine"), MapColor.COLOR_LIGHT_GRAY, Items.POLISHED_ANDESITE_SLAB, Items.ANDESITE, 1.5F, true, true,
      true, true);

  public static final StoneSet CHERT = new StoneSet(Identifier.fromNamespaceAndPath(MOD_ID, "chert"), MapColor.WOOD, TRAVERTINE.getTilesSlab().asItem(), TRAVERTINE.getBase().asItem(), .9F, false,
      true, false, true, true);

  public static final Block CHERT_GOLD_ORE = registerBlock("chert_gold_ore",
      new DropExperienceBlock(ConstantInt.of(0), BlockBehaviour.Properties.ofLegacyCopy(Blocks.GOLD_ORE).mapColor(MapColor.WOOD).strength(.9f)), Items.GOLD_ORE, CreativeModeTabs.NATURAL_BLOCKS);

  public static final Block CHERT_IRON_ORE = registerBlock("chert_iron_ore",
      new DropExperienceBlock(ConstantInt.of(0), BlockBehaviour.Properties.ofLegacyCopy(Blocks.IRON_ORE).mapColor(MapColor.WOOD).strength(.9f)), Items.IRON_ORE, CreativeModeTabs.NATURAL_BLOCKS);

  public static final Block CHERT_COAL_ORE = registerBlock("chert_coal_ore",
      new DropExperienceBlock(UniformInt.of(0, 2), BlockBehaviour.Properties.ofLegacyCopy(Blocks.COAL_ORE).mapColor(MapColor.WOOD).strength(.9f)), Items.COAL_ORE, CreativeModeTabs.NATURAL_BLOCKS);

  public static final Block CHERT_LAPIS_ORE = registerBlock("chert_lapis_ore",
      new DropExperienceBlock(UniformInt.of(2, 5), BlockBehaviour.Properties.ofLegacyCopy(Blocks.LAPIS_ORE).mapColor(MapColor.WOOD).strength(.9f)), Items.LAPIS_ORE, CreativeModeTabs.NATURAL_BLOCKS);

  public static final Block CHERT_DIAMOND_ORE = registerBlock("chert_diamond_ore",
      new DropExperienceBlock(UniformInt.of(3, 7), BlockBehaviour.Properties.ofLegacyCopy(Blocks.DIAMOND_ORE).mapColor(MapColor.WOOD).strength(.9f)), Items.DIAMOND_ORE, CreativeModeTabs.NATURAL_BLOCKS);

  public static final Block CHERT_REDSTONE_ORE = registerBlock("chert_redstone_ore",
      new RedStoneOreBlock(BlockBehaviour.Properties.ofLegacyCopy(Blocks.REDSTONE_ORE).mapColor(MapColor.WOOD).strength(.6f)), Items.REDSTONE_ORE, CreativeModeTabs.NATURAL_BLOCKS);

  public static final Block CHERT_EMERALD_ORE = registerBlock("chert_emerald_ore",
      new DropExperienceBlock(UniformInt.of(3, 7), BlockBehaviour.Properties.ofLegacyCopy(Blocks.EMERALD_ORE).mapColor(MapColor.WOOD).strength(.6f)), Items.EMERALD_ORE, CreativeModeTabs.NATURAL_BLOCKS);

  public static final Block CHERT_COPPER_ORE = registerBlock("chert_copper_ore",
      new DropExperienceBlock(ConstantInt.of(0), BlockBehaviour.Properties.ofLegacyCopy(Blocks.COPPER_ORE).mapColor(MapColor.WOOD).strength(.6f)), Items.COPPER_ORE, CreativeModeTabs.NATURAL_BLOCKS);


  public static final BlockSetType PAPER_BLOCK_SET = BlockSetType.register(new BlockSetType(MOD_ID + ":paper", true, true, true, BlockSetType.PressurePlateSensitivity.EVERYTHING,
      SoundType.CHERRY_WOOD, SoundEvents.CHERRY_WOOD_DOOR_CLOSE, SoundEvents.CHERRY_WOOD_DOOR_OPEN, SoundEvents.CHERRY_WOOD_TRAPDOOR_CLOSE, SoundEvents.CHERRY_WOOD_TRAPDOOR_OPEN,
      SoundEvents.CHERRY_WOOD_PRESSURE_PLATE_CLICK_OFF, SoundEvents.CHERRY_WOOD_PRESSURE_PLATE_CLICK_ON, SoundEvents.CHERRY_WOOD_BUTTON_CLICK_OFF, SoundEvents.CHERRY_WOOD_BUTTON_CLICK_ON));

  public static final WoodType PAPER_WOOD_TYPE = WoodType.register(new WoodType(MOD_ID + ":paper", PAPER_BLOCK_SET));

  public static final Block PAPER_BLOCK = registerBlock("paper_block", new Block(BlockBehaviour.Properties.ofFullCopy(NSWoods.SUGI.getPlanks())), Items.WARPED_BUTTON, CreativeModeTabs.BUILDING_BLOCKS);

  public static final Block PAPER_PANEL = registerBlock("paper_panel", new IronBarsBlock(BlockBehaviour.Properties.ofFullCopy(NSWoods.SUGI.getPlanks()).noOcclusion()), PAPER_BLOCK, CreativeModeTabs.BUILDING_BLOCKS);

  public static final Block PAPER_DOOR = registerTransparentBlock("paper_door", new DoorBlock(PAPER_BLOCK_SET, BlockBehaviour.Properties.ofFullCopy(NSWoods.SUGI.getDoor())), PAPER_PANEL, CreativeModeTabs.BUILDING_BLOCKS);

  public static final Block PAPER_TRAPDOOR = registerTransparentBlock("paper_trapdoor", new TrapDoorBlock(PAPER_BLOCK_SET,
      BlockBehaviour.Properties.of().ignitedByLava().instrument(NoteBlockInstrument.BASS).strength(3.0f).sound(SoundType.WOOD).isValidSpawn(NSRegistryHelper::never)
          .noOcclusion()), PAPER_DOOR, CreativeModeTabs.BUILDING_BLOCKS);

  public static final Block FRAMED_PAPER_BLOCK = registerBlock("framed_paper_block", new Block(BlockBehaviour.Properties.ofFullCopy(NSWoods.SUGI.getPlanks())), PAPER_TRAPDOOR, CreativeModeTabs.BUILDING_BLOCKS);

  public static final Block FRAMED_PAPER_PANEL = registerBlock("framed_paper_panel", new IronBarsBlock(BlockBehaviour.Properties.ofFullCopy(NSWoods.SUGI.getPlanks()).noOcclusion()), FRAMED_PAPER_BLOCK, CreativeModeTabs.BUILDING_BLOCKS);

  public static final Block FRAMED_PAPER_DOOR = registerTransparentBlock("framed_paper_door", new DoorBlock(PAPER_BLOCK_SET, BlockBehaviour.Properties.ofFullCopy(NSWoods.SUGI.getDoor())), FRAMED_PAPER_PANEL, CreativeModeTabs.BUILDING_BLOCKS);

  public static final Block FRAMED_PAPER_TRAPDOOR = registerTransparentBlock("framed_paper_trapdoor", new TrapDoorBlock(PAPER_BLOCK_SET,
      BlockBehaviour.Properties.of().ignitedByLava().instrument(NoteBlockInstrument.BASS).strength(3.0f).sound(SoundType.WOOD).isValidSpawn(NSRegistryHelper::never)
          .noOcclusion()), FRAMED_PAPER_DOOR, CreativeModeTabs.BUILDING_BLOCKS);

  public static final Block BLOOMING_PAPER_BLOCK = registerBlock("blooming_paper_block", new GlazedTerracottaBlock(BlockBehaviour.Properties.ofFullCopy(NSWoods.SUGI.getPlanks())), FRAMED_PAPER_TRAPDOOR, CreativeModeTabs.BUILDING_BLOCKS);

  public static final Block BLOOMING_PAPER_PANEL = registerBlock("blooming_paper_panel", new IronBarsBlock(BlockBehaviour.Properties.ofFullCopy(NSWoods.SUGI.getPlanks()).noOcclusion()), BLOOMING_PAPER_BLOCK, CreativeModeTabs.BUILDING_BLOCKS);

  public static final Block BLOOMING_PAPER_DOOR = registerTransparentBlock("blooming_paper_door",
      new DoorBlock(PAPER_BLOCK_SET, BlockBehaviour.Properties.ofFullCopy(NSWoods.SUGI.getDoor())), BLOOMING_PAPER_PANEL, CreativeModeTabs.BUILDING_BLOCKS);

  public static final Block BLOOMING_PAPER_TRAPDOOR = registerTransparentBlock("blooming_paper_trapdoor", new TrapDoorBlock(PAPER_BLOCK_SET,
      BlockBehaviour.Properties.of().ignitedByLava().instrument(NoteBlockInstrument.BASS).strength(3.0f).sound(SoundType.WOOD).isValidSpawn(NSRegistryHelper::never)
          .noOcclusion()), BLOOMING_PAPER_DOOR, CreativeModeTabs.BUILDING_BLOCKS);


  public static final Block PAPER_SIGN = registerBlockWithoutTab("paper_sign", new StandingSignBlock(PAPER_WOOD_TYPE, BlockBehaviour.Properties.ofFullCopy(NSWoods.SUGI.getSign())));

  public static final Block PAPER_WALL_SIGN = registerBlockWithoutTab("paper_wall_sign",
      new WallSignBlock(PAPER_WOOD_TYPE, BlockBehaviour.Properties.ofFullCopy(NSWoods.SUGI.getSign()).overrideLootTable(PAPER_SIGN.getLootTable())));

  public static final Block PAPER_HANGING_SIGN = registerBlockWithoutTab("paper_hanging_sign",
      new CeilingHangingSignBlock(PAPER_WOOD_TYPE, BlockBehaviour.Properties.ofFullCopy(NSWoods.SUGI.getHangingSign())));

  public static final Block PAPER_WALL_HANGING_SIGN = registerBlockWithoutTab("paper_wall_hanging_sign",
      new WallHangingSignBlock(PAPER_WOOD_TYPE, BlockBehaviour.Properties.ofFullCopy(NSWoods.SUGI.getHangingSign()).overrideLootTable(PAPER_HANGING_SIGN.getLootTable())));

  public static final Item PAPER_SIGN_ITEM = registerItem("paper_sign", new SignItem(PAPER_SIGN, PAPER_WALL_SIGN, new Item.Properties().stacksTo(16)), Items.WARPED_SIGN, CreativeModeTabs.FUNCTIONAL_BLOCKS);

  public static final Item PAPER_HANGING_SIGN_ITEM = registerItem("paper_hanging_sign",
      new HangingSignItem(PAPER_HANGING_SIGN, PAPER_WALL_HANGING_SIGN, new Item.Properties().stacksTo(16)), PAPER_SIGN_ITEM, CreativeModeTabs.FUNCTIONAL_BLOCKS);

  /** Flammability data (burn odds, spread chance) to be wired from a NeoForge event (FireBlock.setFlammable). */
  public static final Map<Block, int[]> FLAMMABLES = new LinkedHashMap<>();

  /** Compost chances to be wired from a NeoForge setup event (ComposterBlock.COMPOSTABLES). */
  public static final Map<net.minecraft.world.level.ItemLike, Float> COMPOSTABLES = new LinkedHashMap<>();

  /** Strippable block pairs (original -> stripped) to be wired from a NeoForge BlockToolModificationEvent. */
  public static final Map<Block, Block> STRIPPABLES = new LinkedHashMap<>();

  /** Blocks that must be added as valid blocks to the sign block entity type. */
  public static final java.util.List<Block> EXTRA_SIGN_BLOCKS = new java.util.ArrayList<>();

  /** Blocks that must be added as valid blocks to the hanging sign block entity type. */
  public static final java.util.List<Block> EXTRA_HANGING_SIGN_BLOCKS = new java.util.ArrayList<>();

  private static void flammable(Block block, int burn, int spread) {
    FLAMMABLES.put(block, new int[] {burn, spread});
  }

  private static void compost(net.minecraft.world.level.ItemLike item, float chance) {
    COMPOSTABLES.put(item, chance);
  }

  public static void registerMiscBlocks() {
    NSColoredBlocks.registerColoredBlocks();
    NatureSpirit.LOGGER.debug("Registering ModBlocks for " + MOD_ID);
    flammable(NSWoods.COCONUT_THATCH, 5, 20);
    flammable(NSWoods.COCONUT_THATCH_SLAB, 5, 20);
    flammable(NSWoods.COCONUT_THATCH_STAIRS, 5, 20);
    flammable(NSWoods.COCONUT_THATCH_CARPET, 5, 20);
    flammable(NSWoods.XERIC_THATCH, 5, 20);
    flammable(NSWoods.XERIC_THATCH_SLAB, 5, 20);
    flammable(NSWoods.XERIC_THATCH_STAIRS, 5, 20);
    flammable(NSWoods.XERIC_THATCH_CARPET, 5, 20);
    flammable(NSWoods.EVERGREEN_THATCH, 5, 20);
    flammable(NSWoods.EVERGREEN_THATCH_SLAB, 5, 20);
    flammable(NSWoods.EVERGREEN_THATCH_STAIRS, 5, 20);
    flammable(NSWoods.EVERGREEN_THATCH_CARPET, 5, 20);
    compost(NSWoods.COCONUT_THATCH, 0.65F);
    compost(NSWoods.COCONUT_THATCH_CARPET, 0.1F);
    compost(NSWoods.COCONUT_THATCH_STAIRS, 0.5F);
    compost(NSWoods.COCONUT_THATCH_SLAB, 0.3F);
    compost(NSWoods.XERIC_THATCH, 0.65F);
    compost(NSWoods.XERIC_THATCH_CARPET, 0.1F);
    compost(NSWoods.XERIC_THATCH_STAIRS, 0.5F);
    compost(NSWoods.XERIC_THATCH_SLAB, 0.3F);
    compost(NSWoods.EVERGREEN_THATCH, 0.65F);
    compost(NSWoods.EVERGREEN_THATCH_CARPET, 0.1F);
    compost(NSWoods.EVERGREEN_THATCH_STAIRS, 0.5F);
    compost(NSWoods.EVERGREEN_THATCH_SLAB, 0.3F);
    compost(RED_MOSS_BLOCK, 0.65F);
    compost(RED_MOSS_CARPET, 0.3F);
    compost(LOTUS_FLOWER_ITEM, 0.65F);
    compost(AZOLLA_ITEM, 0.3F);
    compost(HELVOLA_FLOWER_ITEM, 0.3F);
    compost(HELVOLA_PAD_ITEM, 0.3F);
    compost(DESERT_TURNIP, 0.3F);
    compost(DESERT_TURNIP_BLOCK, 0.65F);
    compost(DESERT_TURNIP_ROOT_BLOCK, 0.5F);
    compost(WHOLE_PIZZA, 1.0F);
    compost(THREE_QUARTERS_PIZZA, .85F);
    compost(HALF_PIZZA, .5F);
    compost(QUARTER_PIZZA, .35F);
    compost(AUREATE_SUCCULENT_ITEM, .65F);
    compost(DROWSY_SUCCULENT_ITEM, .65F);
    compost(FOAMY_SUCCULENT_ITEM, .65F);
    compost(SAGE_SUCCULENT_ITEM, .65F);
    compost(IMPERIAL_SUCCULENT_ITEM, .65F);
    compost(ORNATE_SUCCULENT_ITEM, .65F);
    compost(REGAL_SUCCULENT_ITEM, .65F);
    STRIPPABLES.put(ALLUAUDIA_BUNDLE, STRIPPED_ALLUAUDIA_BUNDLE);

    EXTRA_SIGN_BLOCKS.add(PAPER_SIGN);
    EXTRA_SIGN_BLOCKS.add(PAPER_WALL_SIGN);

    EXTRA_HANGING_SIGN_BLOCKS.add(PAPER_HANGING_SIGN);
    EXTRA_HANGING_SIGN_BLOCKS.add(PAPER_WALL_HANGING_SIGN);
  }
}
