package com.otectus.immersivetanning.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.otectus.immersivetanning.hide.HideData;
import com.otectus.immersivetanning.hide.HideSize;
import com.otectus.immersivetanning.trait.HideTraitLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Map;
import java.util.Optional;

/**
 * Another mod's skin, pelt or fur that the Tanner's Rack accepts as a raw hide of one size, so every route to leather
 * (a hide drop, a skinned carcass, a modded pelt) ends at the same rack. The item becomes an ordinary raw hide the
 * moment it is mounted. The only link between a butchery mod and this one is item ids, tags and, optionally, one string
 * data component that picks the size ({@code component} plus {@code sizes}).
 *
 * <pre>
 * { "ingredient": ["diaithirbutchery:raw_hide"], "size": "medium", "trait": "immersive_tanning:tough",
 *   "component": "diaithirbutchery:body", "sizes": {"deer": "medium", "bear": "large"} }
 * </pre>
 */
public record HideMountingRecipe(Ingredient ingredient, HideSize size, Optional<Identifier> trait,
                                 Optional<Identifier> component, Map<String, HideSize> sizes) {
    private static final Codec<HideSize> SIZE_CODEC = com.mojang.serialization.Codec.STRING.comapFlatMap(s -> {
        HideSize size = HideSize.byName(s);
        return size == null ? com.mojang.serialization.DataResult.error(() -> "size must be small, medium or large") : com.mojang.serialization.DataResult.success(size);
    }, HideSize::getSerializedName);

    public static final Codec<HideMountingRecipe> CODEC = RecordCodecBuilder.create(i -> i.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(HideMountingRecipe::ingredient),
            SIZE_CODEC.optionalFieldOf("size", HideSize.MEDIUM).forGetter(HideMountingRecipe::size),
            Identifier.CODEC.optionalFieldOf("trait").forGetter(HideMountingRecipe::trait),
            Identifier.CODEC.optionalFieldOf("component").forGetter(HideMountingRecipe::component),
            Codec.unboundedMap(Codec.STRING, SIZE_CODEC).optionalFieldOf("sizes", Map.of()).forGetter(HideMountingRecipe::sizes)
    ).apply(i, HideMountingRecipe::new));

    public boolean accepts(ItemStack stack) {
        return !stack.isEmpty() && ingredient.test(stack);
    }

    /** The raw hide this item becomes. The trait resolves on the server, where hide traits are loaded. */
    public HideData hide(ItemStack stack) {
        return HideData.raw(sizeOf(stack), trait.map(HideTraitLoader::get).orElse(null));
    }

    private HideSize sizeOf(ItemStack stack) {
        if (component.isPresent() && !sizes.isEmpty()) {
            var type = BuiltInRegistries.DATA_COMPONENT_TYPE.getOptional(component.get()).orElse(null);
            if (type != null && stack.get(type) instanceof String key && sizes.containsKey(key)) return sizes.get(key);
        }
        return size;
    }
}
