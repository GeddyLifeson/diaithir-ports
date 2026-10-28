package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerGroup;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerGroupDistinctness;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.ResearchData;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.SerializerIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntCircuitIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredientExtensions;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item.*;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.ui.GTRecipeTypeUI;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.common.recipe.condition.ResearchCondition;
import com.gregtechceu.gtceu.common.valueprovider.*;
import com.gregtechceu.gtceu.config.ConfigHolder;





import com.gregtechceu.gtceu.utils.*;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.jei.IngredientIO;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.*;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import it.unimi.dsi.fastutil.objects.*;
import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static com.gregtechceu.gtceu.api.recipe.RecipeHelper.addToRecipeHandlerMap;

@ExtensionMethod(SizedIngredientExtensions.class)
public class ItemRecipeCapability extends RecipeCapability<SizedIngredient> {

    public final static ItemRecipeCapability CAP = new ItemRecipeCapability();

    protected ItemRecipeCapability() {
        super("item", 0xFFD96106, true, 0, SerializerIngredient.INSTANCE);
    }

    @Override
    public SizedIngredient copyInner(SizedIngredient content) {
        return content;
    }

    @Override
    public SizedIngredient copyWithModifier(SizedIngredient content, ContentModifier modifier) { return null; /* ported stub */ }

    public IntProviderIngredient copyWithModifier(IntProviderIngredient content, ContentModifier modifier) {
        return IntProviderIngredient.of(content.getInner(),
                ModifiedIntProvider.of(content.getCountProvider(), modifier));
    }

    @Override
    public List<Object> compressIngredients(@Unmodifiable Collection<Object> ingredients) { return null; /* ported stub */ }

    @Override
    public @Nullable List<AbstractMapIngredient> getDefaultMapIngredient(Object object) {
        if (object instanceof Ingredient ingredient) {
            return CustomItemMapIngredient.from(ingredient);
        } else if (object instanceof SizedIngredient sized) {
            return getDefaultMapIngredient(sized.ingredient());
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean isRecipeSearchFilter() {
        return true;
    }

    @Override
    public int limitMaxParallelByOutput(IRecipeCapabilityHolder holder, GTRecipe recipe, int multiplier, boolean tick) { return (int)0; /* ported stub */ }

    @Override
    public int getMaxParallelByInput(IRecipeCapabilityHolder holder, GTRecipe recipe, int limit, boolean tick) { return (int)0; /* ported stub */ }

    private static List<Object2LongMap<ItemStack>> getInputContents(IRecipeCapabilityHolder holder) {
        var handlerLists = holder.getCapabilitiesForIO(IO.IN);
        if (handlerLists.isEmpty()) return Collections.emptyList();

        Map<RecipeHandlerGroup, List<RecipeHandlerList>> handlerGroups = new HashMap<>();
        for (var handler : handlerLists) {
            if (!handler.hasCapability(ItemRecipeCapability.CAP)) continue;
            addToRecipeHandlerMap(handler.getGroup(), handler, handlerGroups);
        }

        final var strat = ItemStackHashStrategy.comparingAllButCount();

        List<RecipeHandlerList> distinctHandlerLists = handlerGroups.getOrDefault(
                RecipeHandlerGroupDistinctness.BUS_DISTINCT,
                Collections.emptyList());
        List<Object2LongMap<ItemStack>> invs = new ArrayList<>(distinctHandlerLists.size() + 1);
        // Handle distinct groups first, adding an inventory based on their contents individually.
        for (RecipeHandlerList handlerList : distinctHandlerLists) {
            var handlers = handlerList.getCapability(ItemRecipeCapability.CAP);
            Object2LongOpenCustomHashMap<ItemStack> distinctInv = new Object2LongOpenCustomHashMap<>(strat);

            for (IRecipeHandler<?> handler : handlers) {
                for (var content : handler.getContents()) {
                    if (content instanceof ItemStack stack && !stack.isEmpty()) {
                        distinctInv.addTo(stack, stack.getCount());
                    }
                }
            }
            if (!distinctInv.isEmpty()) invs.add(distinctInv);
        }

        // Then handle other groups. The logic of undyed buses belonging to
        // everything has already been taken care of by addToRecipeMap()
        for (Map.Entry<RecipeHandlerGroup, List<RecipeHandlerList>> handlerListEntry : handlerGroups.entrySet()) {
            if (handlerListEntry.getKey() == RecipeHandlerGroupDistinctness.BUS_DISTINCT) continue;

            Object2LongOpenCustomHashMap<ItemStack> inventory = new Object2LongOpenCustomHashMap<>(strat);
            for (RecipeHandlerList handlerList : handlerListEntry.getValue()) {
                var handlers = handlerList.getCapability(ItemRecipeCapability.CAP);
                for (var handler : handlers) {
                    for (var content : handler.getContents()) {
                        if (content instanceof ItemStack stack && !stack.isEmpty()) {
                            inventory.addTo(stack, stack.getCount());
                        }
                    }
                }
            }
            if (!inventory.isEmpty()) invs.add(inventory);
        }

        return invs;
    }

    @Override
    public @NotNull List<Object> createXEIContainerContents(List<Content> contents, GTRecipe recipe, IO io) { return null; /* ported stub */ }

    public Object createXEIContainer(List<?> contents) { return null; /* ported stub */ }

    @NotNull
    @Override
    public Widget createWidget() {
        SlotWidget slot = new SlotWidget();
        slot.initTemplate();
        return slot;
    }

    @NotNull
    @Override
    public Class<? extends Widget> getWidgetClass() {
        return SlotWidget.class;
    }

    @Override
    public void applyWidgetInfo(@NotNull Widget widget,
                                int index,
                                boolean isXEI,
                                IO io,
                                GTRecipeTypeUI.@UnknownNullability("null when storage == null") RecipeHolder recipeHolder,
                                @NotNull GTRecipeType recipeType,
                                @UnknownNullability("null when content == null") GTRecipe recipe,
                                @Nullable Content content,
                                @Nullable Object storage, int recipeTier, int chanceTier) {  /* ported stub */ }/* ported: removed *//* ported: removed *//* ported: removed *//* ported: removed */

    public interface ICustomParallel {

        /**
         * Custom impl of the parallel limiter used by ParallelLogic to limit by outputs
         *
         * @param recipe     Recipe
         * @param multiplier Initial multiplier
         * @param tick       Tick or not
         * @return Limited multiplier
         */
        int limitItemParallel(GTRecipe recipe, int multiplier, boolean tick);
    }

    // Items should be respected for distinct checks
    @Override
    public boolean shouldBypassDistinct() {
        return false;
    }
}
