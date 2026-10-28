package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.common.data.machines.GTAEMachines;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.GTCraftingComponents;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.ItemStack;





import java.util.Locale;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.LD_FLUID_PIPE;
import static com.gregtechceu.gtceu.common.data.GTBlocks.LD_ITEM_PIPE;
import static com.gregtechceu.gtceu.common.data.GTItems.*;
import static com.gregtechceu.gtceu.common.data.GTMachines.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.ASSEMBLER_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.ASSEMBLY_LINE_RECIPES;

public class MetaTileEntityMachineRecipeLoader {

    public static void init(RecipeOutput provider) {  /* ported stub */ }

    private static void registerLaserRecipes(RecipeOutput provider) {
        // 256A Laser Target Hatches
        for (int tier = 0; tier < LASER_INPUT_HATCH_256.length; tier++) {
            var hatch = LASER_INPUT_HATCH_256[tier];
            if (hatch == null) continue;

            ASSEMBLER_RECIPES.recipeBuilder(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_256a_laser_target_hatch")
                    .inputItems(HULL[tier])
                    .inputItems(lens, Diamond)
                    .inputItems(GTCraftingComponents.SENSOR.get(tier))
                    .inputItems(GTCraftingComponents.PUMP.get(tier))
                    .inputItems(GTCraftingComponents.CABLE.get(tier), 4)
                    .circuitMeta(1)
                    .outputItems(hatch)
                    .duration(300).EUt(VA[tier])
                    .addMaterialInfo(true).save(provider);
        }

        // 256A Laser Source Hatches
        for (int tier = 0; tier < LASER_OUTPUT_HATCH_256.length; tier++) {
            var hatch = LASER_OUTPUT_HATCH_256[tier];
            if (hatch == null) continue;

            ASSEMBLER_RECIPES.recipeBuilder(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_256a_laser_source_hatch")
                    .inputItems(HULL[tier])
                    .inputItems(lens, Diamond)
                    .inputItems(GTCraftingComponents.EMITTER.get(tier))
                    .inputItems(GTCraftingComponents.PUMP.get(tier))
                    .inputItems(GTCraftingComponents.CABLE.get(tier), 4)
                    .circuitMeta(1)
                    .outputItems(hatch)
                    .duration(300).EUt(VA[tier])
                    .addMaterialInfo(true).save(provider);
        }

        // 1024A Laser Target Hatches
        for (int tier = 0; tier < LASER_INPUT_HATCH_1024.length; tier++) {
            var hatch = LASER_INPUT_HATCH_1024[tier];
            if (hatch == null) continue;

            ASSEMBLER_RECIPES.recipeBuilder(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_1024a_laser_target_hatch")
                    .inputItems(HULL[tier])
                    .inputItems(lens, Diamond, 2)
                    .inputItems(GTCraftingComponents.SENSOR.get(tier), 2)
                    .inputItems(GTCraftingComponents.PUMP.get(tier), 2)
                    .inputItems(GTCraftingComponents.CABLE_DOUBLE.get(tier), 4)
                    .circuitMeta(2)
                    .outputItems(hatch)
                    .duration(600).EUt(VA[tier])
                    .addMaterialInfo(true).save(provider);
        }

        // 1024A Laser Source Hatches
        for (int tier = 0; tier < LASER_OUTPUT_HATCH_1024.length; tier++) {
            var hatch = LASER_OUTPUT_HATCH_1024[tier];
            if (hatch == null) continue;

            ASSEMBLER_RECIPES.recipeBuilder(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_1024a_laser_source_hatch")
                    .inputItems(HULL[tier])
                    .inputItems(lens, Diamond, 2)
                    .inputItems(GTCraftingComponents.EMITTER.get(tier), 2)
                    .inputItems(GTCraftingComponents.PUMP.get(tier), 2)
                    .inputItems(GTCraftingComponents.CABLE_DOUBLE.get(tier), 4)
                    .circuitMeta(2)
                    .outputItems(hatch)
                    .duration(600).EUt(VA[tier])
                    .addMaterialInfo(true).save(provider);
        }

        // 4096A Laser Target Hatches
        for (int tier = 0; tier < LASER_INPUT_HATCH_4096.length; tier++) {
            var hatch = LASER_INPUT_HATCH_4096[tier];
            if (hatch == null) continue;

            ASSEMBLER_RECIPES.recipeBuilder(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_4096a_laser_target_hatch")
                    .inputItems(HULL[tier])
                    .inputItems(lens, Diamond, 4)
                    .inputItems(GTCraftingComponents.SENSOR.get(tier), 4)
                    .inputItems(GTCraftingComponents.PUMP.get(tier), 4)
                    .inputItems(GTCraftingComponents.CABLE_QUAD.get(tier), 4)
                    .circuitMeta(3)
                    .outputItems(hatch)
                    .duration(1200).EUt(VA[tier])
                    .addMaterialInfo(true).save(provider);
        }

        // 4096A Laser Source Hatches
        for (int tier = 0; tier < LASER_OUTPUT_HATCH_4096.length; tier++) {
            var hatch = LASER_OUTPUT_HATCH_4096[tier];
            if (hatch == null) continue;

            ASSEMBLER_RECIPES.recipeBuilder(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_4096a_laser_output_hatch")
                    .inputItems(HULL[tier])
                    .inputItems(lens, Diamond, 4)
                    .inputItems(GTCraftingComponents.EMITTER.get(tier), 4)
                    .inputItems(GTCraftingComponents.PUMP.get(tier), 4)
                    .inputItems(GTCraftingComponents.CABLE_QUAD.get(tier), 4)
                    .circuitMeta(3)
                    .outputItems(hatch)
                    .duration(1200).EUt(VA[tier])
                    .addMaterialInfo(true).save(provider);
        }
    }
}
