package com.buuz135.industrial.plugin.emi.category;

import com.buuz135.industrial.module.ModuleResourceProduction;
import com.buuz135.industrial.utils.Reference;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.Identifier;

public class LaserDrillFluidEmiCategory extends EmiRecipeCategory {

    public static Identifier ID = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "laser_drill_fluid");

    public LaserDrillFluidEmiCategory() {
        super(ID, EmiStack.of(ModuleResourceProduction.FLUID_LASER_BASE.asItem()));
    }

}
