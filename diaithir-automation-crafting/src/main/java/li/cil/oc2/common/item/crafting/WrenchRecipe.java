/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.item.crafting;

import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.Nullable;

public final class WrenchRecipe extends ShapelessRecipe {
    public WrenchRecipe(final ShapelessRecipe recipe) {
        super(new Recipe.CommonInfo(recipe.showNotification()),
            new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, recipe.group()),
            recipe.result(),
            recipe.placementInfo().ingredients());
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(final CraftingInput input) {
        final NonNullList<ItemStack> result = NonNullList.withSize(input.size(), ItemStack.EMPTY);

        for (int slot = 0; slot < input.size(); slot++) {
            final ItemStack stack = input.getItem(slot);
            final ItemStackTemplate remainderTemplate = stack.getCraftingRemainder();
            if (remainderTemplate != null) {
                final ItemStack remainder = remainderTemplate.create();
                if (!remainder.isEmpty()) {
                    result.set(slot, remainder);
                }
            }
            // TODO(oc2-restore): Wrenches.isWrench(stack) check (li.cil.oc2.common.integration is excluded); wrench stacks should be kept as a copy with count 1.
        }

        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RecipeSerializer<ShapelessRecipe> getSerializer() {
        // RecipeSerializer is a final record now and ShapelessRecipe fixes the serializer type, so cast.
        return (RecipeSerializer<ShapelessRecipe>) (RecipeSerializer<?>) Serializer.INSTANCE;
    }

    public static final class Serializer {
        public static final MapCodec<WrenchRecipe> CODEC =
            ShapelessRecipe.SERIALIZER.codec()
            .xmap(
                WrenchRecipe::new,
                x -> x
            );
        public static final StreamCodec<RegistryFriendlyByteBuf, WrenchRecipe> STREAM_CODEC =
            ShapelessRecipe.SERIALIZER.streamCodec()
            .map(
                WrenchRecipe::new,
                x -> x
            );
        public static final RecipeSerializer<WrenchRecipe> INSTANCE = new RecipeSerializer<>(CODEC, STREAM_CODEC);

        private Serializer() {
        }

        public MapCodec<WrenchRecipe> codec() {
            return CODEC;
        }

        public StreamCodec<RegistryFriendlyByteBuf, WrenchRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }

    public static final class WrenchRecipeOutputAdapter implements RecipeOutput {
        private final RecipeOutput inner;

        public WrenchRecipeOutputAdapter(RecipeOutput inner) {
            this.inner = inner;
        }

        @Override
        public Advancement.Builder advancement() {
            return inner.advancement();
        }

        @Override
        public void includeRootAdvancement() {
            inner.includeRootAdvancement();
        }

        @Override
        public void accept(final ResourceKey<Recipe<?>> resourceKey, final Recipe<?> recipe, @Nullable final AdvancementHolder advancementHolder, final ICondition... iConditions) {
            if (!(recipe instanceof ShapelessRecipe shapeless)) {
                throw new IllegalStateException("WrenchRecipeOutputAdapter can only be used on shapeless recipes");
            }

            inner.accept(resourceKey, new WrenchRecipe(shapeless), advancementHolder, iConditions);
        }
    }
}
