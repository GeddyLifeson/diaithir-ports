package com.buuz135.industrial.plugin.emi.category;

import com.buuz135.industrial.module.ModuleResourceProduction;
import com.buuz135.industrial.utils.Reference;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.Identifier;

public class FermentationStationEmiCategory extends EmiRecipeCategory {

    public static Identifier ID = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "fermentation_station");

    public FermentationStationEmiCategory() {
        super(ID, EmiStack.of(ModuleResourceProduction.FERMENTATION_STATION.asItem()));
    }

}
