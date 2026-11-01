package com.github.ysbbbbbb.kaleidoscopetavern.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class StringLightsBlockItem extends BlockItem {
    public StringLightsBlockItem(Identifier id, Supplier<? extends Block> block) {
        super(block.get(), new Properties().setId(ResourceKey.create(Registries.ITEM, id)));
    }

    @Override
    public @Nullable EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EquipmentSlot.CHEST;
    }
}
