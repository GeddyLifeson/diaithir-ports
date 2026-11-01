package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.compat.Tier;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.data.recipe.CustomTags;


import net.minecraft.world.item.crafting.Ingredient;


@SuppressWarnings({ "unused", "FieldCanBeLocal" })
public class GTToolTiers {

    private static Tier DURANIUM;
    private static Tier NEUTRONIUM;

    @SuppressWarnings("DataFlowIssue")
    public static void init() {
        DURANIUM = new com.gregtechceu.gtceu.compat.SimpleTier(CustomTags.INCORRECT_FOR_DURANIUM_TOOL, 8193, 14.0F, 12.0F, 33,
                () -> Ingredient.of(com.gregtechceu.gtceu.compat.TagCompat.tag(net.minecraft.core.registries.BuiltInRegistries.ITEM, ChemicalHelper.getTag(TagPrefix.ingot, GTMaterials.Duranium))));
        NEUTRONIUM = new com.gregtechceu.gtceu.compat.SimpleTier(CustomTags.INCORRECT_FOR_NEUTRONIUM_TOOL, 65536, 180.0F, 100.0F, 33,
                () -> Ingredient.of(com.gregtechceu.gtceu.compat.TagCompat.tag(net.minecraft.core.registries.BuiltInRegistries.ITEM, ChemicalHelper.getTag(TagPrefix.ingot, GTMaterials.Neutronium))));
    }
}
