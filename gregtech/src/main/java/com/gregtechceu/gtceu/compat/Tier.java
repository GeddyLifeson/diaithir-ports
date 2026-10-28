package com.gregtechceu.gtceu.compat;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.Block;

/** Replacement for the removed 1.21 Tier; use {@link #toToolMaterial()} to build the 26.1 equivalent. */
public interface Tier {

    int getUses();

    float getSpeed();

    float getAttackDamageBonus();

    TagKey<Block> getIncorrectBlocksForDrops();

    int getEnchantmentValue();

    Ingredient getRepairIngredient();

    default ToolMaterial toToolMaterial(net.minecraft.tags.TagKey<net.minecraft.world.item.Item> repairItems) {
        return new ToolMaterial(getIncorrectBlocksForDrops(), getUses(), getSpeed(), getAttackDamageBonus(),
                getEnchantmentValue(), repairItems);
    }
}
