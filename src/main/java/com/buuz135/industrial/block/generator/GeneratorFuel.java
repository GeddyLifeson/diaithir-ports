package com.buuz135.industrial.block.generator;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.FuelValues;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/** 26.1 burn times need the level's fuel values, but generator filters and recipe lists have no level to ask. */
public final class GeneratorFuel {
    private GeneratorFuel() {}

    public static int burnTime(ItemStack stack) {
        var server = ServerLifecycleHooks.getCurrentServer();
        FuelValues fuel = server != null ? server.fuelValues() : Minecraft.getInstance().level.fuelValues();
        return stack.getBurnTime(RecipeType.SMELTING, fuel);
    }
}
