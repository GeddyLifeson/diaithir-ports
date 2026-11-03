package com.diaithir.overworldbiomes.naturesspirit.registration.sets;

import com.diaithir.overworldbiomes.naturesspirit.registration.NSRegistryHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.ArrayList;
import java.util.List;

import static com.diaithir.overworldbiomes.naturesspirit.registration.NSRegistryHelper.StoneHashMap;

public class StoneSet {

  /**
   * Minimal replacement for the removed datagen BlockFamily: groups the blocks of one stone family.
   * Pure data holder, no datagen dependency.
   */
  public static class BlockFamily {
    private final Block baseBlock;
    private Block slab;
    private Block stairs;
    private Block wall;
    private Block chiseled;
    private Block polished;
    private Block cracked;

    public BlockFamily(Block baseBlock) {
      this.baseBlock = baseBlock;
    }

    public Block getBaseBlock() {
      return baseBlock;
    }

    public Block getSlab() {
      return slab;
    }

    public Block getStairs() {
      return stairs;
    }

    public Block getWall() {
      return wall;
    }

    public Block getChiseled() {
      return chiseled;
    }

    public Block getPolished() {
      return polished;
    }

    public Block getCracked() {
      return cracked;
    }

    public static class Builder {
      private final BlockFamily family;

      public Builder(Block base) {
        this.family = new BlockFamily(base);
      }

      public Builder slab(Block block) {
        family.slab = block;
        return this;
      }

      public Builder stairs(Block block) {
        family.stairs = block;
        return this;
      }

      public Builder wall(Block block) {
        family.wall = block;
        return this;
      }

      public Builder chiseled(Block block) {
        family.chiseled = block;
        return this;
      }

      public Builder polished(Block block) {
        family.polished = block;
        return this;
      }

      public Builder cracked(Block block) {
        family.cracked = block;
        return this;
      }

      public BlockFamily build() {
        return family;
      }
    }
  }

  private static BlockFamily.Builder register(Block base) {
    return new BlockFamily.Builder(base);
  }

  private Block cobbled;
  private BlockFamily cobbledFamily;
  private Block cobbledStairs;
  private Block cobbledSlab;
  private Block cobbledWall;
  private Block mossyCobbled;
  private BlockFamily mossyCobbledFamily;
  private Block mossyCobbledStairs;
  private Block mossyCobbledSlab;
  private Block mossyCobbledWall;
  private Block base;
  private BlockFamily baseFamily;
  private Block baseStairs;
  private Block baseSlab;
  private Block polished;
  private BlockFamily polishedFamily;
  private Block polishedStairs;
  private Block polishedSlab;
  private Block polishedWall;
  private Block tiles;
  private BlockFamily tileFamily;
  private Block tilesStairs;
  private Block tilesSlab;
  private Block tilesWall;
  private Block bricks;
  private BlockFamily brickFamily;
  private Block bricksStairs;
  private Block bricksSlab;
  private Block bricksWall;
  private Block chiseled;
  private Block crackedBricks;
  private Block crackedTiles;
  private Block mossyBricks;
  private BlockFamily mossyBrickFamily;
  private Block mossyBricksStairs;
  private Block mossyBricksSlab;
  private Block mossyBricksWall;
  private final List<Block> registeredBlocksList = new ArrayList<>();

  private final Identifier name;
  private final MapColor mapColor;
  private final Item itemBefore;
  private final Item item2Before;
  private final boolean hasTiles;
  private final boolean hasCobbled;
  private final boolean hasCracked;
  private final boolean hasMossy;
  private boolean isRotatable;
  private final float hardness;


