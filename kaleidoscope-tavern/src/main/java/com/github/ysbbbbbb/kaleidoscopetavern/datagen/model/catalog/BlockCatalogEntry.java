package com.github.ysbbbbbb.kaleidoscopetavern.datagen.model.catalog;

import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public record BlockCatalogEntry(String path, Supplier<? extends Block> block) {
}

