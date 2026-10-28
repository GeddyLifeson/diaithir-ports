package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.datacomponents.SimpleEnergyContent;
import com.gregtechceu.gtceu.common.data.GTRecipeSerializers;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;

import com.gregtechceu.gtceu.utils.codec.StreamCodecUtils;

import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import org.jetbrains.annotations.NotNullByDefault;

@NotNullByDefault
public class ShapedEnergyTransferRecipe extends ShapedRecipe {

    @Getter
    private final Ingredient chargeIngredient;
    @Getter
    private final boolean transferMaxCharge;
    @Getter
    private final boolean overrideCharge;

    public ShapedEnergyTransferRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern,
                                      Ingredient chargeIngredient, boolean overrideCharge, boolean transferMaxCharge,
                                      ItemStack result, boolean showNotification) {
        super(group, category, pattern, result, showNotification);
        this.chargeIngredient = chargeIngredient;
        this.transferMaxCharge = transferMaxCharge;
        this.overrideCharge = overrideCharge;
    }

    @Override
    public ItemStack assemble(CraftingInput craftingContainer, HolderLookup.Provider provider) { return null; /* ported stub */ }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) { return null; /* ported stub */ }

    @Override
    public RecipeSerializer<?> getSerializer() { return null; /* ported stub */ }

    public static class Serializer {

        public static final MapCodec<ShapedEnergyTransferRecipe> CODEC = RecordCodecBuilder
                .mapCodec(instance -> instance.group(
                        Codec.STRING.optionalFieldOf("group", "").forGetter(ShapedRecipe::getGroup),
                        CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC)
                                .forGetter(ShapedRecipe::category),
                        ShapedRecipePattern.MAP_CODEC.forGetter(val -> val.pattern),
                        Ingredient.CODEC.fieldOf("chargeIngredient")
                                .forGetter(ShapedEnergyTransferRecipe::getChargeIngredient),
                        Codec.BOOL.fieldOf("overrideCharge").forGetter(ShapedEnergyTransferRecipe::isOverrideCharge),
                        Codec.BOOL.fieldOf("transferMaxCharge")
                                .forGetter(ShapedEnergyTransferRecipe::isTransferMaxCharge),
                        ItemStack.CODEC.fieldOf("result").forGetter(val -> ((ShapedRecipeAccessor) val).getResult()),
                        Codec.BOOL.optionalFieldOf("show_notification", true)
                                .forGetter(val -> ((ShapedRecipeAccessor) val).getShowNotification()))
                        .apply(instance, ShapedEnergyTransferRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, ShapedEnergyTransferRecipe> STREAM_CODEC = StreamCodecUtils
                .composite(
                        ByteBufCodecs.STRING_UTF8, ShapedRecipe::getGroup,
                        CraftingBookCategory.STREAM_CODEC, ShapedRecipe::category,
                        ShapedRecipePattern.STREAM_CODEC, val -> val.pattern,
                        Ingredient.CONTENTS_STREAM_CODEC, ShapedEnergyTransferRecipe::getChargeIngredient,
                        ByteBufCodecs.BOOL, ShapedEnergyTransferRecipe::isOverrideCharge,
                        ByteBufCodecs.BOOL, ShapedEnergyTransferRecipe::isTransferMaxCharge,
                        ItemStack.STREAM_CODEC, val -> ((ShapedRecipeAccessor) val).getResult(),
                        ByteBufCodecs.BOOL, ShapedRecipe::showNotification,
                        ShapedEnergyTransferRecipe::new);

        @Override
        public MapCodec<ShapedEnergyTransferRecipe> codec() { return null; /* ported stub */ }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ShapedEnergyTransferRecipe> streamCodec() { return null; /* ported stub */ }
    }
}
