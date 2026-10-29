package com.gregtechceu.gtceu.client.model.machine.variant;

import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;

import com.gregtechceu.gtceu.client.util.VariantRotationHelpers;

import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;

import com.google.gson.*;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Transformation;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Type;
import java.util.Objects;

public class VariantState implements ModelState {

    @Getter
    private final Either<Identifier, UnbakedModel> model;
    @Getter
    private final Transformation rotation;
    @Getter
    private final boolean uvLocked;
    @Getter
    private final int weight;
    @Getter
    @Setter
    private UnbakedModel resolvedModel;

    public VariantState(Either<Identifier, UnbakedModel> model,
                        Transformation rotation, boolean uvLocked, int weight) {
        this.model = model;
        this.rotation = rotation;
        this.uvLocked = uvLocked;
        this.weight = weight;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (!(other instanceof VariantState variantState)) {
            return false;
        } else {
            return this.model.equals(variantState.model) &&
                    Objects.equals(this.rotation, variantState.rotation) &&
                    this.uvLocked == variantState.uvLocked &&
                    this.weight == variantState.weight;
        }
    }

    public int hashCode() {
        int i = this.model.hashCode();
        i = 31 * i + this.rotation.hashCode();
        i = 31 * i + Boolean.valueOf(this.uvLocked).hashCode();
        return 31 * i + this.weight;
    }

    public static class Deserializer implements JsonDeserializer<VariantState> {

        public VariantState deserialize(JsonElement json, Type type, JsonDeserializationContext context)
                                                                                                         throws JsonParseException { return null; /* ported stub */ }

        protected Transformation getBlockRotation(JsonObject json) { return null; /* ported stub */ }

        protected int getWeight(JsonObject json) {
            int i = GsonHelper.getAsInt(json, "weight", 1);
            if (i < 1) {
                throw new JsonParseException("Invalid weight " + i + " found, expected integer >= 1");
            } else {
                return i;
            }
        }
    }
}
