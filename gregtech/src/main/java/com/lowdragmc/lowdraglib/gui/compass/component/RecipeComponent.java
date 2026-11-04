package com.lowdragmc.lowdraglib.gui.compass.component;

import com.lowdragmc.lowdraglib.gui.compass.ILayoutComponent;
import com.lowdragmc.lowdraglib.gui.compass.LayoutPageWidget;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.ProgressWidget;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.CycleItemStackHandler;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.w3c.dom.Element;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author KilaBash
 * @date 2022/10/21
 * @implNote RecipeComponent
 */
@NoArgsConstructor
public class RecipeComponent extends AbstractComponent {
    public static final ResourceTexture PROGRESS_BAR_ARROW = new ResourceTexture("ldlib:textures/gui/progress_bar_arrow.png");

    private static final List<RecipeViewCreator> RECIPE_VIEW_CREATORS = new ArrayList<>();

    public interface RecipeViewCreator extends Predicate<Recipe<?>> {
        ItemStack getWorkstation(RecipeHolder<?> recipe);

        WidgetGroup getViewWidget(RecipeHolder<?> recipe);
    }

    public static void registerRecipeViewCreator(RecipeViewCreator recipeViewCreator) {
        RECIPE_VIEW_CREATORS.add(recipeViewCreator);
    }

    @Nullable
    protected RecipeHolder<?> recipe;

    @Override
    public ILayoutComponent fromXml(Element element) { return null; /* ported stub */ }

    @Override
    protected LayoutPageWidget addWidgets(LayoutPageWidget currentPage) { return null; /* ported stub */ }

    protected WidgetGroup createSmeltingRecipeWidget(Int2ObjectMap<Ingredient> input, ItemStack output) { return null; /* ported stub */ }

    protected WidgetGroup createCraftingRecipeWidget(Int2ObjectMap<Ingredient> input, ItemStack output) { return null; /* ported stub */ }
}