  private void registerStone() {

    base = isRotatable ? createRotatable(getName(), Blocks.ANDESITE) : createBasic(getName(), Blocks.ANDESITE);
    baseStairs = createStairs(getName(), base);
    baseSlab = createSlab(getName(), base);
    chiseled = createBasic("chiseled_" + getName(), Blocks.CHISELED_STONE_BRICKS);

    if (hasCobbled()) {
      cobbled = createBasic("cobbled_" + getName(), Blocks.COBBLESTONE);
      cobbledStairs = createStairs("cobbled_" + getName(), cobbled);
      cobbledSlab = createSlab("cobbled_" + getName(), cobbled);
      cobbledWall = createWall("cobbled_" + getName(), cobbled);
      cobbledFamily = register(cobbled).slab(cobbledSlab).stairs(cobbledStairs).chiseled(chiseled).wall(cobbledWall).build();
    }
    if (hasMossy() && hasCobbled()) {
      mossyCobbled = createBasic("mossy_cobbled_" + getName(), Blocks.MOSSY_COBBLESTONE);
      mossyCobbledStairs = createStairs("mossy_cobbled_" + getName(), mossyCobbled);
      mossyCobbledSlab = createSlab("mossy_cobbled_" + getName(), mossyCobbled);
      mossyCobbledWall = createWall("mossy_cobbled_" + getName(), mossyCobbled);
      mossyCobbledFamily = register(mossyCobbled).slab(mossyCobbledSlab).stairs(mossyCobbledStairs).wall(mossyCobbledWall).build();
    }

    polished = createBasic("polished_" + getName(), Blocks.POLISHED_ANDESITE);
    polishedStairs = createStairs("polished_" + getName(), polished);
    polishedSlab = createSlab("polished_" + getName(), polished);
    polishedWall = createWall("polished_" + getName(), polished);
    polishedFamily = register(polished).slab(polishedSlab).stairs(polishedStairs).wall(polishedWall).build();

    bricks = createBasic(getName() + "_bricks", Blocks.STONE_BRICKS);
    bricksStairs = createStairs(getName() + "_brick", bricks);
    bricksSlab = createSlab(getName() + "_brick", bricks);
    bricksWall = createWall(getName() + "_brick", bricks);

    if (hasMossy()) {
      mossyBricks = createBasic("mossy_" + getName() + "_bricks", Blocks.MOSSY_STONE_BRICKS);
      mossyBricksStairs = createStairs("mossy_" + getName() + "_brick", bricks);
      mossyBricksSlab = createSlab("mossy_" + getName() + "_brick", bricks);
      mossyBricksWall = createWall("mossy_" + getName() + "_brick", bricks);
      mossyBrickFamily = register(mossyBricks).slab(mossyBricksSlab).stairs(mossyBricksStairs).wall(mossyBricksWall).build();
    }
    if (hasCracked()) {
      crackedBricks = createBasic("cracked_" + getName() + "_bricks", Blocks.CRACKED_STONE_BRICKS);
      brickFamily = register(bricks).slab(bricksSlab).stairs(bricksStairs).wall(bricksWall).cracked(crackedBricks).build();
    } else {
      brickFamily = register(bricks).slab(bricksSlab).stairs(bricksStairs).wall(bricksWall).build();
    }
    if (hasTiles()) {
      tiles = createBasic(getName() + "_tiles", Blocks.COBBLESTONE);
      tilesStairs = createStairs(getName() + "_tile", polished);
      tilesSlab = createSlab(getName() + "_tile", polished);
      tilesWall = createWall(getName() + "_tile", polished);
      if (hasCracked()) {
        crackedTiles = createBasic("cracked_" + getName() + "_tiles", Blocks.CRACKED_STONE_BRICKS);
        tileFamily = register(tiles).slab(tilesSlab).stairs(tilesStairs).wall(tilesWall).cracked(crackedTiles).build();
      } else {
        tileFamily = register(tiles).slab(tilesSlab).stairs(tilesStairs).wall(tilesWall).build();
      }
    }

    if (hasCobbled()) {
      baseFamily = register(base).slab(baseSlab).stairs(baseStairs).polished(polished).build();
    } else {
      baseFamily = register(base).slab(baseSlab).stairs(baseStairs).polished(polished).chiseled(chiseled).build();
    }
    addToBuildingTab(itemBefore, item2Before, this);
    StoneHashMap.put(getName(), this);
  }

  public StoneSet(Identifier name, MapColor mapColor, Item itemBefore, Item item2Before, float hardness, boolean hasCobbled, boolean hasCracked, boolean hasMossy,
      boolean hasTiles) {
    this.name = name;
    this.mapColor = mapColor;
    this.itemBefore = itemBefore;
    this.item2Before = item2Before;
    this.hardness = hardness;
    this.hasTiles = hasTiles;
    this.hasCobbled = hasCobbled;
    this.hasCracked = hasCracked;
    this.hasMossy = hasMossy;
    registerStone();
  }

