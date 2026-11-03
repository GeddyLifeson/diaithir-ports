package com.diaithir.overworld_biomes.natures;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.item.BlockItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Natures: self-contained sub-feature of diaithir-overworld-biomes. */
public final class Natures {
    public static final String MOD_ID = "diaithir_overworld_biomes";
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);

    public static final DeferredBlock<Block> HEATH_TURF = BLOCKS.registerSimpleBlock("heath_turf",
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK).sound(SoundType.GRASS));
    public static final DeferredBlock<Block> STEPPE_LOAM = BLOCKS.registerSimpleBlock("steppe_loam",
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT));
    public static final DeferredBlock<Block> RIVETED_BASALT = BLOCKS.registerSimpleBlock("riveted_basalt",
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.STONE));

    public static final DeferredItem<BlockItem> HEATH_TURF_ITEM = ITEMS.registerSimpleBlockItem("heath_turf", HEATH_TURF);
    public static final DeferredItem<BlockItem> STEPPE_LOAM_ITEM = ITEMS.registerSimpleBlockItem("steppe_loam", STEPPE_LOAM);
    public static final DeferredItem<BlockItem> RIVETED_BASALT_ITEM = ITEMS.registerSimpleBlockItem("riveted_basalt", RIVETED_BASALT);

    public static final TagKey<Biome> NATURES_BIOMES =
            TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(MOD_ID, "natures"));

    private Natures() {}

    public static void init(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
    }

    public static boolean tellusPresent() { return ModList.get().isLoaded("tellus"); }
    public static boolean terralithPresent() { return ModList.get().isLoaded("terralith"); }
    public static boolean codexPresent() { return ModList.get().isLoaded("codex"); }
}
