package com.gregtechceu.gtceu.api.sync_system.data_transformers.gtceu;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeSerializer;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.ValueTransformer;

import net.minecraft.nbt.*;
import net.minecraft.resources.Identifier;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class GTRecipeTransformer implements ValueTransformer<GTRecipe> {

    @Override
    public Tag serializeNBT(GTRecipe value, ValueTransformer.TransformerContext<GTRecipe> context) {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", value.id.toString());
        tag.put("recipe",
                GTRecipeSerializer.CODEC.encode(value, NbtOps.INSTANCE, NbtOps.INSTANCE.mapBuilder())
                        .build(new CompoundTag()).getOrThrow());
        tag.putInt("parallels", value.parallels);
        tag.putInt("ocLevel", value.ocLevel);
        return tag;
    }

    @Override
    public @Nullable GTRecipe deserializeNBT(Tag tag, ValueTransformer.TransformerContext<GTRecipe> context) { return null; /* ported stub */ }
}
