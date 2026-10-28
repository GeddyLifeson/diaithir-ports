package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerGroup;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerGroupDistinctness;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.SerializerFluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderFluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredientExtensions;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.fluid.*;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.ui.GTRecipeTypeUI;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.client.TooltipsHandler;
import com.gregtechceu.gtceu.common.valueprovider.*;





import com.gregtechceu.gtceu.utils.GTMath;

import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.jei.IngredientIO;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.crafting.*;

import it.unimi.dsi.fastutil.objects.*;
import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.stream.Collectors;

import static com.gregtechceu.gtceu.api.recipe.RecipeHelper.addToRecipeHandlerMap;

@ExtensionMethod(SizedIngredientExtensions.class)
public class FluidRecipeCapability extends RecipeCapability<SizedFluidIngredient> {

    public final static FluidRecipeCapability CAP = new FluidRecipeCapability();

    protected FluidRecipeCapability() {
        super("fluid", 0xFF3C70EE, true, 1, SerializerFluidIngredient.INSTANCE);
    }

    @Override
    public SizedFluidIngredient copyInner(SizedFluidIngredient content) { return null; /* ported stub */ }

    @Override
    public SizedFluidIngredient copyWithModifier(SizedFluidIngredient content, ContentModifier modifier) { return null; /* ported stub */ }

    public IntProviderFluidIngredient copyWithModifier(IntProviderFluidIngredient content, ContentModifier modifier) { return null; /* ported stub */ }

    @Override
    public List<Object> compressIngredients(@Unmodifiable Collection<Object> ingredients) {
        List<Object> list = new ObjectArrayList<>(ingredients.size());
        for (Object item : ingredients) {
            if (item instanceof SizedFluidIngredient fluid) {
                boolean isEqual = false;
                for (Object obj : list) {
                    if (obj instanceof SizedFluidIngredient SizedFluidIngredient) {
                        if (fluid.equals(SizedFluidIngredient)) {
                            isEqual = true;
                            break;
                        }
                    } else if (obj instanceof FluidStack fluidStack) {
                        if (fluid.ingredient().test(fluidStack)) {
                            isEqual = true;
                            break;
                        }
                    }
                }
                if (isEqual) continue;
                list.add(fluid);
            } else if (item instanceof FluidStack fluidStack) {
                boolean isEqual = false;
                for (Object obj : list) {
                    if (obj instanceof SizedFluidIngredient fluidIngredient) {
                        if (fluidIngredient.ingredient().test(fluidStack)) {
                            isEqual = true;
                            break;
                        }
                    } else if (obj instanceof FluidStack stack) {
                        if (FluidStack.isSameFluidSameComponents(fluidStack, stack)) {
                            isEqual = true;
                            break;
                        }
                    }
                }
                if (isEqual) continue;
                list.add(fluidStack);
            }
        }
        return list;
    }

