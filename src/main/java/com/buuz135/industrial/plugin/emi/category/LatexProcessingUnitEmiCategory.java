package com.buuz135.industrial.plugin.emi.category;

import com.buuz135.industrial.module.ModuleCore;
import com.buuz135.industrial.utils.Reference;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.Identifier;

public class LatexProcessingUnitEmiCategory extends EmiRecipeCategory {
    public static Identifier ID = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "latex_processing_unit");

    public LatexProcessingUnitEmiCategory() {
        super(ID, EmiStack.of(ModuleCore.LATEX_PROCESSING.asItem()));
    }
}
