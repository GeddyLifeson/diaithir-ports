package com.github.ysbbbbbb.kaleidoscopetavern.item;

import com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public class GrapevineItem extends BlockItem {
    public GrapevineItem(Identifier id) {
        super(ModBlocks.WILD_GRAPEVINE.get(), new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)));
    }
}
