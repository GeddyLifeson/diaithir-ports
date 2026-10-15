/*
 * MIT License
 *
 * Copyright (c) 2020 Azercoco & Technici4n
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package aztech.modern_industrialization.compat.kubejs.recipe;

import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.resources.ResourceKey;
import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import dev.latvian.mods.kubejs.core.ItemStackKJS;
import dev.latvian.mods.kubejs.plugin.builtin.wrapper.ItemWrapper;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.kubejs.recipe.component.SimpleRecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.UniqueIdBuilder;
import dev.latvian.mods.kubejs.recipe.filter.RecipeMatchContext;
import dev.latvian.mods.kubejs.recipe.match.ItemMatch;
import dev.latvian.mods.kubejs.recipe.match.ReplacementMatchInfo;

public class ItemOutputComponent extends SimpleRecipeComponent<MachineRecipe.ItemOutput> {
    public static final ResourceKey<RecipeComponentType<?>> TYPE = RecipeComponentType.key(MI.id("item_output"));
    public static final ItemOutputComponent INSTANCE = new ItemOutputComponent();

    private ItemOutputComponent() {
        super(TYPE, MachineRecipe.ItemOutput.CODEC, ItemWrapper.TYPE_INFO);
    }

    @Override
    public MachineRecipe.ItemOutput wrap(RecipeScriptContext cx, Object from) {
        var itemStack = ItemWrapper.wrap(cx.cx(), from);
        return new MachineRecipe.ItemOutput(ItemStackTemplate.fromNonEmptyStack(itemStack), 1);
    }

    @Override
    public boolean matches(RecipeMatchContext cx, MachineRecipe.ItemOutput value, ReplacementMatchInfo match) {
        return match.match() instanceof ItemMatch m && value.template().count() > 0
                && m.matches(cx, value.getStack(), match.exact());
    }

    @Override
    public MachineRecipe.ItemOutput replace(RecipeScriptContext cx, MachineRecipe.ItemOutput original, ReplacementMatchInfo match,
            Object with) {
        if (matches(cx, original, match)) {
            var stack = ItemWrapper.wrap(cx.cx(), with);
            return new MachineRecipe.ItemOutput(ItemStackTemplate.fromNonEmptyStack(stack.copyWithCount(original.template().count())), original.probability());
        } else {
            return original;
        }
    }

    @Override
    public boolean isEmpty(MachineRecipe.ItemOutput value) {
        return value.getStack().isEmpty();
    }

    @Override
    public void buildUniqueId(UniqueIdBuilder builder, MachineRecipe.ItemOutput value) {
        if (!value.getStack().isEmpty()) {
            builder.append(ItemStackKJS.class.cast(value.getStack()).kjs$getIdLocation());
        }
    }
}
