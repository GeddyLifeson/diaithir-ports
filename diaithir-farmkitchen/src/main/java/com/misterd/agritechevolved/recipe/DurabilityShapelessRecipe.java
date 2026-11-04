package com.misterd.agritechevolved.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class DurabilityShapelessRecipe extends CustomRecipe {
    private final ItemStackTemplate result;
    private final NonNullList<Ingredient> ingredients;
    private final Ingredient toolIngredient;
    private final int durabilityPerItem;
    private final CraftingBookCategory bookCategory;

    public DurabilityShapelessRecipe(CraftingBookCategory category, ItemStackTemplate result,
                                     NonNullList<Ingredient> ingredients, Ingredient toolIngredient,
                                     int durabilityPerItem) {
        super();
        this.bookCategory = category;
        this.result = result;
        this.ingredients = ingredients;
        this.toolIngredient = toolIngredient;
        this.durabilityPerItem = durabilityPerItem;
    }

    @Override
    public CraftingBookCategory category() {
        return bookCategory;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        NonNullList<Ingredient> remaining = NonNullList.create();
        remaining.addAll(ingredients);
        boolean foundTool = false;
        int totalProcessableItems = 0;
        ItemStack foundToolStack = ItemStack.EMPTY;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;

            if (toolIngredient.test(stack)) {
                if (foundTool) return false;
                foundTool = true;
                foundToolStack = stack;
            } else {
                boolean matched = false;
                for (int j = 0; j < remaining.size(); j++) {
                    if (remaining.get(j).test(stack)) {
                        totalProcessableItems++;
                        remaining.remove(j);
                        matched = true;
                        break;
                    }
                }
                if (!matched) return false;
            }
        }

        if (!foundTool || !remaining.isEmpty()) return false;

        if (foundToolStack.isDamageableItem()) {
            int neededDurability = totalProcessableItems * durabilityPerItem;
            return foundToolStack.getDamageValue() + neededDurability < foundToolStack.getMaxDamage();
        }
        return true;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        int processableItems = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty() || toolIngredient.test(stack)) continue;
            for (Ingredient ingredient : ingredients) {
                if (ingredient.test(stack)) {
                    processableItems++;
                    break;
                }
            }
        }
        ItemStack out = result.create();
        out.setCount(processableItems);
        return out;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        ItemStack toolStack = ItemStack.EMPTY;
        int toolSlot = -1;
        int processableItems = 0;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (toolIngredient.test(stack)) {
                toolStack = stack.copy();
                toolSlot = i;
            } else {
                for (Ingredient ingredient : ingredients) {
                    if (ingredient.test(stack)) {
                        processableItems++;
                        break;
                    }
                }
            }
        }

        if (!toolStack.isEmpty() && toolSlot != -1) {
            int totalDurability = processableItems * durabilityPerItem;
            if (toolStack.isDamageableItem()) {
                int newDamage = toolStack.getDamageValue() + totalDurability;
                if (newDamage < toolStack.getMaxDamage()) {
                    toolStack.setDamageValue(newDamage);
                    remaining.set(toolSlot, toolStack);
                }
            } else {
                remaining.set(toolSlot, toolStack);
            }
        }

        return remaining;
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return ATERecipe.DURABILITY_SHAPELESS_SERIALIZER.get();
    }

    public ItemStack getResult() {
        return result.create();
    }

    public ItemStackTemplate getResultTemplate() {
        return result;
    }

    public NonNullList<Ingredient> getRecipeIngredients() {
        return ingredients;
    }

    public Ingredient getToolIngredient() {
        return toolIngredient;
    }

    public int getDurabilityPerItem() {
        return durabilityPerItem;
    }
}