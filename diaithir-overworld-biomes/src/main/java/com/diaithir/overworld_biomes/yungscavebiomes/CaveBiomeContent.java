package com.diaithir.overworld_biomes.yungscavebiomes;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;
import java.util.function.UnaryOperator;

public final class CaveBiomeContent {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CaveBiomes.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CaveBiomes.MOD_ID);

    public static final DeferredBlock<Block> GLOW_CRYSTAL_CLUSTER = block("glow_crystal_cluster", p -> p.lightLevel(s -> 10).noOcclusion().strength(1.5f).mapColor(MapColor.COLOR_CYAN));
    public static final DeferredBlock<Block> CRYSTAL_VAULT_STONE = block("crystal_vault_stone", p -> p.strength(2f, 6f).requiresCorrectToolForDrops().mapColor(MapColor.COLOR_LIGHT_BLUE));
    public static final DeferredBlock<Block> GLOW_FUNGUS_STALK = block("glow_fungus_stalk", p -> p.lightLevel(s -> 8).strength(1f).mapColor(MapColor.COLOR_PURPLE));
    public static final DeferredBlock<Block> SPORE_MOSS = block("spore_moss", p -> p.strength(0.5f).mapColor(MapColor.COLOR_GREEN));
    public static final DeferredBlock<Block> EMBER_BASALT = block("ember_basalt", p -> p.strength(1.5f, 4f).requiresCorrectToolForDrops().lightLevel(s -> 4).mapColor(MapColor.COLOR_ORANGE));
    public static final DeferredBlock<Block> VENT_STONE = block("vent_stone", p -> p.strength(2f, 6f).requiresCorrectToolForDrops().lightLevel(s -> 6).mapColor(MapColor.COLOR_RED));
    public static final DeferredBlock<Block> FROST_STONE = block("frost_stone", p -> p.strength(1.5f).requiresCorrectToolForDrops().friction(0.98f).mapColor(MapColor.ICE));
    public static final DeferredBlock<Block> ICE_SPIKE = block("ice_spike", p -> p.strength(0.8f).noOcclusion().friction(0.98f).mapColor(MapColor.ICE));
    public static final DeferredBlock<Block> RUSTED_PLATE = block("rusted_plate", p -> p.strength(3f, 6f).requiresCorrectToolForDrops().mapColor(MapColor.COLOR_BROWN));
    public static final DeferredBlock<Block> DRIP_PIPE_BLOCK = block("drip_pipe_block", p -> p.strength(2f, 5f).requiresCorrectToolForDrops().mapColor(MapColor.METAL));
    public static final DeferredBlock<Block> BIOLUM_VEIN = block("biolum_vein", p -> p.strength(1.5f, 3f).requiresCorrectToolForDrops().lightLevel(s -> 7).mapColor(MapColor.COLOR_BLUE));

    public static final DeferredItem<Item> CRYSTAL_SHARD = ITEMS.registerSimpleItem("crystal_shard");
    public static final DeferredItem<Item> SPORE_DUST = ITEMS.registerSimpleItem("spore_dust");
    public static final DeferredItem<Item> EMBER_CORE = ITEMS.registerSimpleItem("ember_core");

    private CaveBiomeContent() {}

    private static DeferredBlock<Block> block(String name, UnaryOperator<BlockBehaviour.Properties> props) {
        DeferredBlock<Block> b = BLOCKS.registerBlock(name, Block::new, () -> props.apply(BlockBehaviour.Properties.of()));
        ITEMS.registerSimpleBlockItem(b);
        return b;
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
    }
}
