package com.github.ysbbbbbb.kaleidoscopetavern.datagen.model.catalog;

import com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;

import java.util.List;

public final class StringLightsModelCatalog {
    private static final List<BlockItemCatalogEntry> ENTRIES = List.of(
            new BlockItemCatalogEntry("colorless", ModBlocks.STRING_LIGHTS_COLORLESS, ModItems.STRING_LIGHTS_COLORLESS),
            new BlockItemCatalogEntry("white", ModBlocks.STRING_LIGHTS_WHITE, ModItems.STRING_LIGHTS_WHITE),
            new BlockItemCatalogEntry("light_gray", ModBlocks.STRING_LIGHTS_LIGHT_GRAY, ModItems.STRING_LIGHTS_LIGHT_GRAY),
            new BlockItemCatalogEntry("gray", ModBlocks.STRING_LIGHTS_GRAY, ModItems.STRING_LIGHTS_GRAY),
            new BlockItemCatalogEntry("black", ModBlocks.STRING_LIGHTS_BLACK, ModItems.STRING_LIGHTS_BLACK),
            new BlockItemCatalogEntry("brown", ModBlocks.STRING_LIGHTS_BROWN, ModItems.STRING_LIGHTS_BROWN),
            new BlockItemCatalogEntry("red", ModBlocks.STRING_LIGHTS_RED, ModItems.STRING_LIGHTS_RED),
            new BlockItemCatalogEntry("orange", ModBlocks.STRING_LIGHTS_ORANGE, ModItems.STRING_LIGHTS_ORANGE),
            new BlockItemCatalogEntry("yellow", ModBlocks.STRING_LIGHTS_YELLOW, ModItems.STRING_LIGHTS_YELLOW),
            new BlockItemCatalogEntry("lime", ModBlocks.STRING_LIGHTS_LIME, ModItems.STRING_LIGHTS_LIME),
            new BlockItemCatalogEntry("green", ModBlocks.STRING_LIGHTS_GREEN, ModItems.STRING_LIGHTS_GREEN),
            new BlockItemCatalogEntry("cyan", ModBlocks.STRING_LIGHTS_CYAN, ModItems.STRING_LIGHTS_CYAN),
            new BlockItemCatalogEntry("light_blue", ModBlocks.STRING_LIGHTS_LIGHT_BLUE, ModItems.STRING_LIGHTS_LIGHT_BLUE),
            new BlockItemCatalogEntry("blue", ModBlocks.STRING_LIGHTS_BLUE, ModItems.STRING_LIGHTS_BLUE),
            new BlockItemCatalogEntry("purple", ModBlocks.STRING_LIGHTS_PURPLE, ModItems.STRING_LIGHTS_PURPLE),
            new BlockItemCatalogEntry("magenta", ModBlocks.STRING_LIGHTS_MAGENTA, ModItems.STRING_LIGHTS_MAGENTA),
            new BlockItemCatalogEntry("pink", ModBlocks.STRING_LIGHTS_PINK, ModItems.STRING_LIGHTS_PINK)
    );

    public static List<BlockItemCatalogEntry> entries() {
        return ENTRIES;
    }
}


