package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.fluids.GTFluid;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

import org.jetbrains.annotations.Nullable;

public class GTBucketItem extends BucketItem {

    final Material material;
    final String langKey;

    public GTBucketItem(Fluid fluid, Properties properties, Material material, String langKey) {
        super(fluid, properties);
        this.material = material;
        this.langKey = langKey;
    }

    public static int color(ItemStack itemStack, int index) { return (int)0; /* ported stub */ }

    


    
    public Component getDescription() { return null; /* ported stub */ }

    @Override
    public Component getName(ItemStack stack) {
        return this.getDescription();
    }

    
    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
        if (this.content instanceof GTFluid gtFluid) {
            return gtFluid.getBurnTime();
        }
        return 0;
    }
}
