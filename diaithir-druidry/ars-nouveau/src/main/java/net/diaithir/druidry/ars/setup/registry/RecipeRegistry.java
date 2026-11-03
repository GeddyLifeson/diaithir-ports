package net.diaithir.druidry.ars.setup.registry;

import net.diaithir.druidry.ars.ArsNouveau;
import net.diaithir.druidry.ars.common.crafting.recipes.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static net.diaithir.druidry.ars.ArsNouveau.MODID;

@SuppressWarnings("Convert2MethodRef")
public class RecipeRegistry {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, MODID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, MODID);

    public static final String ENCHANTING_APPARATUS_RECIPE_ID = "enchanting_apparatus";
    public static final String ENCHANTMENT_RECIPE_ID = "enchantment";
    public static final String CRUSH_RECIPE_ID = "crush";
    public static final String IMBUEMENT_RECIPE_ID = "imbuement";
    public static final String REACTIVE_RECIPE_ID = "reactive_enchantment";
    public static final String SPELL_WRITE_RECIPE_ID = "spell_write";
    public static final String GLYPH_RECIPE_ID = "glyph";
    public static final String DYE_RECIPE_ID = "dye";
    public static final String ARMOR_RECIPE_ID = "armor_upgrade";
    public static final String TOME_DATAPACK = "caster_tome";
    public static final String SUMMON_RITUAL_DATAPACK = "summon_ritual";
    public static final String BUDDING_CONVERSION_RECIPE_ID = "budding_conversion";
    public static final String ALAKARKINOS_RECIPE_ID = "alakarkinos_conversion";
    public static final String DISPEL_ENTITY_RECIPE_ID = "dispel_entity";
    public static final String SCRY_RITUAL_RECIPE_ID = "scry_ritual";
    public static final String BOOK_UPGRADE_RECIPE_ID = "book_upgrade";
    public static final String POTION_FLASK_RECIPE_ID = "potion_flask";
    public static final String PRESTIDIGITATION_RECIPE_ID = "prestidigitation";














    public static final DeferredHolder<RecipeType<?>, RecipeType<GlyphRecipe>> GLYPH_TYPE = RECIPE_TYPES.register(GLYPH_RECIPE_ID, () -> RecipeType.simple(ArsNouveau.prefix(GLYPH_RECIPE_ID)));








    public static class ModRecipeType<T extends Recipe<?>> implements RecipeType<T> {
        @Override
        public String toString() {
            return BuiltInRegistries.RECIPE_TYPE.getKey(this).toString();
        }
    }

}
