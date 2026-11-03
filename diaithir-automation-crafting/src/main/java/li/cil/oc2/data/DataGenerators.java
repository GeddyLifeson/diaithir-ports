/* SPDX-License-Identifier: MIT */

package li.cil.oc2.data;

import li.cil.oc2.api.API;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Collections;
import java.util.Set;

@EventBusSubscriber(modid = API.MOD_ID)
public final class DataGenerators {
    @SubscribeEvent
    public static void gatherServerData(final GatherDataEvent.Server event) {
        final PackOutput output = event.getGenerator().getPackOutput();

        event.addProvider(
            new LootTableProvider(
                output,
                Set.of(),
                Collections.singletonList(
                    new LootTableProvider.SubProviderEntry(
                        ModLootTableProvider.ModBlockLootTables::new,
                        LootContextParamSets.BLOCK
                    )
                ),
                event.getLookupProvider()
            )
        );
        final ModBlockTagsProvider blockTagsProvider = new ModBlockTagsProvider(output, event.getLookupProvider());
        event.addProvider(blockTagsProvider);
        event.addProvider(new ModItemTagsProvider(output, event.getLookupProvider(), blockTagsProvider.contentsGetter()));
        event.addProvider(new RecipeProvider.Runner(output, event.getLookupProvider()) {
            @Override
            protected RecipeProvider createRecipeProvider(final HolderLookup.Provider registries, final RecipeOutput recipeOutput) {
                return new ModRecipesProvider(registries, recipeOutput);
            }

            @Override
            public String getName() {
                return "Recipes";
            }
        });
    }

    @SubscribeEvent
    public static void gatherClientData(final GatherDataEvent.Client event) {
        final PackOutput output = event.getGenerator().getPackOutput();

        event.addProvider(new ModBlockStateProvider(output));
        event.addProvider(new ModItemModelProvider(output));
    }
}
