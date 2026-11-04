package com.diaithir.nether.nether;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Registry holder for the Nether content pack. All ids live in the diaithir_nether namespace. */
public final class NetherContent {
    public static final String MOD_ID = "diaithir_nether";

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);

    public static final DeferredBlock<Block> ASH_SOIL = BLOCKS.registerSimpleBlock("ash_soil",
            p -> p.mapColor(MapColor.COLOR_GRAY).strength(0.6F).requiresCorrectToolForDrops());
    public static final DeferredBlock<Block> BASALT_SPIRE_STONE = BLOCKS.registerSimpleBlock("basalt_spire_stone",
            p -> p.mapColor(MapColor.COLOR_BLACK).strength(1.6F, 6.0F).requiresCorrectToolForDrops());
    public static final DeferredBlock<Block> EMBER_CRYSTAL_BLOCK = BLOCKS.registerSimpleBlock("ember_crystal_block",
            p -> p.mapColor(MapColor.COLOR_ORANGE).strength(1.2F, 3.0F).lightLevel(s -> 12).requiresCorrectToolForDrops());
    public static final DeferredBlock<Block> EMBER_ORE = BLOCKS.registerSimpleBlock("ember_ore",
            p -> p.mapColor(MapColor.NETHER).strength(3.0F, 3.0F).requiresCorrectToolForDrops());

    public static final DeferredItem<BlockItem> ASH_SOIL_ITEM = ITEMS.registerSimpleBlockItem("ash_soil", ASH_SOIL);
    public static final DeferredItem<BlockItem> BASALT_SPIRE_STONE_ITEM = ITEMS.registerSimpleBlockItem("basalt_spire_stone", BASALT_SPIRE_STONE);
    public static final DeferredItem<BlockItem> EMBER_CRYSTAL_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("ember_crystal_block", EMBER_CRYSTAL_BLOCK);
    public static final DeferredItem<BlockItem> EMBER_ORE_ITEM = ITEMS.registerSimpleBlockItem("ember_ore", EMBER_ORE);
    public static final DeferredItem<Item> EMBER_SHARD = ITEMS.registerSimpleItem("ember_shard", p -> p.fireResistant());

    public static final ResourceKey<net.minecraft.world.level.biome.Biome> ASH_PLAINS = biome("ash_plains");
    public static final ResourceKey<net.minecraft.world.level.biome.Biome> CRYSTAL_CAVERN = biome("crystal_cavern");

    private NetherContent() {}

    private static ResourceKey<net.minecraft.world.level.biome.Biome> biome(String name) {
        return ResourceKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(MOD_ID, name));
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
    }
}
