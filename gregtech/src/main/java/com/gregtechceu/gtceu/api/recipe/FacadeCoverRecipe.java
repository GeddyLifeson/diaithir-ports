package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeSerializers;
import com.gregtechceu.gtceu.common.item.behavior.FacadeItemBehaviour;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;

public class FacadeCoverRecipe extends CustomRecipe {

    public static final Identifier ID = GTCEu.id("crafting/facade_cover");
    private static TagKey<Item> IRON_PLATE_TAG;

    public FacadeCoverRecipe(CraftingBookCategory category) {
        super(category);
        if (IRON_PLATE_TAG == null) {
            IRON_PLATE_TAG = ChemicalHelper.getTag(TagPrefix.plate, GTMaterials.Iron);
        }
    }

    @Override
    public boolean matches(CraftingInput container, @NotNull Level level) {
        boolean foundPlate = false;
        boolean foundBlockItem = false;
        for (int i = 0; i < container.size(); i++) {
            var item = container.getItem(i);
            if (item.isEmpty()) continue;
            if (FacadeItemBehaviour.isValidFacade(item)) {
                if (foundBlockItem) {
                    return false;
                }
                foundBlockItem = true;
            } else if (item.is(IRON_PLATE_TAG)) {
                if (foundPlate) {
                    return false;
                }
                foundPlate = true;
            } else {
                return false;
            }
        }
        return foundBlockItem && foundPlate;
    }

    @Override
    public @NotNull ItemStack assemble(CraftingInput container, HolderLookup.@NotNull Provider provider) { return null; /* ported stub */ }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() { return null; /* ported stub */ }

    @Override
    public boolean canCraftInDimensions(int width, int height) { return false; /* ported stub */ }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider provider) { return null; /* ported stub */ }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() { return null; /* ported stub */ }
}