  public StoneSet(Identifier name, MapColor mapColor, Item itemBefore, Item item2Before, float hardness, boolean hasCobbled, boolean hasCracked, boolean hasMossy, boolean hasTiles,
      boolean isRotatable) {
    this.name = name;
    this.mapColor = mapColor;
    this.itemBefore = itemBefore;
    this.item2Before = item2Before;
    this.hardness = hardness;
    this.hasTiles = hasTiles;
    this.hasCobbled = hasCobbled;
    this.hasCracked = hasCracked;
    this.hasMossy = hasMossy;
    this.isRotatable = isRotatable;
    registerStone();
  }

  private Block createBlockWithItem(String blockID, Block block) {
    Block listBlock = NSRegistryHelper.registerBlock(NSRegistryHelper.name(blockID), block);
    registeredBlocksList.add(listBlock);
    return listBlock;
  }

  public String getName() {
    return name.getPath();
  }

  public List<Block> getRegisteredBlocksList() {
    return registeredBlocksList;
  }

  private Block createBasic(String name, Block template) {
    return createBlockWithItem(name, new Block(NSRegistryHelper.bpCopy(template).destroyTime(hardness).mapColor(getMapColor())));
  }

  private Block createRotatable(String name, Block template) {
    return createBlockWithItem(name, new RotatedPillarBlock(NSRegistryHelper.bpCopy(template).destroyTime(hardness).mapColor(getMapColor())));
  }

  private Block createStairs(String name, Block template) {
    return createBlockWithItem(name + "_stairs", new StairBlock(template.defaultBlockState(), NSRegistryHelper.bpCopy(template)));
  }

  private Block createSlab(String name, Block template) {
    return createBlockWithItem(name + "_slab", new SlabBlock(NSRegistryHelper.bpCopy(template)));
  }

  private Block createWall(String name, Block template) {
    return createBlockWithItem(name + "_wall", new WallBlock(NSRegistryHelper.bpCopy(template).forceSolidOn()));
  }

  public boolean hasTiles() {
    return hasTiles;
  }

  public boolean hasCracked() {
    return hasCracked;
  }

  public boolean hasCobbled() {
    return hasCobbled;
  }

  public boolean hasMossy() {
    return hasMossy;
  }

  public boolean isRotatable() {
    return isRotatable;
  }

  /**
   * Fabric ItemGroupEvents are not available on NeoForge. The creative tab placement is recorded
   * in NSRegistryHelper's tab placement lists so that the module can wire it from
   * BuildCreativeModeTabContentsEvent.
   */
  public static void addToBuildingTab(Item proceedingItem, Item naturalStonePlacement, StoneSet stoneSet) {
    List<Block> buildingBlocks = new ArrayList<>();
    buildingBlocks.add(stoneSet.getBase());
    buildingBlocks.add(stoneSet.getBaseStairs());
    buildingBlocks.add(stoneSet.getBaseSlab());
    if (stoneSet.hasCobbled()) {
      buildingBlocks.add(stoneSet.getCobbled());
      buildingBlocks.add(stoneSet.getCobbledStairs());
      buildingBlocks.add(stoneSet.getCobbledSlab());
      buildingBlocks.add(stoneSet.getCobbledWall());
      if (stoneSet.hasMossy()) {
        buildingBlocks.add(stoneSet.getMossyCobbled());
        buildingBlocks.add(stoneSet.getMossyCobbledStairs());
        buildingBlocks.add(stoneSet.getMossyCobbledSlab());
        buildingBlocks.add(stoneSet.getMossyCobbledWall());
      }
    }
    buildingBlocks.add(stoneSet.getChiseled());
    buildingBlocks.add(stoneSet.getPolished());
    buildingBlocks.add(stoneSet.getPolishedStairs());
    buildingBlocks.add(stoneSet.getPolishedSlab());
    buildingBlocks.add(stoneSet.getPolishedWall());
    buildingBlocks.add(stoneSet.getBricks());
    buildingBlocks.add(stoneSet.getBricksStairs());
    buildingBlocks.add(stoneSet.getBricksSlab());
    buildingBlocks.add(stoneSet.getBricksWall());
    if (stoneSet.hasCracked()) {
      buildingBlocks.add(stoneSet.getCrackedBricks());
    }
    if (stoneSet.hasMossy()) {
      buildingBlocks.add(stoneSet.getMossyBricks());
      buildingBlocks.add(stoneSet.getMossyBricksStairs());
      buildingBlocks.add(stoneSet.getMossyBricksSlab());
      buildingBlocks.add(stoneSet.getMossyBricksWall());
    }
    if (stoneSet.hasTiles()) {
      buildingBlocks.add(stoneSet.getTiles());
      buildingBlocks.add(stoneSet.getTilesStairs());
      buildingBlocks.add(stoneSet.getTilesSlab());
      buildingBlocks.add(stoneSet.getTilesWall());
      if (stoneSet.hasCracked()) {
        buildingBlocks.add(stoneSet.getCrackedTiles());
      }
    }
    BUILDING_TAB_ENTRIES.add(new TabPlacement(proceedingItem, buildingBlocks));
    NATURAL_TAB_ENTRIES.add(new TabPlacement(naturalStonePlacement, List.of(stoneSet.getBase())));
  }

