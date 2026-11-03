package com.diaithir.overworldbiomes.naturesspirit.registration;

import com.diaithir.overworldbiomes.naturesspirit.NatureSpirit;
import com.diaithir.overworldbiomes.naturesspirit.registration.sets.FlowerSet;
import com.diaithir.overworldbiomes.naturesspirit.registration.sets.StoneSet;
import com.diaithir.overworldbiomes.naturesspirit.registration.sets.WoodSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class NSRegistryHelper {

  public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(NatureSpirit.MOD_ID);
  public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NatureSpirit.MOD_ID);
  public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, NatureSpirit.MOD_ID);

  public static HashMap<String, FlowerSet> FlowerHashMap = new HashMap<>();
  public static HashMap<String, WoodSet> WoodHashMap = new HashMap<>();
  public static HashMap<String, StoneSet> StoneHashMap = new HashMap<>();
  public static HashMap<String, Block[]> SaplingHashMap = new HashMap<>();
  public static HashMap<String, Block> LeavesHashMap = new HashMap<>();
  public static HashMap<String, Block> RenderLayerHashMap = new HashMap<>();
  public static HashMap<String, Item> NatureSpiritItemHashMap = new HashMap<>();

  /** Compost chances (item -> chance) to be wired by the module from NeoForge events / data maps. */
  public static final Map<Item, Float> COMPOSTABLES = new LinkedHashMap<>();
  /** Compost chances registered before the item instance exists (block -> chance). */
  public static final Map<Block, Float> COMPOSTABLE_BLOCKS = new LinkedHashMap<>();

  /** Entries requested for creative tabs: tab key -> list of placements. */
  public static final Map<ResourceKey<CreativeModeTab>, List<TabEntry>> TAB_ENTRIES = new LinkedHashMap<>();

  public record TabEntry(ItemLike before, Supplier<? extends ItemLike> item) {}

  public static void init(IEventBus modBus) {
    BLOCKS.register(modBus);
    ITEMS.register(modBus);
    BLOCK_ENTITY_TYPES.register(modBus);
  }

  private static void addToTab(ResourceKey<CreativeModeTab> tab, ItemLike before, Supplier<? extends ItemLike> item) {
    TAB_ENTRIES.computeIfAbsent(tab, k -> new ArrayList<>()).add(new TabEntry(before, item));
  }

  /** Returns a compost chance for the given item, checking both block-based and item-based entries. */
  public static float getCompostChance(Item item) {
    Float chance = COMPOSTABLES.get(item);
    if (chance != null) {
      return chance;
    }
    if (item instanceof BlockItem blockItem) {
      Float blockChance = COMPOSTABLE_BLOCKS.get(blockItem.getBlock());
      if (blockChance != null) {
        return blockChance;
      }
    }
    return 0.0F;
  }

  public static Boolean never(BlockState state, BlockGetter world, BlockPos pos, EntityType<?> type) {
    return false;
  }

  public static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> registerBlockEntity(String name, Supplier<BlockEntityType<T>> factory) {
    return BLOCK_ENTITY_TYPES.register(name, factory);
  }

  public static Block registerBlockWithoutTab(String name, Block block) {
    return registerBlockInternal(name, block);
  }

  private static Block registerBlockInternal(String name, Block block) {
    // Blocks are constructed by callers; register the instance directly.
    BLOCKS.register(name, () -> block);
    return block;
  }

  public static Block registerBlock(String name, Block block) {
    Block registered = registerBlockWithoutTab(name, block);
    registerItem(name, new BlockItem(block, new Item.Properties()));
    return registered;
  }

  public static Block registerBlock(String name, Block block, ItemLike blockBefore) {
    Block block1 = registerBlockWithoutTab(name, block);
    registerItemWithoutTab(name, new BlockItem(block, new Item.Properties()));
    if (NatureSpirit.CONFIG.creative_tab) {
      addToTab(NSItemGroups.NS_ITEM_GROUP, blockBefore, block1::asItem);
    }
    return block1;
  }

  public static Block registerBlock(String name, Block block, ItemLike blockBefore, ResourceKey<CreativeModeTab> secondaryTab) {
    Block block1 = registerBlock(name, block);
    addToTab(secondaryTab, blockBefore, block1::asItem);
    return block1;
  }

  public static Block registerBlock(String name, Block block, ItemLike blockBefore, ResourceKey<CreativeModeTab> secondaryTab, ItemLike blockBefore2,
      ResourceKey<CreativeModeTab> thirdTab) {
    Block block1 = registerBlock(name, block, blockBefore, secondaryTab);
    addToTab(thirdTab, blockBefore2, block1::asItem);
    return block1;
  }

  public static Block registerTransparentBlockWithoutTab(String name, Block block) {
    Block block1 = registerBlockWithoutTab(name, block);
    RenderLayerHashMap.put(name, block1);
    return block1;
  }

  public static Block registerTransparentBlock(String name, Block block) {
    Block block1 = registerBlock(name, block);
    RenderLayerHashMap.put(name, block1);
    return block1;
  }

  public static Block registerTransparentBlock(String name, Block block, ItemLike itemBefore, ResourceKey<CreativeModeTab> secondaryTab) {
    Block block1 = registerBlock(name, block, itemBefore, secondaryTab);
    RenderLayerHashMap.put(name, block1);
    return block1;
  }

  public static Block registerTransparentBlock(String name, Block block, ItemLike itemBefore, ResourceKey<CreativeModeTab> secondaryTab, ItemLike blockBefore2,
      ResourceKey<CreativeModeTab> thirdTab) {
    Block block1 = registerBlock(name, block, itemBefore, secondaryTab, blockBefore2, thirdTab);
    RenderLayerHashMap.put(name, block1);
    return block1;
  }

  public static Block registerPlantBlock(String name, Block block, ItemLike itemBefore, float compost) {
    Block plant = registerTransparentBlock(name, block, itemBefore, CreativeModeTabs.NATURAL_BLOCKS);
    COMPOSTABLE_BLOCKS.put(block, compost);
    return plant;
  }

  public static Item registerItemWithoutTab(String name, Item item) {
    ITEMS.register(name, () -> item);
    NatureSpiritItemHashMap.put(name, item);
    return item;
  }

  public static Item registerItem(String name, Item item) {
    ITEMS.register(name, () -> item);
    if (NatureSpirit.CONFIG.creative_tab) {
      TAB_ENTRIES.computeIfAbsent(NSItemGroups.NS_ITEM_GROUP, k -> new ArrayList<>()).add(new TabEntry(null, () -> item));
    }
    NatureSpiritItemHashMap.put(name, item);
    return item;
  }

  public static Item registerItem(String name, Item item, ItemLike itemBefore, ResourceKey<CreativeModeTab> secondaryTab) {
    addToTab(secondaryTab, itemBefore, () -> item);
    return registerItem(name, item);
  }

  public static Item registerPlantItem(String name, Item item, ItemLike itemBefore, ResourceKey<CreativeModeTab> secondaryTab, float compost) {
    COMPOSTABLES.put(item, compost);
    return registerItem(name, item, itemBefore, secondaryTab);
  }

  public static Block registerTallPlantBlock(String name, Block block, ItemLike itemBefore, float compost) {
    Block plant = registerBlockWithoutTab(name, block);
    registerItem(name, new DoubleHighBlockItem(block, new Item.Properties()));
    addToTab(CreativeModeTabs.NATURAL_BLOCKS, itemBefore, plant::asItem);
    RenderLayerHashMap.put(name, plant);
    COMPOSTABLE_BLOCKS.put(block, compost);
    return plant;
  }
}
