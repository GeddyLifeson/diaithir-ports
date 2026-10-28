package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.recipe.condition.ResearchCondition;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

import static com.gregtechceu.gtceu.utils.codec.GTCodecUtils.quietExceptionCodec;

@SuppressWarnings("DataFlowIssue")
public class GTRecipeSerializer {

    // spotless:off
    public static final Codec<GTRecipeType> GT_RECIPE_TYPE_CODEC = BuiltInRegistries.RECIPE_TYPE.byNameCodec()
            .comapFlatMap(recipeType -> {
                if (recipeType instanceof GTRecipeType gtRecipeType) {
                    return DataResult.success(gtRecipeType);
                } else {
                    return DataResult.error(() -> "Recipe type " + recipeType + " is not a GTRecipeType");
                }
            }, Function.identity());
    public static final StreamCodec<ByteBuf, GTRecipeType> GT_RECIPE_TYPE_STREAM_CODEC = new StreamCodec<>() {

        private static final StreamCodec<ByteBuf, RecipeType<?>> STREAM_CODEC = Identifier.STREAM_CODEC
                .map(id -> BuiltInRegistries.RECIPE_TYPE.getValue(id), BuiltInRegistries.RECIPE_TYPE::getKey);

        @Override
        public @NotNull GTRecipeType decode(@NotNull ByteBuf buffer) {
            RecipeType<?> recipeType = STREAM_CODEC.decode(buffer);
            if (!(recipeType instanceof GTRecipeType gtRecipeType)) {
                throw new DecoderException("Recipe type " + recipeType + " is not a GTRecipeType");
            }
            return gtRecipeType;
        }

        @Override
        public void encode(@NotNull ByteBuf buffer, @NotNull GTRecipeType value) {
            STREAM_CODEC.encode(buffer, value);
        }
    };

    public static final Codec<Map<RecipeCapability<?>, ChanceLogic>> CHANCE_LOGIC_MAP_CODEC = Codec.
            unboundedMap(RecipeCapability.DIRECT_CODEC, GTRegistries.CHANCE_LOGICS.byNameCodec());

