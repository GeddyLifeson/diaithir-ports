package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.data.chemical.material.ItemMaterialData;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.data.recipe.MaterialInfoLoader;
import com.gregtechceu.gtceu.data.recipe.configurable.RecipeAddition;
import com.gregtechceu.gtceu.data.recipe.configurable.RecipeRemoval;
import com.gregtechceu.gtceu.data.recipe.generated.*;
import com.gregtechceu.gtceu.data.recipe.misc.*;
import com.gregtechceu.gtceu.data.recipe.serialized.chemistry.ChemistryRecipes;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;


import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Consumer;

public class GTRecipes {

    public static final Set<Identifier> RECIPE_FILTERS = new ObjectOpenHashSet<>();

    /*
     * Called on resource reload in-game.
     *
     * These methods are meant for recipes that cannot be reasonably changed by a Datapack,
     * such as "X Ingot -> 2 X Rods" types of recipes, that follow a pattern for many recipes.
     *
     * This should also be used for recipes that need
     * to respond to a config option in ConfigHolder.
     */
    public static void recipeAddition(RecipeOutput originalConsumer) {
        RecipeOutput consumer = new RecipeOutput() {

            @Override
            public Advancement.@NotNull Builder advancement() {
                return originalConsumer.advancement();
            }

            @Override
            public void accept(net.minecraft.resources.ResourceKey<Recipe<?>> id, @NotNull Recipe<?> recipe,
                               @Nullable AdvancementHolder advancement,
                               net.neoforged.neoforge.common.conditions.ICondition... conditions) {
                accept(id, recipe, advancement);
            }

            @Override
            public void includeRootAdvancement() {
                originalConsumer.includeRootAdvancement();
            }

            @Override
            public void accept(net.minecraft.resources.ResourceKey<Recipe<?>> id, @NotNull Recipe<?> recipe,
                               @Nullable AdvancementHolder advancement) {
                if (!RECIPE_FILTERS.contains(id.identifier())) {
                    originalConsumer.accept(id, recipe, advancement);
                }
            }
        };

        // Decomposition info loading
        ItemMaterialData.reinitializeMaterialData();
        try { MaterialInfoLoader.init(); } catch (Throwable t) { GTCEu.LOGGER.error("Recipe step failed: MaterialInfoLoader.init", t); }

        // com.gregtechceu.gtceu.data.recipe.generated.*
        for (Material material : GTCEuAPI.materialManager) {
            if (material.hasFlag(MaterialFlags.DISABLE_MATERIAL_RECIPES)) {
                continue;
            }

            try { DecompositionRecipeHandler.run(consumer, material); } catch (Throwable t) { GTCEu.LOGGER.error("Recipe step failed: DecompositionRecipeHandler.run", t); }
            try { MaterialRecipeHandler.run(consumer, material); } catch (Throwable t) { GTCEu.LOGGER.error("Recipe step failed: MaterialRecipeHandler.run", t); }
            try { OreRecipeHandler.run(consumer, material); } catch (Throwable t) { GTCEu.LOGGER.error("Recipe step failed: OreRecipeHandler.run", t); }
            try { PartsRecipeHandler.run(consumer, material); } catch (Throwable t) { GTCEu.LOGGER.error("Recipe step failed: PartsRecipeHandler.run", t); }
            try { PipeRecipeHandler.run(consumer, material); } catch (Throwable t) { GTCEu.LOGGER.error("Recipe step failed: PipeRecipeHandler.run", t); }
            try { PolarizingRecipeHandler.run(consumer, material); } catch (Throwable t) { GTCEu.LOGGER.error("Recipe step failed: PolarizingRecipeHandler.run", t); }
            try { RecyclingRecipeHandler.run(consumer, material); } catch (Throwable t) { GTCEu.LOGGER.error("Recipe step failed: RecyclingRecipeHandler.run", t); }
            try { ToolRecipeHandler.run(consumer, material); } catch (Throwable t) { GTCEu.LOGGER.error("Recipe step failed: ToolRecipeHandler.run", t); }
            try { WireCombiningHandler.run(consumer, material); } catch (Throwable t) { GTCEu.LOGGER.error("Recipe step failed: WireCombiningHandler.run", t); }
            try { WireRecipeHandler.run(consumer, material); } catch (Throwable t) { GTCEu.LOGGER.error("Recipe step failed: WireRecipeHandler.run", t); }
        }

        try { CustomToolRecipes.init(consumer); } catch (Throwable t) { GTCEu.LOGGER.error("Recipe step failed: CustomToolRecipes.init", t); }
        try { AirScrubberRecipes.init(consumer); } catch (Throwable t) { GTCEu.LOGGER.error("Recipe step failed: AirScrubberRecipes.init", t); }
        try { ChemistryRecipes.init(consumer); } catch (Throwable t) { GTCEu.LOGGER.error("Recipe step failed: ChemistryRecipes.init", t); }
        try { MetaTileEntityMachineRecipeLoader.init(consumer); } catch (Throwable t) { GTCEu.LOGGER.error("Recipe step failed: MetaTileEntityMachineRecipeLoader.init", t); }
        try { MiscRecipeLoader.init(consumer); } catch (Throwable t) { GTCEu.LOGGER.error("Recipe step failed: MiscRecipeLoader.init", t); }
        try { VanillaStandardRecipes.init(consumer); } catch (Throwable t) { GTCEu.LOGGER.error("Recipe step failed: VanillaStandardRecipes.init", t); }
        try { WoodMachineRecipes.init(consumer); } catch (Throwable t) { GTCEu.LOGGER.error("Recipe step failed: WoodMachineRecipes.init", t); }
        try { StoneMachineRecipes.init(consumer); } catch (Throwable t) { GTCEu.LOGGER.error("Recipe step failed: StoneMachineRecipes.init", t); }
        try { CraftingRecipeLoader.init(consumer); } catch (Throwable t) { GTCEu.LOGGER.error("Recipe step failed: CraftingRecipeLoader.init", t); }
        try { FuelRecipes.init(consumer); } catch (Throwable t) { GTCEu.LOGGER.error("Recipe step failed: FuelRecipes.init", t); }
        try { FusionLoader.init(consumer); } catch (Throwable t) { GTCEu.LOGGER.error("Recipe step failed: FusionLoader.init", t); }
        try { MachineRecipeLoader.init(consumer); } catch (Throwable t) { GTCEu.LOGGER.error("Recipe step failed: MachineRecipeLoader.init", t); }
        try { AssemblerRecipeLoader.init(consumer); } catch (Throwable t) { GTCEu.LOGGER.error("Recipe step failed: AssemblerRecipeLoader.init", t); }
        try { AssemblyLineLoader.init(consumer); } catch (Throwable t) { GTCEu.LOGGER.error("Recipe step failed: AssemblyLineLoader.init", t); }
        try { BatteryRecipes.init(consumer); } catch (Throwable t) { GTCEu.LOGGER.error("Recipe step failed: BatteryRecipes.init", t); }
        try { DecorationRecipes.init(consumer); } catch (Throwable t) { GTCEu.LOGGER.error("Recipe step failed: DecorationRecipes.init", t); }

        try { CircuitRecipes.init(consumer); } catch (Throwable t) { GTCEu.LOGGER.error("Recipe step failed: CircuitRecipes.init", t); }
        try { ComponentRecipes.init(consumer); } catch (Throwable t) { GTCEu.LOGGER.error("Recipe step failed: ComponentRecipes.init", t); }
        try { MetaTileEntityLoader.init(consumer); } catch (Throwable t) { GTCEu.LOGGER.error("Recipe step failed: MetaTileEntityLoader.init", t); }

        // GCYM
        try { GCYMRecipes.init(consumer); } catch (Throwable t) { GTCEu.LOGGER.error("Recipe step failed: GCYMRecipes.init", t); }

        // Config-dependent recipes
        try { RecipeAddition.init(consumer); } catch (Throwable t) { GTCEu.LOGGER.error("Recipe step failed: RecipeAddition.init", t); }

        AddonFinder.getAddonList().forEach(addon -> addon.addRecipes(consumer));

        // Must run recycling recipes very last
        if (true) {
            try { RecyclingRecipes.init(consumer); } catch (Throwable t) { GTCEu.LOGGER.error("Recipe step failed: RecyclingRecipes.init", t); }
            try { ItemMaterialData.resolveItemMaterialInfos(consumer); } catch (Throwable t) { GTCEu.LOGGER.error("resolveItemMaterialInfos failed", t); }
        }
    }

    /*
     * Called on resource reload in-game, just before the above method.
     *
     * This is also where any recipe removals should happen.
     */
    public static void recipeRemoval(Consumer<Identifier> registry) {
        final Consumer<Identifier> actualConsumer = registry.andThen(RECIPE_FILTERS::add);
        RECIPE_FILTERS.clear();

        try { RecipeRemoval.init(actualConsumer); } catch (Throwable t) { GTCEu.LOGGER.error("Recipe step failed: RecipeRemoval.init", t); }
        AddonFinder.getAddonList().forEach(addon -> addon.removeRecipes(actualConsumer));
    }
}
