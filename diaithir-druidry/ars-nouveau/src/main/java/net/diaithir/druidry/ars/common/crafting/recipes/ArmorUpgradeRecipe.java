package net.diaithir.druidry.ars.common.crafting.recipes;

import net.diaithir.druidry.ars.api.perk.IPerkHolder;
import net.diaithir.druidry.ars.api.util.PerkUtil;
import net.diaithir.druidry.ars.common.items.data.ArmorPerkHolder;
import net.diaithir.druidry.ars.setup.registry.DataComponentRegistry;
import net.diaithir.druidry.ars.setup.registry.RecipeRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ArmorUpgradeRecipe extends EnchantingApparatusRecipe implements ITextOutput{

    public int tier; // 0 indexed

//    public ArmorUpgradeRecipe(List<Ingredient> pedestalItems, int cost, int tier) {
//        this(ArsNouveau.prefix( "upgrade_" + tier), pedestalItems, cost, tier);
//    }

    public ArmorUpgradeRecipe(List<Ingredient> pedestalItems, int cost, int tier) {
        super(Ingredient.of(net.minecraft.world.item.Items.AIR), ItemStack.EMPTY, pedestalItems, cost, false);
        this.tier = tier;
    }

    public int tier(){
        return tier;
    }

    @Override
    public boolean excludeJei() {
        return true;
    }

    @Override
    public boolean doesReagentMatch(ApparatusRecipeInput input, Level level, @org.jetbrains.annotations.Nullable Player player) {
        return true;
    }

    @Override
    public boolean matches(ApparatusRecipeInput input, Level level, @org.jetbrains.annotations.Nullable Player player) {
        IPerkHolder perkHolder = PerkUtil.getPerkHolder(input.catalyst());
        if(!(perkHolder instanceof ArmorPerkHolder armorPerkHolder)){
            return false;
        }
        return armorPerkHolder.getTier() == (tier - 1) && super.matches(input, level, player);
    }

        public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider pRegistries) {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack assemble(ApparatusRecipeInput input) {
        ItemStack reagent = input.catalyst();
        ArmorPerkHolder perkHolder = PerkUtil.getPerkHolder(reagent);
        if(!(perkHolder instanceof ArmorPerkHolder armorPerkHolder)){
            return reagent.copy();
        }
        reagent.set(DataComponentRegistry.ARMOR_PERKS, armorPerkHolder.setTier(tier));
        return reagent.copy();
    }

    @Override
    public @NotNull RecipeType<? extends ArmorUpgradeRecipe> getType() {
        return RecipeRegistry.ARMOR_UPGRADE_TYPE.get();
    }

    @Override
    public Component getOutputComponent() {
        return Component.translatable("ars_nouveau.armor_upgrade.book_desc", tier);
    }

    @Override
    public @NotNull RecipeSerializer<? extends ArmorUpgradeRecipe> getSerializer() {
        return Serializer.SERIALIZER;
    }

    public static class Serializer {
        public static MapCodec<ArmorUpgradeRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.listOf().fieldOf("pedestalItems").forGetter(ArmorUpgradeRecipe::pedestalItems),
                Codec.INT.fieldOf("sourceCost").forGetter(ArmorUpgradeRecipe::sourceCost),
                Codec.INT.fieldOf("tier").forGetter(ArmorUpgradeRecipe::tier)
        ).apply(instance, ArmorUpgradeRecipe::new));

        public static StreamCodec<RegistryFriendlyByteBuf, ArmorUpgradeRecipe> STREAM_CODEC = CheatSerializer.create(CODEC);

        public static final RecipeSerializer<ArmorUpgradeRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);



    }
}