    @Override
    public @Nullable List<AbstractMapIngredient> getDefaultMapIngredient(Object object) {
        if (object instanceof FluidIngredient fluidIngredient) {
            return CustomFluidMapIngredient.from(fluidIngredient);
        } else if (object instanceof SizedFluidIngredient ingredient) {
            return getDefaultMapIngredient(ingredient.ingredient());
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean isRecipeSearchFilter() {
        return true;
    }

    @Override
    public int limitMaxParallelByOutput(IRecipeCapabilityHolder holder, GTRecipe recipe, int multiplier, boolean tick) {
        if (holder instanceof ICustomParallel p) return p.limitFluidParallel(recipe, multiplier, tick);
        var outputContents = (tick ? recipe.tickOutputs : recipe.outputs).get(this);
        if (outputContents == null || outputContents.isEmpty()) return multiplier;

        if (!holder.hasCapabilityProxies()) return 0;

        var handlers = holder.getCapabilitiesFlat(IO.OUT, this);
        if (handlers.isEmpty()) return 0;

        int minMultiplier = 0;
        int maxMultiplier = multiplier;

        int maxAmount = 0;
        List<SizedFluidIngredient> ingredients = new ArrayList<>(outputContents.size());
        for (var content : outputContents) {
            var ing = this.of(content.content);
            maxAmount = Math.max(maxAmount, ing.amount());
            ingredients.add(ing);
        }
        if (maxAmount == 0) return multiplier;
        if (multiplier > Integer.MAX_VALUE / maxAmount) {
            maxMultiplier = multiplier = Integer.MAX_VALUE / maxAmount;
        }

        while (minMultiplier != maxMultiplier) {
            List<SizedFluidIngredient> copied = new ArrayList<>();
            for (final var ing : ingredients) {
                copied.add(this.copyWithModifier(ing, ContentModifier.multiplier(multiplier)));
            }

            for (var handler : handlers) {
                // noinspection unchecked
                copied = (List<SizedFluidIngredient>) handler.handleRecipe(IO.OUT, recipe, copied, true);
                if (copied == null) break;
            }
            int[] bin = ParallelLogic.adjustMultiplier(copied == null, minMultiplier, multiplier, maxMultiplier);
            minMultiplier = bin[0];
            multiplier = bin[1];
            maxMultiplier = bin[2];
        }

        return multiplier;
    }

    @Override
    public int getMaxParallelByInput(IRecipeCapabilityHolder holder, GTRecipe recipe, int limit, boolean tick) { return (int)0; /* ported stub */ }

    private static List<Object2LongMap<FluidStack>> getInputContents(IRecipeCapabilityHolder holder) {
        var handlerLists = holder.getCapabilitiesForIO(IO.IN);
        if (handlerLists.isEmpty()) return Collections.emptyList();

        Map<RecipeHandlerGroup, List<RecipeHandlerList>> handlerGroups = new HashMap<>();
        for (var handler : handlerLists) {
            if (!handler.hasCapability(FluidRecipeCapability.CAP)) continue;
            addToRecipeHandlerMap(handler.getGroup(), handler, handlerGroups);
        }

        List<RecipeHandlerList> distinctHandlerLists = handlerGroups.getOrDefault(
                RecipeHandlerGroupDistinctness.BUS_DISTINCT,
                Collections.emptyList());
        List<Object2LongMap<FluidStack>> invs = new ArrayList<>(distinctHandlerLists.size() + 1);
        // Handle distinct groups first, adding an inventory based on their contents individually.
        for (RecipeHandlerList handlerList : distinctHandlerLists) {
            var handlers = handlerList.getCapability(FluidRecipeCapability.CAP);
            Object2LongOpenHashMap<FluidStack> distinctInv = new Object2LongOpenHashMap<>();

            for (IRecipeHandler<?> handler : handlers) {
                for (var content : handler.getContents()) {
                    if (content instanceof FluidStack stack && !stack.isEmpty()) {
                        distinctInv.addTo(stack, stack.getAmount());
                    }
                }
            }
            if (!distinctInv.isEmpty()) invs.add(distinctInv);
        }

        // Then handle other groups. The logic of undyed hatches belonging to
        // everything has already been taken care of by addToRecipeMap()
        for (Map.Entry<RecipeHandlerGroup, List<RecipeHandlerList>> handlerListEntry : handlerGroups.entrySet()) {
            if (handlerListEntry.getKey() == RecipeHandlerGroupDistinctness.BUS_DISTINCT) continue;

            Object2LongOpenHashMap<FluidStack> inventory = new Object2LongOpenHashMap<>();
            for (RecipeHandlerList handlerList : handlerListEntry.getValue()) {
                var handlers = handlerList.getCapability(FluidRecipeCapability.CAP);
                for (var handler : handlers) {
                    for (var content : handler.getContents()) {
                        if (content instanceof FluidStack stack && !stack.isEmpty()) {
                            inventory.addTo(stack, stack.getAmount());
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
        TankWidget tank = new TankWidget();
        tank.initTemplate();
        tank.setFillDirection(ProgressTexture.FillDirection.ALWAYS_FULL);
        return tank;
    }

    @NotNull
    @Override
    public Class<? extends Widget> getWidgetClass() {
        return TankWidget.class;
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
                                @Nullable Object storage, int recipeTier, int chanceTier) {  /* ported stub */ }/* ported: removed *//* ported: removed */

    public interface ICustomParallel {

        /**
         * Custom impl of the parallel limiter used by ParallelLogic to limit by outputs
         *
         * @param recipe     Recipe
         * @param multiplier Initial multiplier
         * @param tick       Tick or not
         * @return Limited multiplier
         */
        int limitFluidParallel(GTRecipe recipe, int multiplier, boolean tick);
    }

    // Fluids should be respected for distinct checks
    @Override
    public boolean shouldBypassDistinct() {
        return false;
    }
}
