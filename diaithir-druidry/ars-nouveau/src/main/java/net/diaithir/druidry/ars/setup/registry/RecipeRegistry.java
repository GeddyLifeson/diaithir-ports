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














    public static final DeferredHolder<RecipeType<?>, RecipeType<EnchantingApparatusRecipe>> APPARATUS_TYPE = RECIPE_TYPES.register(ENCHANTING_APPARATUS_RECIPE_ID, () -> RecipeType.simple(ArsNouveau.prefix(ENCHANTING_APPARATUS_RECIPE_ID)));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<EnchantingApparatusRecipe>> APPARATUS_SERIALIZER = RECIPE_SERIALIZERS.register(ENCHANTING_APPARATUS_RECIPE_ID, () -> EnchantingApparatusRecipe.Serializer.SERIALIZER);
    public static final DeferredHolder<RecipeType<?>, RecipeType<EnchantmentRecipe>> ENCHANTMENT_TYPE = RECIPE_TYPES.register(ENCHANTMENT_RECIPE_ID, () -> RecipeType.simple(ArsNouveau.prefix(ENCHANTMENT_RECIPE_ID)));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<EnchantmentRecipe>> ENCHANTMENT_SERIALIZER = RECIPE_SERIALIZERS.register(ENCHANTMENT_RECIPE_ID, () -> EnchantmentRecipe.Serializer.SERIALIZER);
    public static final DeferredHolder<RecipeType<?>, RecipeType<CrushRecipe>> CRUSH_TYPE = RECIPE_TYPES.register(CRUSH_RECIPE_ID, () -> RecipeType.simple(ArsNouveau.prefix(CRUSH_RECIPE_ID)));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CrushRecipe>> CRUSH_SERIALIZER = RECIPE_SERIALIZERS.register(CRUSH_RECIPE_ID, () -> CrushRecipe.Serializer.SERIALIZER);
    public static final DeferredHolder<RecipeType<?>, RecipeType<ReactiveEnchantmentRecipe>> REACTIVE_TYPE = RECIPE_TYPES.register(REACTIVE_RECIPE_ID, () -> RecipeType.simple(ArsNouveau.prefix(REACTIVE_RECIPE_ID)));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ReactiveEnchantmentRecipe>> REACTIVE_RECIPE = RECIPE_SERIALIZERS.register(REACTIVE_RECIPE_ID, () -> ReactiveEnchantmentRecipe.Serializer.SERIALIZER);
    public static final DeferredHolder<RecipeType<?>, RecipeType<SpellWriteRecipe>> SPELL_WRITE_TYPE = RECIPE_TYPES.register(SPELL_WRITE_RECIPE_ID, () -> RecipeType.simple(ArsNouveau.prefix(SPELL_WRITE_RECIPE_ID)));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<SpellWriteRecipe>> SPELL_WRITE_RECIPE = RECIPE_SERIALIZERS.register(SPELL_WRITE_RECIPE_ID, () -> SpellWriteRecipe.Serializer.SERIALIZER);
    public static final DeferredHolder<RecipeType<?>, RecipeType<ArmorUpgradeRecipe>> ARMOR_UPGRADE_TYPE = RECIPE_TYPES.register(ARMOR_RECIPE_ID, () -> RecipeType.simple(ArsNouveau.prefix(ARMOR_RECIPE_ID)));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ArmorUpgradeRecipe>> ARMOR_SERIALIZER = RECIPE_SERIALIZERS.register(ARMOR_RECIPE_ID, () -> ArmorUpgradeRecipe.Serializer.SERIALIZER);
    public static final DeferredHolder<RecipeType<?>, RecipeType<CasterTomeData>> CASTER_TOME_TYPE = RECIPE_TYPES.register(TOME_DATAPACK, () -> RecipeType.simple(ArsNouveau.prefix(TOME_DATAPACK)));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CasterTomeData>> CASTER_TOME_SERIALIZER = RECIPE_SERIALIZERS.register(TOME_DATAPACK, () -> CasterTomeData.Serializer.SERIALIZER);
    public static final DeferredHolder<RecipeType<?>, RecipeType<SummonRitualRecipe>> SUMMON_RITUAL_TYPE = RECIPE_TYPES.register(SUMMON_RITUAL_DATAPACK, () -> RecipeType.simple(ArsNouveau.prefix(SUMMON_RITUAL_DATAPACK)));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<SummonRitualRecipe>> SUMMON_RITUAL_SERIALIZER = RECIPE_SERIALIZERS.register(SUMMON_RITUAL_DATAPACK, () -> SummonRitualRecipe.Serializer.SERIALIZER);
    public static final DeferredHolder<RecipeType<?>, RecipeType<BuddingConversionRecipe>> BUDDING_CONVERSION_TYPE = RECIPE_TYPES.register(BUDDING_CONVERSION_RECIPE_ID, () -> RecipeType.simple(ArsNouveau.prefix(BUDDING_CONVERSION_RECIPE_ID)));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BuddingConversionRecipe>> BUDDING_CONVERSION_SERIALIZER = RECIPE_SERIALIZERS.register(BUDDING_CONVERSION_RECIPE_ID, () -> BuddingConversionRecipe.Serializer.SERIALIZER);
    public static final DeferredHolder<RecipeType<?>, RecipeType<DispelEntityRecipe>> DISPEL_ENTITY_TYPE = RECIPE_TYPES.register(DISPEL_ENTITY_RECIPE_ID, () -> RecipeType.simple(ArsNouveau.prefix(DISPEL_ENTITY_RECIPE_ID)));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DispelEntityRecipe>> DISPEL_ENTITY_SERIALIZER = RECIPE_SERIALIZERS.register(DISPEL_ENTITY_RECIPE_ID, () -> DispelEntityRecipe.Serializer.SERIALIZER);
    public static final DeferredHolder<RecipeType<?>, RecipeType<ScryRitualRecipe>> SCRY_RITUAL_TYPE = RECIPE_TYPES.register(SCRY_RITUAL_RECIPE_ID, () -> RecipeType.simple(ArsNouveau.prefix(SCRY_RITUAL_RECIPE_ID)));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ScryRitualRecipe>> SCRY_RITUAL_SERIALIZER = RECIPE_SERIALIZERS.register(SCRY_RITUAL_RECIPE_ID, () -> ScryRitualRecipe.Serializer.SERIALIZER);
    public static final DeferredHolder<RecipeType<?>, RecipeType<BookUpgradeRecipe>> BOOK_UPGRADE_TYPE = RECIPE_TYPES.register(BOOK_UPGRADE_RECIPE_ID, () -> RecipeType.simple(ArsNouveau.prefix(BOOK_UPGRADE_RECIPE_ID)));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BookUpgradeRecipe>> BOOK_UPGRADE_RECIPE = RECIPE_SERIALIZERS.register(BOOK_UPGRADE_RECIPE_ID, () -> ExtendableShapelessSerializer.create(BookUpgradeRecipe::new));
    public static final DeferredHolder<RecipeType<?>, RecipeType<PotionFlaskRecipe>> POTION_FLASK_TYPE = RECIPE_TYPES.register(POTION_FLASK_RECIPE_ID, () -> RecipeType.simple(ArsNouveau.prefix(POTION_FLASK_RECIPE_ID)));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<PotionFlaskRecipe>> POTION_FLASK_RECIPE = RECIPE_SERIALIZERS.register(POTION_FLASK_RECIPE_ID, () -> ExtendableShapelessSerializer.create(PotionFlaskRecipe::new));
    public static final DeferredHolder<RecipeType<?>, RecipeType<DyeRecipe>> DYE_TYPE = RECIPE_TYPES.register(DYE_RECIPE_ID, () -> RecipeType.simple(ArsNouveau.prefix(DYE_RECIPE_ID)));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DyeRecipe>> DYE_RECIPE = RECIPE_SERIALIZERS.register(DYE_RECIPE_ID, () -> ExtendableShapelessSerializer.create(DyeRecipe::new));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GlyphRecipe>> GLYPH_SERIALIZER = RECIPE_SERIALIZERS.register(GLYPH_RECIPE_ID, () -> GlyphRecipe.Serializer.SERIALIZER);

    public static final DeferredHolder<RecipeType<?>, RecipeType<GlyphRecipe>> GLYPH_TYPE = RECIPE_TYPES.register(GLYPH_RECIPE_ID, () -> RecipeType.simple(ArsNouveau.prefix(GLYPH_RECIPE_ID)));








    public static class ModRecipeType<T extends Recipe<?>> implements RecipeType<T> {
        @Override
        public String toString() {
            return BuiltInRegistries.RECIPE_TYPE.getKey(this).toString();
        }
    }

}