  /** Placement record: blocks to add after the given anchor item in a creative tab. */
  public record TabPlacement(Item after, List<Block> blocks) {
  }

  public static final List<TabPlacement> BUILDING_TAB_ENTRIES = new ArrayList<>();
  public static final List<TabPlacement> NATURAL_TAB_ENTRIES = new ArrayList<>();

  public Block getCobbled() {
    return cobbled;
  }

  public Block getCobbledStairs() {
    return cobbledStairs;
  }

  public Block getCobbledSlab() {
    return cobbledSlab;
  }

  public Block getCobbledWall() {
    return cobbledWall;
  }

  public Block getBase() {
    return base;
  }

  public Block getBaseStairs() {
    return baseStairs;
  }

  public Block getBaseSlab() {
    return baseSlab;
  }

  public Block getPolished() {
    return polished;
  }

  public Block getPolishedStairs() {
    return polishedStairs;
  }

  public Block getPolishedSlab() {
    return polishedSlab;
  }

  public Block getPolishedWall() {
    return polishedWall;
  }

  public Block getTiles() {
    return tiles;
  }

  public Block getTilesStairs() {
    return tilesStairs;
  }

  public Block getTilesSlab() {
    return tilesSlab;
  }

  public Block getTilesWall() {
    return tilesWall;
  }

  public Block getBricks() {
    return bricks;
  }

  public Block getBricksStairs() {
    return bricksStairs;
  }

  public Block getBricksSlab() {
    return bricksSlab;
  }

  public Block getBricksWall() {
    return bricksWall;
  }

  public Block getChiseled() {
    return chiseled;
  }

  public Block getCrackedBricks() {
    return crackedBricks;
  }

  public Block getCrackedTiles() {
    return crackedTiles;
  }

  public Block getMossyBricks() {
    return mossyBricks;
  }

  public Block getMossyBricksStairs() {
    return mossyBricksStairs;
  }

  public Block getMossyBricksSlab() {
    return mossyBricksSlab;
  }

  public Block getMossyBricksWall() {
    return mossyBricksWall;
  }

  public Block getMossyCobbled() {
    return mossyCobbled;
  }

  public Block getMossyCobbledStairs() {
    return mossyCobbledStairs;
  }

  public Block getMossyCobbledSlab() {
    return mossyCobbledSlab;
  }

  public Block getMossyCobbledWall() {
    return mossyCobbledWall;
  }

  public MapColor getMapColor() {
    return mapColor;
  }

  public BlockFamily getMossyBrickFamily() {
    return mossyBrickFamily;
  }

  public BlockFamily getBrickFamily() {
    return brickFamily;
  }

  public BlockFamily getTileFamily() {
    return tileFamily;
  }

  public BlockFamily getPolishedFamily() {
    return polishedFamily;
  }

  public BlockFamily getBaseFamily() {
    return baseFamily;
  }

  public BlockFamily getCobbledFamily() {
    return cobbledFamily;
  }

  public BlockFamily getMossyCobbledFamily() {
    return mossyCobbledFamily;
  }
}
