package com.github.talrey.createdeco;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** Direct vanilla-registry registration in the same style as the Create port (no Registrate). */
public final class Reg {
  public static final List<Item> TAB_ITEMS = new ArrayList<>();
  public static final List<Item> BRICK_ITEMS = new ArrayList<>();
  public static boolean bricksTab = false;

  private Reg() {}

  @SuppressWarnings("unchecked")
  public static <T extends Block> T block(String id, Function<BlockBehaviour.Properties, T> f, BlockBehaviour.Properties p) {
    return block(id, f, p, (b, ip) -> new BlockItem(b, ip), new Item.Properties());
  }

  public static <T extends Block, I extends Item> T block(
      String id, Function<BlockBehaviour.Properties, T> f, BlockBehaviour.Properties p,
      BiFunction<T, Item.Properties, I> itemFactory, Item.Properties ip) {
    ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, CreateDecoMod.id(id));
    T block = Registry.register(BuiltInRegistries.BLOCK, key, f.apply(p.setId(key)));
    ResourceKey<Item> ikey = ResourceKey.create(Registries.ITEM, CreateDecoMod.id(id));
    I item = itemFactory.apply(block, ip.setId(ikey).useBlockDescriptionPrefix());
    ((BlockItem) item).registerBlocks(Item.BY_BLOCK, item);
    Registry.register(BuiltInRegistries.ITEM, ikey, item);
    (bricksTab ? BRICK_ITEMS : TAB_ITEMS).add(item);
    return block;
  }

  public static <T extends Block> T blockNoItem(String id, Function<BlockBehaviour.Properties, T> f, BlockBehaviour.Properties p) {
    ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, CreateDecoMod.id(id));
    return Registry.register(BuiltInRegistries.BLOCK, key, f.apply(p.setId(key)));
  }

  public static <T extends Item> T item(String id, Function<Item.Properties, T> f, Item.Properties p) {
    ResourceKey<Item> ikey = ResourceKey.create(Registries.ITEM, CreateDecoMod.id(id));
    T item = Registry.register(BuiltInRegistries.ITEM, ikey, f.apply(p.setId(ikey)));
    TAB_ITEMS.add(item);
    return item;
  }

  public static Identifier id(String path) { return CreateDecoMod.id(path); }
}
