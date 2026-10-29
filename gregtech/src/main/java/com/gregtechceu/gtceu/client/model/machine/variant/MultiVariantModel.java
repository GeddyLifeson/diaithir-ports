package com.gregtechceu.gtceu.client.model.machine.variant;

import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.Identifier;


import com.google.gson.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record MultiVariantModel(List<VariantState> variants) implements UnbakedModel {

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (other instanceof MultiVariantModel model) {
            return this.variants.equals(model.variants);
        } else {
            return false;
        }
    }

    public @NotNull Collection<Identifier> getDependencies() { return null; /* ported stub */ }

    public void resolveParents(@NotNull Function<Identifier, UnbakedModel> resolver) {  /* ported stub */ }/* ported: removed */

    public static class Deserializer implements JsonDeserializer<MultiVariantModel> {

        public MultiVariantModel deserialize(JsonElement json, Type type,
                                             JsonDeserializationContext context) throws JsonParseException {
            List<VariantState> variants = new ArrayList<>();
            if (json.isJsonArray()) {
                JsonArray array = json.getAsJsonArray();
                if (array.isEmpty()) {
                    throw new JsonParseException("Empty variant array");
                }

                for (JsonElement v : array) {
                    variants.add(context.deserialize(v, VariantState.class));
                }
            } else {
                variants.add(context.deserialize(json, VariantState.class));
            }

            return new MultiVariantModel(variants);
        }
    }
}
