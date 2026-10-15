package com.buuz135.industrial.plugin.emi.category;

import com.buuz135.industrial.module.ModuleResourceProduction;
import com.buuz135.industrial.utils.Reference;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.Identifier;

public class DyeMixerEmiCategory extends EmiRecipeCategory {
    public static Identifier ID = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "dye_mixer");

    public DyeMixerEmiCategory() {
        super(ID, EmiStack.of(ModuleResourceProduction.DYE_MIXER.asItem()));
    }
}
