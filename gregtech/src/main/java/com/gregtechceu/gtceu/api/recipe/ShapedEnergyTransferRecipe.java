package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.datacomponents.SimpleEnergyContent;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredientExtensions;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class ShapedEnergyTransferRecipe extends ShapedRecipe {

    @Getter
    private final Ingredient chargeIngredient;
    @Getter
    private final boolean transferMaxCharge;
    @Getter
    private final boolean overrideCharge;

    public ShapedEnergyTransferRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo,
                                      ShapedRecipePattern pattern, Ingredient chargeIngredient,
                                      boolean overrideCharge, boolean transferMaxCharge, ItemStackTemplate result) {
        super(commonInfo, bookInfo, pattern, result);
        this.chargeIngredient = chargeIngredient;
        this.transferMaxCharge = transferMaxCharge;
        this.overrideCharge = overrideCharge;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CraftingInput craftingContainer) {
        long maxCharge = 0L;
        long charge = 0L;
        ItemStack resultStack = super.assemble(craftingContainer);
        for (ItemStack chargeStack : SizedIngredientExtensions.getItems(chargeIngredient)) {
            for (int i = 0; i < craftingContainer.size(); i++) {
                if (ItemStack.isSameItem(craftingContainer.getItem(i), chargeStack)) {
                    ItemStack stack = craftingContainer.getItem(i);
                    IElectricItem electricItem = GTCapabilityHelper.getElectricItem(stack);
                    if (electricItem != null) {
                        maxCharge += electricItem.getMaxCharge();
                        charge += electricItem.getCharge();
                        resultStack.set(GTDataComponents.ENERGY_CONTENT, new SimpleEnergyContent(maxCharge, charge));
                        return resultStack;
                    }
                }
            }
        }
        return resultStack;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull RecipeSerializer<ShapedRecipe> getSerializer() {
        return (RecipeSerializer<ShapedRecipe>) (RecipeSerializer<?>) SERIALIZER_ENERGY;
    }

    public static final MapCodec<ShapedEnergyTransferRecipe> CODEC = RecordCodecBuilder
            .mapCodec(instance -> instance.group(
                    Recipe.CommonInfo.MAP_CODEC.forGetter(o -> o.commonInfo),
                    CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(o -> o.bookInfo),
                    ShapedRecipePattern.MAP_CODEC.forGetter(val -> val.pattern),
                    Ingredient.CODEC.fieldOf("chargeIngredient")
                            .forGetter(ShapedEnergyTransferRecipe::getChargeIngredient),
                    Codec.BOOL.fieldOf("overrideCharge").forGetter(ShapedEnergyTransferRecipe::isOverrideCharge),
                    Codec.BOOL.fieldOf("transferMaxCharge")
                            .forGetter(ShapedEnergyTransferRecipe::isTransferMaxCharge),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(val -> val.result))
                    .apply(instance, ShapedEnergyTransferRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ShapedEnergyTransferRecipe> STREAM_CODEC = StreamCodec
            .of((buf, r) -> {
                Recipe.CommonInfo.STREAM_CODEC.encode(buf, r.commonInfo);
                CraftingRecipe.CraftingBookInfo.STREAM_CODEC.encode(buf, r.bookInfo);
                ShapedRecipePattern.STREAM_CODEC.encode(buf, r.pattern);
                Ingredient.CONTENTS_STREAM_CODEC.encode(buf, r.chargeIngredient);
                buf.writeBoolean(r.overrideCharge);
                buf.writeBoolean(r.transferMaxCharge);
                ItemStackTemplate.STREAM_CODEC.encode(buf, r.result);
            }, buf -> new ShapedEnergyTransferRecipe(
                    Recipe.CommonInfo.STREAM_CODEC.decode(buf),
                    CraftingRecipe.CraftingBookInfo.STREAM_CODEC.decode(buf),
                    ShapedRecipePattern.STREAM_CODEC.decode(buf),
                    Ingredient.CONTENTS_STREAM_CODEC.decode(buf),
                    buf.readBoolean(), buf.readBoolean(),
                    ItemStackTemplate.STREAM_CODEC.decode(buf)));
    public static final RecipeSerializer<ShapedEnergyTransferRecipe> SERIALIZER_ENERGY = new RecipeSerializer<>(CODEC,
            STREAM_CODEC);
}
