package net.diaithir.druidry.ars.common.crafting.recipes;

import net.diaithir.druidry.ars.api.registry.SpellCasterRegistry;
import net.diaithir.druidry.ars.api.spell.AbstractCaster;
import net.diaithir.druidry.ars.common.items.data.ReactiveCasterData;
import net.diaithir.druidry.ars.common.util.HolderHelper;
import net.diaithir.druidry.ars.setup.registry.DataComponentRegistry;
import net.diaithir.druidry.ars.setup.registry.EnchantmentRegistry;
import net.diaithir.druidry.ars.setup.registry.RecipeRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.diaithir.druidry.ars.common.crafting.recipes.ReactiveEnchantmentRecipe.getParchment;

public class SpellWriteRecipe extends EnchantingApparatusRecipe implements ITextOutput {

    public SpellWriteRecipe(List<Ingredient> pedestalItems, int cost) {
        super(Ingredient.of(net.minecraft.world.item.Items.AIR), ItemStack.EMPTY, pedestalItems, cost, true);
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
        ItemEnchantments enchantments = input.catalyst().get(DataComponents.ENCHANTMENTS);
        int level1 = enchantments.getLevel(HolderHelper.unwrap(level, EnchantmentRegistry.REACTIVE_ENCHANTMENT));
        ItemStack parchment = getParchment(input.pedestals());
        return !parchment.isEmpty() && !SpellCasterRegistry.from(parchment).getSpell().isEmpty() && level1 > 0 && super.matches(input, level, player);

    }

    @Override
    public @NotNull ItemStack assemble(ApparatusRecipeInput input) {
        ItemStack parchment = getParchment(input.pedestals());
        AbstractCaster<?> caster = SpellCasterRegistry.from(parchment);
        ItemStack result = input.catalyst().copy();
        ReactiveCasterData data = new ReactiveCasterData(0, null, false, null, 1, caster.getSpells())
                .setColor(caster.getColor(), 0);
        result.set(DataComponentRegistry.REACTIVE_CASTER, data);
        return result;
    }

    @Override
    public @NotNull RecipeType<? extends SpellWriteRecipe> getType() {
        return RecipeRegistry.SPELL_WRITE_TYPE.get();
    }

    @Override
    public Component getOutputComponent() {
        return Component.translatable("ars_nouveau.spell_write.book_desc");
    }


    @Override
    public @NotNull RecipeSerializer<? extends SpellWriteRecipe> getSerializer() {
        return Serializer.SERIALIZER;
    }


    public static class Serializer {

        public static final MapCodec<SpellWriteRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.listOf().fieldOf("pedestalItems").forGetter(SpellWriteRecipe::pedestalItems),
                Codec.INT.fieldOf("sourceCost").forGetter(SpellWriteRecipe::sourceCost)
        ).apply(instance, SpellWriteRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, SpellWriteRecipe> STREAM = CheatSerializer.create(CODEC);

        public static final RecipeSerializer<SpellWriteRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM);



    }
}
