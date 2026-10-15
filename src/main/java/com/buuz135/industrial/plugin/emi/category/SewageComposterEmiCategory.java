package com.buuz135.industrial.plugin.emi.category;

import com.buuz135.industrial.module.ModuleAgricultureHusbandry;
import com.buuz135.industrial.utils.Reference;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.Identifier;

public class SewageComposterEmiCategory extends EmiRecipeCategory {
    public static Identifier ID = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "sewage_composter");

    public SewageComposterEmiCategory() {
        super(ID, EmiStack.of(ModuleAgricultureHusbandry.SEWAGE_COMPOSTER.asItem()));
    }
}
