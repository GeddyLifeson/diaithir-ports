package com.github.ysbbbbbb.kaleidoscopetavern.datagen.model.catalog;

import com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;

import java.util.List;

public final class SandwichBoardModelCatalog {
    private static final List<BlockItemCatalogEntry> ENTRIES = List.of(
            new BlockItemCatalogEntry("grass", ModBlocks.GRASS_SANDWICH_BOARD, ModItems.GRASS_SANDWICH_BOARD),
            new BlockItemCatalogEntry("allium", ModBlocks.ALLIUM_SANDWICH_BOARD, ModItems.ALLIUM_SANDWICH_BOARD),
            new BlockItemCatalogEntry("azure_bluet", ModBlocks.AZURE_BLUET_SANDWICH_BOARD, ModItems.AZURE_BLUET_SANDWICH_BOARD),
            new BlockItemCatalogEntry("cornflower", ModBlocks.CORNFLOWER_SANDWICH_BOARD, ModItems.CORNFLOWER_SANDWICH_BOARD),
            new BlockItemCatalogEntry("orchid", ModBlocks.ORCHID_SANDWICH_BOARD, ModItems.ORCHID_SANDWICH_BOARD),
            new BlockItemCatalogEntry("peony", ModBlocks.PEONY_SANDWICH_BOARD, ModItems.PEONY_SANDWICH_BOARD),
            new BlockItemCatalogEntry("pink_petals", ModBlocks.PINK_PETALS_SANDWICH_BOARD, ModItems.PINK_PETALS_SANDWICH_BOARD),
            new BlockItemCatalogEntry("pitcher_plant", ModBlocks.PITCHER_PLANT_SANDWICH_BOARD, ModItems.PITCHER_PLANT_SANDWICH_BOARD),
            new BlockItemCatalogEntry("poppy", ModBlocks.POPPY_SANDWICH_BOARD, ModItems.POPPY_SANDWICH_BOARD),
            new BlockItemCatalogEntry("sunflower", ModBlocks.SUNFLOWER_SANDWICH_BOARD, ModItems.SUNFLOWER_SANDWICH_BOARD),
            new BlockItemCatalogEntry("torchflower", ModBlocks.TORCHFLOWER_SANDWICH_BOARD, ModItems.TORCHFLOWER_SANDWICH_BOARD),
            new BlockItemCatalogEntry("tulip", ModBlocks.TULIP_SANDWICH_BOARD, ModItems.TULIP_SANDWICH_BOARD),
            new BlockItemCatalogEntry("wither_rose", ModBlocks.WITHER_ROSE_SANDWICH_BOARD, ModItems.WITHER_ROSE_SANDWICH_BOARD)
    );

    public static List<BlockItemCatalogEntry> entries() {
        return ENTRIES;
    }
}


