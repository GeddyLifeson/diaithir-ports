package net.diaithir.druidry.ars.common.crafting.recipes;

import net.diaithir.druidry.ars.api.registry.SpellCasterRegistry;
import net.diaithir.druidry.ars.api.spell.AbstractCaster;
import net.diaithir.druidry.ars.common.items.SpellParchment;
import net.diaithir.druidry.ars.common.items.data.ReactiveCasterData;
import net.diaithir.druidry.ars.setup.registry.DataComponentRegistry;
import net.diaithir.druidry.ars.setup.registry.EnchantmentRegistry;
import net.diaithir.druidry.ars.setup.registry.RecipeRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ReactiveEnchantmentRecipe extends EnchantmentRecipe {

    public ReactiveEnchantmentRecipe(List<Ingredient> pedestalItems, int sourceCost) {
        super(pedestalItems, EnchantmentRegistry.REACTIVE_ENCHANTMENT, 1, sourceCost);
    }

    @Override
    public @NotNull ItemStack assemble(ApparatusRecipeInput input) {
        ItemStack result = super.assemble(input);
        ItemStack parchment = getParchment(input.pedestals());
        AbstractCaster<?> parchmentCaster = SpellCasterRegistry.from(parchment);
        result.set(DataComponentRegistry.REACTIVE_CASTER, new ReactiveCasterData(0, "", false, "", 1)
                .setColor(parchmentCaster.getColor())
                .setSpell(parchmentCaster.getSpell()));
        return result;
    }

    @Override
    public boolean matches(ApparatusRecipeInput input, Level level, @org.jetbrains.annotations.Nullable Player player) {
        ItemStack parchment = getParchment(input.pedestals());
        return super.matches(input, level, player) && !parchment.isEmpty() && !SpellCasterRegistry.from(parchment).getSpell().isEmpty();
    }

    public static@NotNull ItemStack getParchment(List<ItemStack> pedestalItems) {
        for (ItemStack stack : pedestalItems) {
            if (stack.getItem() instanceof SpellParchment) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull RecipeType<? extends ReactiveEnchantmentRecipe> getType() {
        return RecipeRegistry.REACTIVE_TYPE.get();
    }

    @Override
    public @NotNull RecipeSerializer<? extends ReactiveEnchantmentRecipe> getSerializer() {
        return Serializer.SERIALIZER;
    }


    public static class Serializer {

        public static final MapCodec<ReactiveEnchantmentRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.listOf().fieldOf("pedestalItems").forGetter(ReactiveEnchantmentRecipe::pedestalItems),
                Codec.INT.fieldOf("sourceCost").forGetter(ReactiveEnchantmentRecipe::sourceCost)
        ).apply(instance, ReactiveEnchantmentRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ReactiveEnchantmentRecipe> STREAM = CheatSerializer.create(CODEC);

        public static final RecipeSerializer<ReactiveEnchantmentRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM);



    }
}
