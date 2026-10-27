package com.github.ysbbbbbb.kaleidoscopetavern.datagen.model.catalog;

import com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;

import java.util.List;

public final class BarStoolModelCatalog {
    private static final List<BlockItemCatalogEntry> ENTRIES = List.of(
            new BlockItemCatalogEntry("white", ModBlocks.WHITE_BAR_STOOL, ModItems.WHITE_BAR_STOOL),
            new BlockItemCatalogEntry("light_gray", ModBlocks.LIGHT_GRAY_BAR_STOOL, ModItems.LIGHT_GRAY_BAR_STOOL),
            new BlockItemCatalogEntry("gray", ModBlocks.GRAY_BAR_STOOL, ModItems.GRAY_BAR_STOOL),
            new BlockItemCatalogEntry("black", ModBlocks.BLACK_BAR_STOOL, ModItems.BLACK_BAR_STOOL),
            new BlockItemCatalogEntry("brown", ModBlocks.BROWN_BAR_STOOL, ModItems.BROWN_BAR_STOOL),
            new BlockItemCatalogEntry("red", ModBlocks.RED_BAR_STOOL, ModItems.RED_BAR_STOOL),
            new BlockItemCatalogEntry("orange", ModBlocks.ORANGE_BAR_STOOL, ModItems.ORANGE_BAR_STOOL),
            new BlockItemCatalogEntry("yellow", ModBlocks.YELLOW_BAR_STOOL, ModItems.YELLOW_BAR_STOOL),
            new BlockItemCatalogEntry("lime", ModBlocks.LIME_BAR_STOOL, ModItems.LIME_BAR_STOOL),
            new BlockItemCatalogEntry("green", ModBlocks.GREEN_BAR_STOOL, ModItems.GREEN_BAR_STOOL),
            new BlockItemCatalogEntry("cyan", ModBlocks.CYAN_BAR_STOOL, ModItems.CYAN_BAR_STOOL),
            new BlockItemCatalogEntry("light_blue", ModBlocks.LIGHT_BLUE_BAR_STOOL, ModItems.LIGHT_BLUE_BAR_STOOL),
            new BlockItemCatalogEntry("blue", ModBlocks.BLUE_BAR_STOOL, ModItems.BLUE_BAR_STOOL),
            new BlockItemCatalogEntry("purple", ModBlocks.PURPLE_BAR_STOOL, ModItems.PURPLE_BAR_STOOL),
            new BlockItemCatalogEntry("magenta", ModBlocks.MAGENTA_BAR_STOOL, ModItems.MAGENTA_BAR_STOOL),
            new BlockItemCatalogEntry("pink", ModBlocks.PINK_BAR_STOOL, ModItems.PINK_BAR_STOOL)
    );

    public static List<BlockItemCatalogEntry> entries() {
        return ENTRIES;
    }
}


