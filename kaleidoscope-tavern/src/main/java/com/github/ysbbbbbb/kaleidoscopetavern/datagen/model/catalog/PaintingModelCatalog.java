package com.github.ysbbbbbb.kaleidoscopetavern.datagen.model.catalog;

import com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;

import java.util.List;

public final class PaintingModelCatalog {
    private static final List<BlockItemCatalogEntry> ENTRIES = List.of(
            new BlockItemCatalogEntry("ysbb", ModBlocks.YSBB_PAINTING, ModItems.YSBB_PAINTING),
            new BlockItemCatalogEntry("tartaric_acid", ModBlocks.TARTARIC_ACID_PAINTING, ModItems.TARTARIC_ACID_PAINTING),
            new BlockItemCatalogEntry("cr019", ModBlocks.CR019_PAINTING, ModItems.CR019_PAINTING),
            new BlockItemCatalogEntry("unknown", ModBlocks.UNKNOWN_PAINTING, ModItems.UNKNOWN_PAINTING),
            new BlockItemCatalogEntry("master_marisa", ModBlocks.MASTER_MARISA_PAINTING, ModItems.MASTER_MARISA_PAINTING),
            new BlockItemCatalogEntry("son_of_man", ModBlocks.SON_OF_MAN_PAINTING, ModItems.SON_OF_MAN_PAINTING),
            new BlockItemCatalogEntry("david", ModBlocks.DAVID_PAINTING, ModItems.DAVID_PAINTING),
            new BlockItemCatalogEntry("girl_with_pearl_earring", ModBlocks.GIRL_WITH_PEARL_EARRING_PAINTING, ModItems.GIRL_WITH_PEARL_EARRING_PAINTING),
            new BlockItemCatalogEntry("starry_night", ModBlocks.STARRY_NIGHT_PAINTING, ModItems.STARRY_NIGHT_PAINTING),
            new BlockItemCatalogEntry("van_gogh_self_portrait", ModBlocks.VAN_GOGH_SELF_PORTRAIT_PAINTING, ModItems.VAN_GOGH_SELF_PORTRAIT_PAINTING),
            new BlockItemCatalogEntry("father", ModBlocks.FATHER_PAINTING, ModItems.FATHER_PAINTING),
            new BlockItemCatalogEntry("great_wave", ModBlocks.GREAT_WAVE_PAINTING, ModItems.GREAT_WAVE_PAINTING),
            new BlockItemCatalogEntry("mona_lisa", ModBlocks.MONA_LISA_PAINTING, ModItems.MONA_LISA_PAINTING),
            new BlockItemCatalogEntry("mondrian", ModBlocks.MONDRIAN_PAINTING, ModItems.MONDRIAN_PAINTING)
    );

    public static List<BlockItemCatalogEntry> entries() {
        return ENTRIES;
    }
}


