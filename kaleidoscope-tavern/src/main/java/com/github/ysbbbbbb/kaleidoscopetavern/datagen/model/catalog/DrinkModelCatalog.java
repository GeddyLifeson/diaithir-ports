package com.github.ysbbbbbb.kaleidoscopetavern.datagen.model.catalog;

import com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;

import java.util.List;

public final class DrinkModelCatalog {
    private static final List<BlockItemCatalogEntry> ENTRIES = List.of(
            new BlockItemCatalogEntry("wine", ModBlocks.WINE, ModItems.WINE),
            new BlockItemCatalogEntry("champagne", ModBlocks.CHAMPAGNE, ModItems.CHAMPAGNE),
            new BlockItemCatalogEntry("vodka", ModBlocks.VODKA, ModItems.VODKA),
            new BlockItemCatalogEntry("brandy", ModBlocks.BRANDY, ModItems.BRANDY),
            new BlockItemCatalogEntry("carignan", ModBlocks.CARIGNAN, ModItems.CARIGNAN),
            new BlockItemCatalogEntry("sakura_wine", ModBlocks.SAKURA_WINE, ModItems.SAKURA_WINE),
            new BlockItemCatalogEntry("plum_wine", ModBlocks.PLUM_WINE, ModItems.PLUM_WINE),
            new BlockItemCatalogEntry("whiskey", ModBlocks.WHISKEY, ModItems.WHISKEY),
            new BlockItemCatalogEntry("ice_wine", ModBlocks.ICE_WINE, ModItems.ICE_WINE),
            new BlockItemCatalogEntry("polaris_sweet_white", ModBlocks.POLARIS_SWEET_WHITE, ModItems.POLARIS_SWEET_WHITE),
            new BlockItemCatalogEntry("honey_wine", ModBlocks.HONEY_WINE, ModItems.HONEY_WINE),
            new BlockItemCatalogEntry("red_queen", ModBlocks.RED_QUEEN, ModItems.RED_QUEEN),
            new BlockItemCatalogEntry("miners_star", ModBlocks.MINERS_STAR, ModItems.MINERS_STAR),
            new BlockItemCatalogEntry("rum", ModBlocks.RUM, ModItems.RUM),
            new BlockItemCatalogEntry("riesling_dry_white", ModBlocks.RIESLING_DRY_WHITE, ModItems.RIESLING_DRY_WHITE),
            new BlockItemCatalogEntry("sunset_glow", ModBlocks.SUNSET_GLOW, ModItems.SUNSET_GLOW),
            new BlockItemCatalogEntry("madame_shexiang", ModBlocks.MADAME_SHEXIANG, ModItems.MADAME_SHEXIANG),
            new BlockItemCatalogEntry("sweet_berry_wine", ModBlocks.SWEET_BERRY_WINE, ModItems.SWEET_BERRY_WINE),
            new BlockItemCatalogEntry("sherry", ModBlocks.SHERRY, ModItems.SHERRY),
            new BlockItemCatalogEntry("mother_snow", ModBlocks.MOTHER_SNOW, ModItems.MOTHER_SNOW),
            new BlockItemCatalogEntry("luminous_bride", ModBlocks.LUMINOUS_BRIDE, ModItems.LUMINOUS_BRIDE),
            new BlockItemCatalogEntry("glowflower_brew", ModBlocks.GLOWFLOWER_BREW, ModItems.GLOWFLOWER_BREW),
            new BlockItemCatalogEntry("sauvignon_blanc_dry_white", ModBlocks.SAUVIGNON_BLANC_DRY_WHITE, ModItems.SAUVIGNON_BLANC_DRY_WHITE),
            new BlockItemCatalogEntry("vinegar", ModBlocks.VINEGAR, ModItems.VINEGAR),
            new BlockItemCatalogEntry("watermelon_juice", ModBlocks.WATERMELON_JUICE, ModItems.WATERMELON_JUICE)
    );

    public static List<BlockItemCatalogEntry> entries() {
        return ENTRIES;
    }
}