    public static final MapCodec<GTRecipe> CODEC = makeCodec(GTCEu.Mods.isKubeJSLoaded());
    public static final StreamCodec<RegistryFriendlyByteBuf, GTRecipe> STREAM_CODEC = StreamCodec
            .of(GTRecipeSerializer::toNetwork, GTRecipeSerializer::fromNetwork);
    public static final RecipeSerializer<GTRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);
    // spotless:on

    
    public @NotNull MapCodec<GTRecipe> codec() {
        return CODEC;
    }

    
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, GTRecipe> streamCodec() {
        return STREAM_CODEC;
    }

    public static Tuple<RecipeCapability<?>, List<Content>> entryReader(RegistryFriendlyByteBuf buf) { return null; /* ported stub */ }

    public static Tuple<RecipeCapability<?>, ChanceLogic> changeLogicEntryReader(RegistryFriendlyByteBuf buf) { return null; /* ported stub */ }

    public static void entryWriter(RegistryFriendlyByteBuf buf,
                                   Map.Entry<RecipeCapability<?>, ? extends List<Content>> entry) {
        RecipeCapability<?> capability = entry.getKey();
        List<Content> contents = entry.getValue();
        buf.writeIdentifier(GTRegistries.RECIPE_CAPABILITIES.getKey(capability));
        writeCollection(contents, buf, capability.serializer::toNetworkContent);
    }

    public static void changeLogicEntryWriter(RegistryFriendlyByteBuf buf,
                                              Map.Entry<RecipeCapability<?>, ChanceLogic> entry) {
        RecipeCapability<?> capability = entry.getKey();
        ChanceLogic logic = entry.getValue();
        buf.writeIdentifier(GTRegistries.RECIPE_CAPABILITIES.getKey(capability));
        buf.writeIdentifier(GTRegistries.CHANCE_LOGICS.getKey(logic));
    }

    public static Map<RecipeCapability<?>, List<Content>> tuplesToMap(List<Tuple<RecipeCapability<?>, List<Content>>> entries) {
        Map<RecipeCapability<?>, List<Content>> map = new HashMap<>();
        entries.forEach(entry -> map.put(entry.getA(), entry.getB()));
        return map;
    }

    public static Map<RecipeCapability<?>, ChanceLogic> logicTuplesToMap(List<Tuple<RecipeCapability<?>, ChanceLogic>> entries) {
        Map<RecipeCapability<?>, ChanceLogic> map = new HashMap<>();
        entries.forEach(entry -> map.put(entry.getA(), entry.getB()));
        return map;
    }

    @NotNull
    public static GTRecipe fromNetwork(@NotNull RegistryFriendlyByteBuf buf) { return null; /* ported stub */ }

    public static void toNetwork(RegistryFriendlyByteBuf buf, GTRecipe recipe) {
        buf.writeIdentifier(recipe.recipeType.registryName);
        buf.writeIdentifier(recipe.id);
        buf.writeVarInt(recipe.duration);
        writeCollection(recipe.inputs.entrySet(), buf, GTRecipeSerializer::entryWriter);
        writeCollection(recipe.tickInputs.entrySet(), buf, GTRecipeSerializer::entryWriter);
        writeCollection(recipe.outputs.entrySet(), buf, GTRecipeSerializer::entryWriter);
        writeCollection(recipe.tickOutputs.entrySet(), buf, GTRecipeSerializer::entryWriter);
        writeCollectionWithMember(recipe.conditions, buf, RecipeCondition::toNetwork);

        writeCollection(recipe.inputChanceLogics.entrySet(), buf,
                GTRecipeSerializer::changeLogicEntryWriter);
        writeCollection(recipe.outputChanceLogics.entrySet(), buf,
                GTRecipeSerializer::changeLogicEntryWriter);
        writeCollection(recipe.tickInputChanceLogics.entrySet(), buf,
                GTRecipeSerializer::changeLogicEntryWriter);
        writeCollection(recipe.tickOutputChanceLogics.entrySet(), buf,
                GTRecipeSerializer::changeLogicEntryWriter);

        if (GTCEu.Mods.isKubeJSLoaded()) {
            KJSCallWrapper.writeIngredientActions(recipe.ingredientActions, buf);
        }
        buf.writeNbt(recipe.data);
        buf.writeInt(recipe.groupColor);
        buf.writeIdentifier(recipe.recipeCategory.registryKey);
    }

    public static <T> ArrayList<T> readCollection(RegistryFriendlyByteBuf buf,
                                                  StreamDecoder<? super RegistryFriendlyByteBuf, T> decoder) {
        int len = buf.readVarInt();
        var list = new ArrayList<T>(len);

        for (int i = 0; i < len; i++) {
            list.add(decoder.decode(buf));
        }
        return list;
    }

    public static <T> void writeCollection(Collection<T> collection, RegistryFriendlyByteBuf buf,
                                           StreamEncoder<? super RegistryFriendlyByteBuf, T> encoder) {
        buf.writeVarInt(collection.size());

        for (T t : collection) {
            encoder.encode(buf, t);
        }
    }

    public static <T> void writeCollectionWithMember(Collection<T> collection, RegistryFriendlyByteBuf buf,
                                                     StreamMemberEncoder<? super RegistryFriendlyByteBuf, T> encoder) {
        buf.writeVarInt(collection.size());

        for (T t : collection) {
            encoder.encode(t, buf);
        }
    }

    private static MapCodec<GTRecipe> makeCodec(boolean isKubeLoaded) { return null; /* ported stub */ }

    public static class KJSCallWrapper {/* ported: removed */

        public static List<?> getIngredientActions(RegistryFriendlyByteBuf buf) { return null; /* ported stub */ }

        public static void writeIngredientActions(List<?> ingredientActions, RegistryFriendlyByteBuf buf) {  /* ported stub */ }
    }
}
