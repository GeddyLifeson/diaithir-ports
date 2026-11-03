package com.diaithir.overworldbiomes.naturesspirit.registration.sets;

import com.google.common.collect.ImmutableList;
import com.diaithir.overworldbiomes.naturesspirit.NatureSpirit;
import com.diaithir.overworldbiomes.naturesspirit.blocks.*;
import com.diaithir.overworldbiomes.naturesspirit.registration.NSParticleTypes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class WoodSet {

  // Data exposed so the module can wire these from NeoForge events.
  public static final Map<Block, int[]> FLAMMABLES = new HashMap<>();
  public static final Map<Block, Float> COMPOSTABLES = new HashMap<>();
  public static final Map<Block, Block> STRIPPABLES = new HashMap<>();
  public static final Map<ItemLike, Integer> FUELS = new HashMap<>();
  public static final Map<String, Block[]> SAPLINGS = new HashMap<>();
  public static final Map<String, Block> LEAVES = new HashMap<>();
  public static final Map<String, Block> RENDER_LAYERS = new HashMap<>();
  public static final Map<String, WoodSet> WOODS = new HashMap<>();
  public static final List<Block> WANDERING_TRADER_SAPLINGS = new ArrayList<>();

  public static void addFlammable(Block block, int burn, int spread) {
    FLAMMABLES.put(block, new int[]{burn, spread});
  }

  public static void addCompostable(Block block, float chance) {
    COMPOSTABLES.put(block, chance);
  }

  public static void addStrippable(Block log, Block stripped) {
    STRIPPABLES.put(log, stripped);
  }

  public static void addFuel(ItemLike item, int ticks) {
    FUELS.put(item, ticks);
  }

  private final ItemLike leavesBefore;
  private final ItemLike saplingBefore;
  private final ItemLike logBefore;
  private final ItemLike signBefore;
  private final ItemLike boatBefore;
  private final ItemLike buttonBefore;
  private final List<Block> registeredBlocksList = new ArrayList<>();
  private final List<Item> registeredItemsList = new ArrayList<>();
  private final Identifier name;
  private final MapColor sideColor;
  private final MapColor topColor;
  private final WoodPreset woodPreset;
  private BlockSetType blockSetType;
  private WoodType woodType;
  private Block log;
  private Block strippedLog;
  private Block bundle;
  private Block strippedBundle;
  private Block wood;
  private Block strippedWood;
  private Block leaves;
  private Block frostyLeaves;
  private Block sapling;
  private Block pottedSapling;
  private Block redLeaves;
  private Block redSapling;
  private Block pottedRedSapling;
  private Block orangeLeaves;
  private Block orangeSapling;
  private Block pottedOrangeSapling;
  private Block yellowLeaves;
  private Block yellowSapling;
  private Block pottedYellowSapling;
  private Block blueLeaves;
  private Block partBlueLeaves;
  private Block blueSapling;
  private Block pottedBlueSapling;
  private Block purpleLeaves;
  private Block partPurpleLeaves;
  private Block purpleSapling;
  private Block pottedPurpleSapling;
  private Block pinkLeaves;
  private Block partPinkLeaves;
  private Block pinkSapling;
  private Block pottedPinkSapling;
  private Block whiteLeaves;
  private Block partWhiteLeaves;
  private Block whiteSapling;
  private Block pottedWhiteSapling;
  private Block vines;
  private Block vinesPlant;
  private Block blueVines;
  private Block purpleVines;
  private Block pinkVines;
  private Block whiteVines;
  private Block blueVinesPlant;
  private Block purpleVinesPlant;
  private Block pinkVinesPlant;
  private Block whiteVinesPlant;
  private Block planks;
  private Block stairs;
  private Block slab;
  private Block mosaic;
  private Block mosaicStairs;
  private Block mosaicSlab;
  private Block fence;
  private Block fenceGate;
  private Block pressurePlate;
  private Block button;
  private Block door;
  private Block trapDoor;
  private Block sign;
  private Block wallSign;
  private Block hangingSign;
  private Block hangingWallSign;
  private Item signItem;
  private Item hangingSignItem;
  private Item boatItem;
  private Item chestBoatItem;
  private TagKey<Item> itemLogsTag;
  private TagKey<Block> blockLogsTag;
  private final Supplier<Object> boatTypeSupplier;
  private final Optional<ResourceKey<ConfiguredFeature<?, ?>>> configuredFeature;
  private final Optional<ResourceKey<ConfiguredFeature<?, ?>>> configuredFeature2;
  private boolean hasLargeTree;
  private final boolean hasMosaic;
  private final List<Block> leavesList = new ArrayList<>();
  private final List<Block> saplingList = new ArrayList<>();

  private static BlockBehaviour.Properties leavesProps() {
    return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).strength(0.2F).randomTicks().sound(SoundType.GRASS).noOcclusion()
        .isValidSpawn((s, g, p, e) -> false).isSuffocating((s, g, p) -> false).isViewBlocking((s, g, p) -> false)
        .ignitedByLava().pushReaction(PushReaction.DESTROY).isRedstoneConductor((s, g, p) -> false);
  }

  private void registerWood() {
    blockSetType = createBlockSetType();
    woodType = createWoodType();

    log = woodPreset == WoodPreset.JOSHUA ? createJoshuaLog() : createLog();
    strippedLog = woodPreset == WoodPreset.JOSHUA ? createStrippedJoshuaLog() : createStrippedLog();

    if (woodPreset == WoodPreset.JOSHUA) {
      bundle = createBundle();
      strippedBundle = createStrippedBundle();
      addStrippable(bundle, strippedBundle);
      addFlammable(bundle, 5, 5);
      addFlammable(strippedBundle, 5, 5);
    } else {
      addStrippable(log, strippedLog);
    }

    if (woodPreset != WoodPreset.BAMBOO && woodPreset != WoodPreset.JOSHUA) {
      wood = createWood();
      strippedWood = createStrippedWood();
      addStrippable(wood, strippedWood);
    }

    if (this.hasDefaultLeaves()) {
      leaves = createLeaves();

      if (this.hasDefaultSapling()) {
        sapling = this.isSandy() ? createSandySapling() : createSapling();
        pottedSapling = createPottedSapling(sapling);
        SAPLINGS.put(getName(), new Block[]{sapling, pottedSapling});
        WANDERING_TRADER_SAPLINGS.add(sapling);
      }
    }

    if (woodPreset == WoodPreset.FROSTABLE) {
      frostyLeaves = createLeaves("frosty_");
      leaves = createFrostableLeaves();
      sapling = createSapling();
      pottedSapling = createPottedSapling(sapling);
      SAPLINGS.put(getName(), new Block[]{sapling, pottedSapling});
      WANDERING_TRADER_SAPLINGS.add(sapling);
    }

    if (woodPreset == WoodPreset.WILLOW) {
      vines = createVines(this::getVinesPlant);
      vinesPlant = createVinesPlant(vines);

      leaves = createVinesLeavesBlock(vinesPlant, vines);

      sapling = createSapling();
      pottedSapling = createPottedSapling(sapling);
      SAPLINGS.put(getName(), new Block[]{sapling, pottedSapling});
      WANDERING_TRADER_SAPLINGS.add(sapling);
    }
    if (woodPreset == WoodPreset.WISTERIA) {
      whiteVines = createVines("white_", this::getWhiteVinesPlant);
      blueVines = createVines("blue_", this::getBlueVinesPlant);
      pinkVines = createVines("pink_", this::getPinkVinesPlant);
      purpleVines = createVines("purple_", this::getPurpleVinesPlant);
      whiteVinesPlant = createVinesPlant("white_", whiteVines);
      blueVinesPlant = createVinesPlant("blue_", blueVines);
      pinkVinesPlant = createVinesPlant("pink_", pinkVines);
      purpleVinesPlant = createVinesPlant("purple_", purpleVines);
      whiteLeaves = createVinesLeavesBlock("white_", whiteVinesPlant, whiteVines);
      blueLeaves = createVinesLeavesBlock("blue_", blueVinesPlant, blueVines);
      pinkLeaves = createVinesLeavesBlock("pink_", pinkVinesPlant, pinkVines);
      purpleLeaves = createVinesLeavesBlock("purple_", purpleVinesPlant, purpleVines);
      partWhiteLeaves = createVinesLeavesBlock("part_white_", whiteVinesPlant, whiteVines);
      partBlueLeaves = createVinesLeavesBlock("part_blue_", blueVinesPlant, blueVines);
      partPinkLeaves = createVinesLeavesBlock("part_pink_", pinkVinesPlant, pinkVines);
      partPurpleLeaves = createVinesLeavesBlock("part_purple_", purpleVinesPlant, purpleVines);
      whiteSapling = createSapling("white_");
      blueSapling = createSapling("blue_");
      pinkSapling = createSapling("pink_");
      purpleSapling = createSapling("purple_");
      pottedWhiteSapling = createPottedSapling("white_", whiteSapling);
      pottedBlueSapling = createPottedSapling("blue_", blueSapling);
      pottedPinkSapling = createPottedSapling("pink_", pinkSapling);
      pottedPurpleSapling = createPottedSapling("purple_", purpleSapling);
      SAPLINGS.put("white_" + getName(), new Block[]{whiteSapling, pottedWhiteSapling});
      SAPLINGS.put("blue_" + getName(), new Block[]{blueSapling, pottedBlueSapling});
      SAPLINGS.put("pink_" + getName(), new Block[]{pinkSapling, pottedPinkSapling});
      SAPLINGS.put("purple_" + getName(), new Block[]{purpleSapling, pottedPurpleSapling});
      SAPLINGS.put("part_white_" + getName(), new Block[]{whiteSapling, pottedWhiteSapling});
      SAPLINGS.put("part_blue_" + getName(), new Block[]{blueSapling, pottedBlueSapling});
      SAPLINGS.put("part_pink_" + getName(), new Block[]{pinkSapling, pottedPinkSapling});
      SAPLINGS.put("part_purple_" + getName(), new Block[]{purpleSapling, pottedPurpleSapling});
      WANDERING_TRADER_SAPLINGS.add(whiteSapling);
      WANDERING_TRADER_SAPLINGS.add(blueSapling);
      WANDERING_TRADER_SAPLINGS.add(purpleSapling);
      WANDERING_TRADER_SAPLINGS.add(pinkSapling);
    }
    if (woodPreset == WoodPreset.MAPLE) {
      redLeaves = createParticleLeaves("red_", NSParticleTypes.RED_MAPLE_LEAVES_PARTICLE, 100);
      orangeLeaves = createParticleLeaves("orange_", NSParticleTypes.ORANGE_MAPLE_LEAVES_PARTICLE, 100);
      yellowLeaves = createParticleLeaves("yellow_", NSParticleTypes.YELLOW_MAPLE_LEAVES_PARTICLE, 100);
      redSapling = createSapling("red_");
      orangeSapling = createSapling("orange_");
      yellowSapling = createSapling("yellow_");
      pottedRedSapling = createPottedSapling("red_", redSapling);
      pottedOrangeSapling = createPottedSapling("orange_", orangeSapling);
      pottedYellowSapling = createPottedSapling("yellow_", yellowSapling);
      SAPLINGS.put("red_" + getName(), new Block[]{redSapling, pottedRedSapling});
      SAPLINGS.put("orange_" + getName(), new Block[]{orangeSapling, pottedOrangeSapling});
      SAPLINGS.put("yellow_" + getName(), new Block[]{yellowSapling, pottedYellowSapling});
      WANDERING_TRADER_SAPLINGS.add(redSapling);
      WANDERING_TRADER_SAPLINGS.add(orangeSapling);
      WANDERING_TRADER_SAPLINGS.add(yellowSapling);
    }
    if (woodPreset == WoodPreset.ASPEN) {
      leaves = createLeaves();
      yellowLeaves = createLeaves("yellow_");
      sapling = createSapling();
      pottedSapling = createPottedSapling(sapling);
      SAPLINGS.put(getName(), new Block[]{sapling, pottedSapling});
      WANDERING_TRADER_SAPLINGS.add(sapling);
    }
    if (this.hasMosaic()) {
      mosaic = createMosaic();
      mosaicStairs = createMosaicStairs();
      mosaicSlab = createMosaicSlab();
      addFlammable(mosaic, 5, 20);
      addFlammable(mosaicSlab, 5, 20);
      addFlammable(mosaicStairs, 5, 20);
      addFuel(mosaic, 300);
      addFuel(mosaicStairs, 300);
      addFuel(mosaicSlab, 150);
    }
    planks = createPlanks();
    stairs = createStairs();
    slab = createSlab();
    fence = createFence();
    fenceGate = createFenceGate();
    pressurePlate = createPressurePlate();
    button = createButton();
    door = createDoor();
    trapDoor = createTrapDoor();
    sign = createSign();
    wallSign = createWallSign();
    hangingSign = createHangingSign();
    hangingWallSign = createWallHangingSign();
    signItem = createSignItem();
    hangingSignItem = createHangingSignItem();

    // Boats: boat items and boat type registration are simplified in 26.1; boat items are not created here.
    boatItem = null;
    chestBoatItem = null;

    RENDER_LAYERS.put(getName() + "_door", door);
    RENDER_LAYERS.put(getName() + "_trapdoor", trapDoor);

    addFlammable(strippedLog, 5, 5);
    addFlammable(log, 5, 5);
    addFlammable(stairs, 5, 20);
    addFlammable(slab, 5, 20);
    addFlammable(planks, 5, 20);
    addFlammable(fence, 5, 20);
    addFlammable(fenceGate, 5, 20);

    addFuel(fence, 300);
    addFuel(fenceGate, 300);

    blockLogsTag = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(getModID(), getName() + "_logs"));
    itemLogsTag = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(getModID(), getName() + "_logs"));
  }

  public WoodSet(
      Identifier name,
      MapColor sideColor,
      MapColor topColor,
      ItemLike leavesBefore,
      ItemLike logBefore,
      ItemLike signBefore,
      ItemLike boatBefore,
      ItemLike buttonBefore,
      ItemLike saplingBefore,
      Supplier<Object> boatType,
      WoodPreset woodPreset,
      boolean hasMosaic,
      ResourceKey<ConfiguredFeature<?, ?>> configuredFeature
  ) {
    this.woodPreset = woodPreset;
    this.name = name;
    this.sideColor = sideColor;
    this.topColor = topColor;
    this.leavesBefore = leavesBefore;
    this.logBefore = logBefore;
    this.signBefore = signBefore;
    this.boatBefore = boatBefore;
    this.buttonBefore = buttonBefore;
    this.saplingBefore = saplingBefore;
    this.boatTypeSupplier = boatType;
    this.hasMosaic = hasMosaic;
    this.configuredFeature = Optional.of(configuredFeature);
    this.configuredFeature2 = Optional.empty();
    registerWood();
    WOODS.put(this.getName(), this);
  }

  public WoodSet(
      Identifier name,
      MapColor sideColor,
      MapColor topColor,
      ItemLike leavesBefore,
      ItemLike logBefore,
      ItemLike signBefore,
      ItemLike boatBefore,
      ItemLike buttonBefore,
      ItemLike saplingBefore,
      Supplier<Object> boatType,
      WoodPreset woodPreset,
      boolean hasMosaic,
      Optional<ResourceKey<ConfiguredFeature<?, ?>>> configuredFeature,
      Optional<ResourceKey<ConfiguredFeature<?, ?>>> configuredFeature2
  ) {
    this.woodPreset = woodPreset;
    this.name = name;
    this.sideColor = sideColor;
    this.topColor = topColor;
    this.leavesBefore = leavesBefore;
    this.logBefore = logBefore;
    this.signBefore = signBefore;
    this.boatBefore = boatBefore;
    this.buttonBefore = buttonBefore;
    this.saplingBefore = saplingBefore;
    this.boatTypeSupplier = boatType;
    this.hasMosaic = hasMosaic;
    this.configuredFeature = configuredFeature;
    this.configuredFeature2 = configuredFeature2;
    this.hasLargeTree = true;
    registerWood();
    WOODS.put(this.getName(), this);
  }

  public Identifier getNameID() {
    return name;
  }

  public String getName() {
    return name.getPath();
  }

  public String getModID() {
    return name.getNamespace();
  }

  public BlockSetType getBlockSetType() {
    return this.blockSetType;
  }

  public WoodPreset getWoodPreset() {
    return woodPreset;
  }

  public MapColor getTopColor() {
    return topColor;
  }

  public WoodType getWoodType() {
    return woodType;
  }

  public Block getButton() {
    return button;
  }

  public Block getFence() {
    return fence;
  }

  public Block getPlanks() {
    return planks;
  }

  public Block getSlab() {
    return slab;
  }

  public Block getFenceGate() {
    return fenceGate;
  }

  public Block getStairs() {
    return stairs;
  }

  public Block getDoor() {
    return door;
  }

  public Block getHangingSign() {
    return hangingSign;
  }

  public Block getHangingWallSign() {
    return hangingWallSign;
  }

  public Block getPressurePlate() {
    return pressurePlate;
  }

  public Block getSign() {
    return sign;
  }

  public Block getTrapDoor() {
    return trapDoor;
  }

  public Block getWallSign() {
    return wallSign;
  }

  public Item getHangingSignItem() {
    return hangingSignItem;
  }

  public Item getSignItem() {
    return signItem;
  }

  public Item getBoatItem() {
    return boatItem;
  }

  public Item getChestBoatItem() {
    return chestBoatItem;
  }

  public Block getLog() {
    return log;
  }

  public Block getStrippedLog() {
    return strippedLog;
  }

  public Block getBundle() {
    return bundle;
  }

  public Block getStrippedBundle() {
    return strippedBundle;
  }

  public Block getWood() {
    return wood;
  }

  public Block getStrippedWood() {
    return strippedWood;
  }

  public Block getMosaic() {
    return mosaic;
  }

  public Block getMosaicStairs() {
    return mosaicStairs;
  }

  public Block getMosaicSlab() {
    return mosaicSlab;
  }

  public Block getLeaves() {
    return leaves;
  }

  public Block getFrostyLeaves() {
    return frostyLeaves;
  }

  public Block getSapling() {
    return sapling;
  }

  public Block getPottedSapling() {
    return pottedSapling;
  }

  public Block getVines() {
    return vines;
  }

  public Block getVinesPlant() {
    return vinesPlant;
  }

  public Block getRedLeaves() {
    return redLeaves;
  }

  public Block getOrangeLeaves() {
    return orangeLeaves;
  }

  public Block getYellowLeaves() {
    return yellowLeaves;
  }

  public Block getBlueLeaves() {
    return blueLeaves;
  }

  public Block getPurpleLeaves() {
    return purpleLeaves;
  }

  public Block getPinkLeaves() {
    return pinkLeaves;
  }

  public Block getWhiteLeaves() {
    return whiteLeaves;
  }

  public Block getPottedRedSapling() {
    return pottedRedSapling;
  }

  public Block getPottedOrangeSapling() {
    return pottedOrangeSapling;
  }

  public Block getPottedYellowSapling() {
    return pottedYellowSapling;
  }

  public Block getPottedBlueSapling() {
    return pottedBlueSapling;
  }

  public Block getPottedPurpleSapling() {
    return pottedPurpleSapling;
  }

  public Block getPottedPinkSapling() {
    return pottedPinkSapling;
  }

  public Block getPottedWhiteSapling() {
    return pottedWhiteSapling;
  }

  public Block getRedSapling() {
    return redSapling;
  }

  public Block getOrangeSapling() {
    return orangeSapling;
  }

  public Block getYellowSapling() {
    return yellowSapling;
  }

  public Block getBlueSapling() {
    return blueSapling;
  }

  public Block getPurpleSapling() {
    return purpleSapling;
  }

  public Block getPinkSapling() {
    return pinkSapling;
  }

  public Block getWhiteSapling() {
    return whiteSapling;
  }

  public Block getBlueVines() {
    return blueVines;
  }

  public Block getPurpleVines() {
    return purpleVines;
  }

  public Block getPinkVines() {
    return pinkVines;
  }

  public Block getWhiteVines() {
    return whiteVines;
  }

  public Block getBlueVinesPlant() {
    return blueVinesPlant;
  }

  public Block getPurpleVinesPlant() {
    return purpleVinesPlant;
  }

  public Block getPinkVinesPlant() {
    return pinkVinesPlant;
  }

  public Block getWhiteVinesPlant() {
    return whiteVinesPlant;
  }

  public Block getPartBlueLeaves() {
    return partBlueLeaves;
  }

  public Block getPartPurpleLeaves() {
    return partPurpleLeaves;
  }

  public Block getPartPinkLeaves() {
    return partPinkLeaves;
  }

  public Block getPartWhiteLeaves() {
    return partWhiteLeaves;
  }

  private String getWoodName() {
    String name;
    if (woodPreset == WoodPreset.NETHER) {
      name = getName() + "_hyphae";
    } else {
      name = getName() + "_wood";
    }
    return name;
  }

  private String getLogName() {
    String name;
    if (woodPreset == WoodPreset.BAMBOO) {
      name = getName() + "_block";
    } else if (woodPreset == WoodPreset.NETHER) {
      name = getName() + "_stem";
    } else {
      name = getName() + "_log";
    }
    return name;
  }

  private Block getBase() {
    Block base;
    if (woodPreset == WoodPreset.BAMBOO) {
      base = Blocks.BAMBOO_PLANKS;
    } else if (woodPreset == WoodPreset.FANCY) {
      base = Blocks.CHERRY_PLANKS;
    } else if (woodPreset == WoodPreset.NETHER) {
      base = Blocks.CRIMSON_PLANKS;
    } else {
      base = Blocks.OAK_PLANKS;
    }
    return base;
  }

  private Block getSignBase() {
    Block base;
    if (woodPreset == WoodPreset.BAMBOO) {
      base = Blocks.BAMBOO_SIGN;
    } else if (woodPreset == WoodPreset.FANCY) {
      base = Blocks.CHERRY_SIGN;
    } else if (woodPreset == WoodPreset.NETHER) {
      base = Blocks.CRIMSON_SIGN;
    } else {
      base = Blocks.OAK_SIGN;
    }
    return base;
  }

  private Block getHangingSignBase() {
    Block base;
    if (woodPreset == WoodPreset.BAMBOO) {
      base = Blocks.BAMBOO_HANGING_SIGN;
    } else if (woodPreset == WoodPreset.FANCY) {
      base = Blocks.CHERRY_HANGING_SIGN;
    } else if (woodPreset == WoodPreset.NETHER) {
      base = Blocks.CRIMSON_HANGING_SIGN;
    } else {
      base = Blocks.OAK_HANGING_SIGN;
    }
    return base;
  }

  public Object getBoatType() {
    return boatTypeSupplier.get();
  }

  public TagKey<Item> getItemLogsTag() {
    return itemLogsTag;
  }

  public TagKey<Block> getBlockLogsTag() {
    return blockLogsTag;
  }

  public List<Block> getRegisteredBlocksList() {
    return ImmutableList.copyOf(registeredBlocksList);
  }

  public List<Item> getRegisteredItemsList() {
    return ImmutableList.copyOf(registeredItemsList);
  }

  public List<Block> getLeavesList() {
    return ImmutableList.copyOf(leavesList);
  }

  public List<Block> getsaplingList() {
    return ImmutableList.copyOf(saplingList);
  }

  private Block createBlockWithItem(String blockID, Block block) {
    Block listBlock = com.diaithir.overworldbiomes.naturesspirit.registration.NSRegistryHelper.registerBlock(blockID, block);
    registeredBlocksList.add(listBlock);
    return listBlock;
  }

  public Item createItem(String blockID, Item item) {
    Item listItem = com.diaithir.overworldbiomes.naturesspirit.registration.NSRegistryHelper.registerItem(blockID, item);
    registeredItemsList.add(listItem);
    return listItem;
  }

  private Block registerBlockWithoutTab(String id, Block block) {
    return com.diaithir.overworldbiomes.naturesspirit.registration.NSRegistryHelper.registerBlockWithoutTab(id, block);
  }

  private Block registerTransparentBlockWithoutTab(String id, Block block) {
    return com.diaithir.overworldbiomes.naturesspirit.registration.NSRegistryHelper.registerTransparentBlockWithoutTab(id, block);
  }

  private Block logBlock() {
    return new RotatedPillarBlock(BlockBehaviour.Properties.of().mapColor(state -> state.getValue(RotatedPillarBlock.AXIS) == net.minecraft.core.Direction.Axis.Y ? topColor : sideColor)
        .instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava());
  }

  private Block createLog() {
    return createBlockWithItem(getLogName(), logBlock());
  }

  private Block createStrippedLog() {
    return createBlockWithItem("stripped_" + getLogName(), logBlock());
  }

  private Block createBundle() {
    return createBlockWithItem(getName() + "_bundle", logBlock());
  }

  private Block createStrippedBundle() {
    return createBlockWithItem("stripped_" + getName() + "_bundle", logBlock());
  }

  private Block createJoshuaLog() {
    return createBlockWithItem(getLogName(), new BranchingTrunkBlock(
        BlockBehaviour.Properties.of().ignitedByLava().mapColor(MapColor.COLOR_GRAY).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD)));
  }

  private Block createStrippedJoshuaLog() {
    return createBlockWithItem("stripped_" + getLogName(), new BranchingTrunkBlock(
        BlockBehaviour.Properties.of().ignitedByLava().mapColor(MapColor.COLOR_GRAY).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD)));
  }

  private Block createWood() {
    return createBlockWithItem(getWoodName(), new RotatedPillarBlock(
        BlockBehaviour.Properties.of().mapColor(sideColor).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()));
  }

  private Block createStrippedWood() {
    return createBlockWithItem("stripped_" + getWoodName(), new RotatedPillarBlock(
        BlockBehaviour.Properties.of().mapColor(topColor).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()));
  }

  private Block finishLeaves(String key, Block block) {
    addCompostable(block, 0.3F);
    addFlammable(block, 5, 20);
    LEAVES.put(key, block);
    leavesList.add(block);
    return block;
  }

  private Block createLeaves() {
    return finishLeaves(getName(), createBlockWithItem(getName() + "_leaves", new LeavesBlock(leavesProps())));
  }

  private Block createLeaves(String prefix) {
    return finishLeaves(prefix + getName(), createBlockWithItem(prefix + getName() + "_leaves", new LeavesBlock(leavesProps())));
  }

  private Block createFrostableLeaves() {
    return finishLeaves(getName(), createBlockWithItem(getName() + "_leaves", new ProjectileLeavesBlock(leavesProps(), frostyLeaves)));
  }

  private Block createFrostableLeaves(String prefix) {
    return finishLeaves(prefix + getName(), createBlockWithItem(prefix + getName() + "_leaves", new ProjectileLeavesBlock(leavesProps(), frostyLeaves)));
  }

  private Block createParticleLeaves(ParticleOptions particle, int chance) {
    return finishLeaves(getName(), createBlockWithItem(getName() + "_leaves", new ParticleLeavesBlock(leavesProps(), particle, chance)));
  }

  private Block createParticleLeaves(String prefix, ParticleOptions particle, int chance) {
    return finishLeaves(prefix + getName(), createBlockWithItem(prefix + getName() + "_leaves", new ParticleLeavesBlock(leavesProps(), particle, chance)));
  }

  private Block createVinesLeavesBlock(Block vinesPlantBlock, Block vinesTipBlock) {
    return finishLeaves(getName(), createBlockWithItem(getName() + "_leaves", new VinesLeavesBlock(leavesProps(), vinesPlantBlock, vinesTipBlock)));
  }

  private Block createVinesLeavesBlock(String prefix, Block vinesPlantBlock, Block vinesTipBlock) {
    return finishLeaves(prefix + getName(), createBlockWithItem(prefix + getName() + "_leaves", new VinesLeavesBlock(leavesProps(), vinesPlantBlock, vinesTipBlock)));
  }

  private static BlockBehaviour.Properties vineProps() {
    return BlockBehaviour.Properties.of().pushReaction(PushReaction.DESTROY).randomTicks().noCollision().noOcclusion().instabreak().sound(SoundType.WEEPING_VINES);
  }

  private Block createVines(Supplier<Block> vinesPlantBlock) {
    Block vinesBlock = createBlockWithItem(getName() + "_vines", new DownwardVineBlock(vineProps(), vinesPlantBlock));
    RENDER_LAYERS.put(getName() + "_vines", vinesBlock);
    addCompostable(vinesBlock, 0.3F);
    return vinesBlock;
  }

  private Block createVines(String prefix, Supplier<Block> vinesPlantBlock) {
    Block vinesBlock = createBlockWithItem(prefix + getName() + "_vines", new DownwardVineBlock(vineProps(), vinesPlantBlock));
    RENDER_LAYERS.put(prefix + getName() + "_vines", vinesBlock);
    addCompostable(vinesBlock, 0.3F);
    return vinesBlock;
  }

  private Block createVinesPlant(Block vines) {
    Block vinesPlant = registerBlockWithoutTab(getName() + "_vines_plant",
        new DownwardsVinePlantBlock(vineProps().noOcclusion().overrideLootTable(vines.getLootTable()), vines));
    RENDER_LAYERS.put(getName() + "_vines_plant", vinesPlant);
    return vinesPlant;
  }

  private Block createVinesPlant(String prefix, Block vines) {
    Block vinesPlant = registerBlockWithoutTab(prefix + getName() + "_vines_plant",
        new DownwardsVinePlantBlock(vineProps().noOcclusion().overrideLootTable(vines.getLootTable()), vines));
    RENDER_LAYERS.put(prefix + getName() + "_vines_plant", vinesPlant);
    return vinesPlant;
  }

  private BlockBehaviour.Properties plankProps() {
    return BlockBehaviour.Properties.ofFullCopy(getBase()).sound(getBlockSetType().soundType()).mapColor(getTopColor());
  }

  private Block createPlanks() {
    return createBlockWithItem(getName() + "_planks", new Block(plankProps()));
  }

  private Block createStairs() {
    return createBlockWithItem(getName() + "_stairs", new StairBlock(getBase().defaultBlockState(), plankProps()));
  }

  private Block createSlab() {
    return createBlockWithItem(getName() + "_slab", new SlabBlock(plankProps()));
  }

  private Block createMosaic() {
    return createBlockWithItem(getName() + "_mosaic", new Block(plankProps()));
  }

  private Block createMosaicStairs() {
    return createBlockWithItem(getName() + "_mosaic_stairs", new StairBlock(getBase().defaultBlockState(), plankProps()));
  }

  private Block createMosaicSlab() {
    return createBlockWithItem(getName() + "_mosaic_slab", new SlabBlock(plankProps()));
  }

  private Block createFence() {
    return createBlockWithItem(getName() + "_fence", new FenceBlock(plankProps()));
  }

  private Block createFenceGate() {
    return createBlockWithItem(getName() + "_fence_gate", new FenceGateBlock(getWoodType(),
        BlockBehaviour.Properties.of().mapColor(getBase().defaultMapColor()).forceSolidOn().instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).ignitedByLava()));
  }

  private Block createPressurePlate() {
    return createBlockWithItem(getName() + "_pressure_plate", new PressurePlateBlock(getBlockSetType(),
        BlockBehaviour.Properties.of().mapColor(this.getBase().defaultMapColor()).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollision().strength(0.5F).ignitedByLava()
            .pushReaction(PushReaction.DESTROY)));
  }

  private Block createButton() {
    return createBlockWithItem(getName() + "_button",
        new ButtonBlock(getBlockSetType(), 30, BlockBehaviour.Properties.of().noCollision().strength(0.5F).pushReaction(PushReaction.DESTROY)));
  }

  private Block createDoor() {
    return createBlockWithItem(getName() + "_door", new DoorBlock(getBlockSetType(), plankProps().noOcclusion()));
  }

  private Block createTrapDoor() {
    return createBlockWithItem(getName() + "_trapdoor", new TrapDoorBlock(getBlockSetType(), plankProps().noOcclusion()));
  }

  private Block createSign() {
    return registerBlockWithoutTab(getName() + "_sign", new StandingSignBlock(woodType, BlockBehaviour.Properties.ofFullCopy(getSignBase()).mapColor(this.getTopColor())));
  }

  private Block createWallSign() {
    return registerBlockWithoutTab(getName() + "_wall_sign",
        new WallSignBlock(woodType, BlockBehaviour.Properties.ofFullCopy(getSignBase()).mapColor(this.getTopColor()).overrideLootTable(sign.getLootTable())));
  }

  private Block createHangingSign() {
    return registerBlockWithoutTab(getName() + "_hanging_sign",
        new CeilingHangingSignBlock(woodType, BlockBehaviour.Properties.ofFullCopy(getHangingSignBase()).mapColor(this.getTopColor())));
  }

  private Block createWallHangingSign() {
    return registerBlockWithoutTab(getName() + "_wall_hanging_sign",
        new WallHangingSignBlock(woodType, BlockBehaviour.Properties.ofFullCopy(getHangingSignBase()).mapColor(this.getTopColor()).overrideLootTable(hangingSign.getLootTable())));
  }

  private Block saplingBlock(String prefix, boolean sandy) {
    BlockBehaviour.Properties props = BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_SAPLING);
    Block block = createBlockWithItem(prefix + getName() + "_sapling",
        sandy ? new SandySaplingBlock(props) : new NSSaplingBlock(props));
    addCompostable(block, 0.3F);
    RENDER_LAYERS.put(prefix + getName() + "_sapling", block);
    saplingList.add(block);
    return block;
  }

  public Block createSapling() {
    return saplingBlock("", false);
  }

  public Block createSandySapling() {
    return saplingBlock("", true);
  }

  public Block createPottedSapling(Block sapling) {
    return registerTransparentBlockWithoutTab("potted_" + getName() + "_sapling",
        new FlowerPotBlock(sapling, BlockBehaviour.Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY)));
  }

  public Block createSapling(String prefix) {
    return saplingBlock(prefix, false);
  }

  public Block createSandySapling(String prefix) {
    return saplingBlock(prefix, true);
  }

  public Block createPottedSapling(String prefix, Block sapling) {
    return registerTransparentBlockWithoutTab("potted_" + prefix + getName() + "_sapling",
        new FlowerPotBlock(sapling, BlockBehaviour.Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY)));
  }

  private Item createSignItem() {
    return createItem(getName() + "_sign", new SignItem(sign, wallSign, new Item.Properties().stacksTo(16)));
  }

  private Item createHangingSignItem() {
    return createItem(getName() + "_hanging_sign", new HangingSignItem(hangingSign, hangingWallSign, new Item.Properties().stacksTo(16)));
  }

  private BlockSetType createBlockSetType() {
    BlockSetType base;
    if (this.woodPreset == WoodPreset.BAMBOO) {
      base = BlockSetType.BAMBOO;
    } else if (woodPreset == WoodPreset.FANCY) {
      base = BlockSetType.CHERRY;
    } else if (this.woodPreset == WoodPreset.NETHER) {
      base = BlockSetType.CRIMSON;
    } else {
      base = BlockSetType.OAK;
    }
    return BlockSetType.register(new BlockSetType(getNameID().toString(), base.canOpenByHand(), base.canOpenByWindCharge(), base.canButtonBeActivatedByArrows(),
        base.pressurePlateSensitivity(), base.soundType(), base.doorClose(), base.doorOpen(), base.trapdoorClose(), base.trapdoorOpen(),
        base.pressurePlateClickOff(), base.pressurePlateClickOn(), base.buttonClickOff(), base.buttonClickOn()));
  }

  private WoodType createWoodType() {
    SoundType soundType = blockSetType.soundType();
    return WoodType.register(new WoodType(getNameID().toString(), blockSetType));
  }

  public boolean isSandy() {
    return woodPreset == WoodPreset.JOSHUA || woodPreset == WoodPreset.SANDY;
  }

  public boolean hasDefaultLeaves() {
    return woodPreset == WoodPreset.DEFAULT || woodPreset == WoodPreset.WISTERIA || woodPreset == WoodPreset.FANCY || woodPreset == WoodPreset.JOSHUA
        || woodPreset == WoodPreset.NO_SAPLING || woodPreset == WoodPreset.SANDY;
  }

  public boolean hasDefaultSapling() {
    return woodPreset != WoodPreset.NO_SAPLING && woodPreset != WoodPreset.WISTERIA;
  }

  public boolean hasBark() {
    return woodPreset != WoodPreset.JOSHUA && woodPreset != WoodPreset.BAMBOO;
  }

  public boolean hasMosaic() {
    return this.hasMosaic;
  }

  public enum WoodPreset {
    DEFAULT, MAPLE, ASPEN, FROSTABLE, JOSHUA, SANDY, NO_SAPLING, WISTERIA, WILLOW, FANCY, NETHER, BAMBOO
  }

  /**
   * Creative tab placement is wired by the module from a BuildCreativeModeTabContentsEvent handler.
   * This returns the ordered building-block entries for the given wood set.
   */
  public static List<Block> getBuildingTabEntries(WoodSet woodset) {
    List<Block> entries = new ArrayList<>();
    entries.add(woodset.getLog());
    if (woodset.getWoodPreset() == WoodPreset.JOSHUA) {
      entries.add(woodset.getBundle());
      entries.add(woodset.getStrippedLog());
      entries.add(woodset.getStrippedBundle());
    } else if (!woodset.hasBark()) {
      entries.add(woodset.getStrippedLog());
    } else {
      entries.add(woodset.getWood());
      entries.add(woodset.getStrippedLog());
      entries.add(woodset.getStrippedWood());
    }
    entries.add(woodset.getPlanks());
    if (woodset.hasMosaic()) {
      entries.add(woodset.getMosaic());
    }
    entries.add(woodset.getStairs());
    if (woodset.hasMosaic()) {
      entries.add(woodset.getMosaicStairs());
    }
    entries.add(woodset.getSlab());
    if (woodset.hasMosaic()) {
      entries.add(woodset.getMosaicSlab());
    }
    entries.add(woodset.getFence());
    entries.add(woodset.getFenceGate());
    entries.add(woodset.getDoor());
    entries.add(woodset.getTrapDoor());
    entries.add(woodset.getPressurePlate());
    entries.add(woodset.getButton());
    return entries;
  }

}
