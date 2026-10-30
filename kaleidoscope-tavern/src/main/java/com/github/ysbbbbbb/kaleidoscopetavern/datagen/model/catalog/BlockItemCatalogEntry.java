package com.github.ysbbbbbb.kaleidoscopetavern.datagen.model.catalog;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public record BlockItemCatalogEntry(String path, Supplier<? extends Block> block, Supplier<? extends Item> item) {
}

