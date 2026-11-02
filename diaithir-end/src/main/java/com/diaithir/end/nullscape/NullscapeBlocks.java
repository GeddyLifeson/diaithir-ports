package com.diaithir.end.nullscape;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NullscapeBlocks {
 public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(NullscapeConstants.MOD_ID);
 public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NullscapeConstants.MOD_ID);

 public static final DeferredBlock<Block> VOID_STONE = BLOCKS.registerSimpleBlock("void_stone",
 p -> p.mapColor(MapColor.COLOR_BLACK).strength(3.0F, 9.0F).requiresCorrectToolForDrops());
 public static final DeferredBlock<Block> ASH_SEDIMENT = BLOCKS.registerSimpleBlock("ash_sediment",
 p -> p.mapColor(MapColor.COLOR_GRAY).strength(0.8F).sound(net.minecraft.world.level.block.SoundType.SAND));

 public static final DeferredItem<BlockItem> VOID_STONE_ITEM = ITEMS.registerSimpleBlockItem("void_stone", VOID_STONE);
 public static final DeferredItem<BlockItem> ASH_SEDIMENT_ITEM = ITEMS.registerSimpleBlockItem("ash_sediment", ASH_SEDIMENT);

 public static void register(IEventBus bus) {
 BLOCKS.register(bus);
 ITEMS.register(bus);
 }

 private NullscapeBlocks() {}
}
