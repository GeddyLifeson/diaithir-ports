package com.github.ysbbbbbb.kaleidoscopetavern.datagen.model.catalog;

import com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;

import java.util.List;

public final class SofaModelCatalog {
    private static final List<BlockItemCatalogEntry> ENTRIES = List.of(
            new BlockItemCatalogEntry("white", ModBlocks.WHITE_SOFA, ModItems.WHITE_SOFA),
            new BlockItemCatalogEntry("light_gray", ModBlocks.LIGHT_GRAY_SOFA, ModItems.LIGHT_GRAY_SOFA),
            new BlockItemCatalogEntry("gray", ModBlocks.GRAY_SOFA, ModItems.GRAY_SOFA),
            new BlockItemCatalogEntry("black", ModBlocks.BLACK_SOFA, ModItems.BLACK_SOFA),
            new BlockItemCatalogEntry("brown", ModBlocks.BROWN_SOFA, ModItems.BROWN_SOFA),
            new BlockItemCatalogEntry("red", ModBlocks.RED_SOFA, ModItems.RED_SOFA),
            new BlockItemCatalogEntry("orange", ModBlocks.ORANGE_SOFA, ModItems.ORANGE_SOFA),
            new BlockItemCatalogEntry("yellow", ModBlocks.YELLOW_SOFA, ModItems.YELLOW_SOFA),
            new BlockItemCatalogEntry("lime", ModBlocks.LIME_SOFA, ModItems.LIME_SOFA),
            new BlockItemCatalogEntry("green", ModBlocks.GREEN_SOFA, ModItems.GREEN_SOFA),
            new BlockItemCatalogEntry("cyan", ModBlocks.CYAN_SOFA, ModItems.CYAN_SOFA),
            new BlockItemCatalogEntry("light_blue", ModBlocks.LIGHT_BLUE_SOFA, ModItems.LIGHT_BLUE_SOFA),
            new BlockItemCatalogEntry("blue", ModBlocks.BLUE_SOFA, ModItems.BLUE_SOFA),
            new BlockItemCatalogEntry("purple", ModBlocks.PURPLE_SOFA, ModItems.PURPLE_SOFA),
            new BlockItemCatalogEntry("magenta", ModBlocks.MAGENTA_SOFA, ModItems.MAGENTA_SOFA),
            new BlockItemCatalogEntry("pink", ModBlocks.PINK_SOFA, ModItems.PINK_SOFA)
    );

    public static List<BlockItemCatalogEntry> entries() {
        return ENTRIES;
    }
}
