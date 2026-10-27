package com.github.ysbbbbbb.kaleidoscopetavern.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public class BarStoolBlockItem extends BlockItem {
    public BarStoolBlockItem(Identifier id, Supplier<? extends Block> block) {
        super(
                block.get(),
                new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))
        );
    }
}
