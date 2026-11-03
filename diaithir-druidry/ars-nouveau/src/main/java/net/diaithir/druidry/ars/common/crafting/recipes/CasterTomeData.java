package net.diaithir.druidry.ars.common.crafting.recipes;

import net.diaithir.druidry.ars.api.registry.GlyphRegistry;
import net.diaithir.druidry.ars.api.registry.ParticleColorRegistry;
import net.diaithir.druidry.ars.api.registry.SpellCasterRegistry;
import net.diaithir.druidry.ars.api.sound.ConfiguredSpellSound;
import net.diaithir.druidry.ars.api.spell.AbstractCaster;
import net.diaithir.druidry.ars.api.spell.AbstractSpellPart;
import net.diaithir.druidry.ars.api.spell.Spell;
import net.diaithir.druidry.ars.client.particle.ParticleColor;
import net.diaithir.druidry.ars.setup.registry.ItemsRegistry;
import net.diaithir.druidry.ars.setup.registry.RecipeRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

import java.util.List;

public record CasterTomeData(String name, List<Identifier> spell, Identifier tomeType, String flavorText, ParticleColor particleColor, ConfiguredSpellSound sound) implements SpecialSingleInputRecipe {



    public CasterTomeData(String name,
                          List<Identifier> spell,
                          Identifier type,
                          String flavorText,
                          CompoundTag particleColor, ConfiguredSpellSound sound) {
        this(name, spell, type, flavorText, ParticleColorRegistry.from(particleColor), sound);
    }

    public static ItemStack makeTome(Item tome, String name, Spell spell, String flavorText) {
        ItemStack stack = tome.getDefaultInstance();
        AbstractCaster<?> spellCaster = SpellCasterRegistry.from(stack);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name).setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE).withItalic(true)));
        spellCaster.setSpell(spell).setFlavorText(flavorText).saveToStack(stack);
        return stack;
    }

    @Override
    public boolean matches(SingleRecipeInput p_346065_, Level p_345375_) {
        return false;
    }

        public ItemStack getResultItem(HolderLookup.Provider p_267052_) {
        Item tomeType = BuiltInRegistries.ITEM.getValue(this.tomeType);
        if (tomeType == Items.AIR)
            tomeType = ItemsRegistry.CASTER_TOME.asItem();
        var spell = new Spell().mutable();
        spell.name = this.name;
        if (this.particleColor != null)
            spell.color = this.particleColor;
        for (Identifier rl : this.spell) {
            AbstractSpellPart part = GlyphRegistry.getSpellpartMap().get(rl);
            if (part != null)
                spell.recipe.add(part);
        }
        spell.sound = sound;
        return makeTome(tomeType, name, spell.immutable(), flavorText);
    }

    @Override
    public RecipeSerializer<? extends CasterTomeData> getSerializer() {
        return Serializer.SERIALIZER;
    }

    @Override
    public RecipeType<? extends CasterTomeData> getType() {
        return RecipeRegistry.CASTER_TOME_TYPE.get();
    }

    public static class Serializer {
        public static MapCodec<CasterTomeData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("name").forGetter(CasterTomeData::name),
                Codec.list(Identifier.CODEC).fieldOf("spell").forGetter(CasterTomeData::spell),
                Identifier.CODEC.fieldOf("tome_type").forGetter(CasterTomeData::tomeType),
                Codec.STRING.fieldOf("flavour_text").forGetter(CasterTomeData::flavorText),
                ParticleColor.CODEC.fieldOf("color").forGetter(CasterTomeData::particleColor),
                ConfiguredSpellSound.CODEC.fieldOf("sound").forGetter(CasterTomeData::sound)
        ).apply(instance, CasterTomeData::new));

        public static StreamCodec<RegistryFriendlyByteBuf, CasterTomeData> STREAM = CheatSerializer.create(CODEC);

        public static final RecipeSerializer<CasterTomeData> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM);



    }
}
