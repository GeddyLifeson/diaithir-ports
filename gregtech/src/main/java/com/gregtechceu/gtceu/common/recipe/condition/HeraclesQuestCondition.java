package com.gregtechceu.gtceu.common.recipe.condition;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.common.data.GTRecipeConditions;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;

import net.minecraft.network.chat.Component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;



import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor
public class HeraclesQuestCondition extends RecipeCondition<HeraclesQuestCondition> {

    // spotless:off
    public static final MapCodec<HeraclesQuestCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> RecipeCondition.isReverse(instance).and(
            Codec.STRING.fieldOf("questId").forGetter(val -> val.questId)
    ).apply(instance, HeraclesQuestCondition::new));
    // spotless:on

    private String questId;

    public HeraclesQuestCondition(String questId) {
        this.questId = questId;
    }

    public HeraclesQuestCondition(boolean isReverse, String questId) {
        super(isReverse);
        this.questId = questId;
    }

    @Override
    public RecipeConditionType<HeraclesQuestCondition> getType() {
        return GTRecipeConditions.HERACLES_QUEST;
    }

    @Override
    public Component getTooltips() { return null; /* ported stub */ }

    @Override
    public boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) { return false; /* ported stub */ }

    @Override
    public HeraclesQuestCondition createTemplate() {
        return new HeraclesQuestCondition();
    }
}
