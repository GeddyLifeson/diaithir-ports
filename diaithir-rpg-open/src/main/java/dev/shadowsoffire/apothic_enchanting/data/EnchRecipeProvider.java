package dev.shadowsoffire.apothic_enchanting.data;

import java.util.concurrent.CompletableFuture;

import dev.shadowsoffire.apothic_enchanting.ApothicEnchanting;
import dev.shadowsoffire.apothic_enchanting.Ench;
import dev.shadowsoffire.apothic_enchanting.table.EnchantingStatRegistry.Stats;
import dev.shadowsoffire.apothic_enchanting.table.infusion.InfusionRecipe;
import dev.shadowsoffire.apothic_enchanting.table.infusion.KeepNBTInfusionRecipe;
import dev.shadowsoffire.placebo.datagen.LegacyRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;

public class EnchRecipeProvider extends LegacyRecipeProvider {

    public EnchRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, ApothicEnchanting.MODID);
    }

    @Override
    public String getName() {
        return "Apothic Enchanting Recipes";
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void genRecipes(RecipeOutput recipeOutput, HolderLookup.Provider registries) {
        addShaped(Ench.Blocks.HELLSHELF, 3, 3,
            Blocks.NETHER_BRICKS, Blocks.NETHER_BRICKS, Blocks.NETHER_BRICKS,
            Items.BLAZE_ROD, Tags.Items.BOOKSHELVES, potionIngredient(Potions.REGENERATION),
            Blocks.NETHER_BRICKS, Blocks.NETHER_BRICKS, Blocks.NETHER_BRICKS);

        addShaped(Ench.Items.PRISMATIC_WEB, 3, 3,
            null, Items.PRISMARINE_SHARD, null,
            Items.PRISMARINE_SHARD, Blocks.COBWEB, Items.PRISMARINE_SHARD,
            null, Items.PRISMARINE_SHARD, null);

        addShaped(new ItemStackTemplate(Ench.Items.HELMET_TOME.value(), 5), 3, 2, Items.BOOK, Items.BOOK, Items.BOOK, Items.BOOK, Items.BLAZE_ROD, Items.BOOK);
        addShaped(new ItemStackTemplate(Ench.Items.CHESTPLATE_TOME.value(), 8), 3, 3, Items.BOOK, Items.BLAZE_ROD, Items.BOOK, Items.BOOK, Items.BOOK, Items.BOOK, Items.BOOK, Items.BOOK, Items.BOOK);
        addShaped(new ItemStackTemplate(Ench.Items.LEGGINGS_TOME.value(), 7), 3, 3, Items.BOOK, null, Items.BOOK, Items.BOOK, Items.BLAZE_ROD, Items.BOOK, Items.BOOK, Items.BOOK, Items.BOOK);
        addShaped(new ItemStackTemplate(Ench.Items.BOOTS_TOME.value(), 4), 3, 2, Items.BOOK, null, Items.BOOK, Items.BOOK, Items.BLAZE_ROD, Items.BOOK);
        addShaped(new ItemStackTemplate(Ench.Items.WEAPON_TOME.value(), 2), 1, 3, Items.BOOK, Items.BOOK, Items.BLAZE_POWDER);
        addShaped(new ItemStackTemplate(Ench.Items.PICKAXE_TOME.value(), 3), 3, 3, Items.BOOK, Items.BOOK, Items.BOOK, null, Items.BLAZE_ROD, null, null, Items.STICK, null);
        addShaped(new ItemStackTemplate(Ench.Items.FISHING_TOME.value(), 2), 3, 3, null, null, Items.BLAZE_ROD, null, Items.STICK, Items.BOOK, Items.STICK, null, Items.BOOK);
        addShaped(new ItemStackTemplate(Ench.Items.BOW_TOME.value(), 3), 3, 3, null, Items.STICK, Items.BOOK, Items.BLAZE_ROD, null, Items.BOOK, null, Items.STICK, Items.BOOK);
        addShapeless(new ItemStackTemplate(Ench.Items.OTHER_TOME.value(), 6), Items.BOOK, Items.BOOK, Items.BOOK, Items.BOOK, Items.BOOK, Items.BOOK, Items.BLAZE_ROD);
        addShaped(new ItemStackTemplate(Ench.Items.SCRAP_TOME.value(), 8), 3, 3, Items.BOOK, Items.BOOK, Items.BOOK, Items.BOOK, Blocks.ANVIL, Items.BOOK, Items.BOOK, Items.BOOK, Items.BOOK);

        addShaped(Ench.Blocks.BLAZING_HELLSHELF, 3, 3,
            null, Items.FIRE_CHARGE, null,
            Items.FIRE_CHARGE, Ench.Blocks.INFUSED_HELLSHELF, Items.FIRE_CHARGE,
            Items.BLAZE_POWDER, Items.BLAZE_POWDER, Items.BLAZE_POWDER);

        addShaped(Ench.Blocks.GLOWING_HELLSHELF, 3, 3,
            null, Blocks.GLOWSTONE, null,
            null, Ench.Blocks.INFUSED_HELLSHELF, null,
            Blocks.GLOWSTONE, null, Blocks.GLOWSTONE);

        addShaped(Ench.Blocks.SEASHELF, 3, 3,
            Blocks.PRISMARINE_BRICKS, Blocks.PRISMARINE_BRICKS, Blocks.PRISMARINE_BRICKS,
            potionIngredient(Potions.WATER), Tags.Items.BOOKSHELVES, Items.PUFFERFISH,
            Blocks.PRISMARINE_BRICKS, Blocks.PRISMARINE_BRICKS, Blocks.PRISMARINE_BRICKS);

        addShaped(Ench.Blocks.CRYSTAL_SEASHELF, 3, 3,
            null, Items.PRISMARINE_CRYSTALS, null,
            null, Ench.Blocks.INFUSED_SEASHELF, null,
            Items.PRISMARINE_CRYSTALS, null, Items.PRISMARINE_CRYSTALS);

        addShaped(Ench.Blocks.HEART_SEASHELF, 3, 3,
            null, Items.HEART_OF_THE_SEA, null,
            Items.PRISMARINE_SHARD, Ench.Blocks.INFUSED_SEASHELF, Items.PRISMARINE_SHARD,
            Items.PRISMARINE_SHARD, Items.PRISMARINE_SHARD, Items.PRISMARINE_SHARD);

        addShaped(Ench.Blocks.BEESHELF, 3, 3,
            Items.HONEYCOMB, Items.BEEHIVE, Items.HONEYCOMB,
            Items.HONEY_BLOCK, Tags.Items.BOOKSHELVES, Items.HONEY_BLOCK,
            Items.HONEYCOMB, Items.BEEHIVE, Items.HONEYCOMB);

        addShaped(Ench.Blocks.MELONSHELF, 3, 3,
            Items.MELON, Items.MELON, Items.MELON,
            Items.GLISTERING_MELON_SLICE, Tags.Items.BOOKSHELVES, Items.GLISTERING_MELON_SLICE,
            Items.MELON, Items.MELON, Items.MELON);

        addShaped(Ench.Blocks.BASIC_BOOKSHELF, 3, 3,
            ItemTags.PLANKS, ItemTags.PLANKS, ItemTags.PLANKS,
            EnchItemTagsProvider.TOMES, EnchItemTagsProvider.TOMES, EnchItemTagsProvider.TOMES,
            ItemTags.PLANKS, ItemTags.PLANKS, ItemTags.PLANKS);

        addShaped(Ench.Blocks.APOTHIC_ENCHANTING_TABLE, 3, 3,
            null, EnchItemTagsProvider.TOMES, null,
            Items.DIAMOND, Items.OBSIDIAN, Items.DIAMOND,
            Items.OBSIDIAN, Items.OBSIDIAN, Items.OBSIDIAN);

        addShaped(Ench.Blocks.GEODE_SHELF, 3, 3,
            Items.CALCITE, Items.CALCITE, Items.CALCITE,
            Items.CALCITE, Tags.Items.BOOKSHELVES, Items.CALCITE,
            Items.CALCITE, Items.BUDDING_AMETHYST, Items.CALCITE);

        addShaped(Ench.Blocks.SIGHTSHELF, 3, 3,
            Tags.Items.STORAGE_BLOCKS_GOLD, Ench.Items.INFUSED_HELLSHELF, Tags.Items.STORAGE_BLOCKS_GOLD,
            potionIngredient(Potions.NIGHT_VISION), Items.ENDER_EYE, Items.SPYGLASS,
            Tags.Items.STORAGE_BLOCKS_GOLD, Ench.Items.INFUSED_HELLSHELF, Tags.Items.STORAGE_BLOCKS_GOLD);

        Ingredient nightVisPot = potionIngredient(Potions.LONG_NIGHT_VISION);
        addShaped(Ench.Blocks.SIGHTSHELF_T2, 3, 3,
            Items.EMERALD_BLOCK, Tags.Items.INGOTS_NETHERITE, Items.EMERALD_BLOCK,
            nightVisPot, Ench.Items.SIGHTSHELF, nightVisPot,
            Items.EMERALD_BLOCK, Tags.Items.INGOTS_NETHERITE, Items.EMERALD_BLOCK);

        addShaped(Ench.Blocks.ENDSHELF, 3, 3,
            Items.END_STONE_BRICKS, Items.END_STONE_BRICKS, Items.END_STONE_BRICKS,
            Ench.Items.INFUSED_BREATH, Tags.Items.BOOKSHELVES, Tags.Items.ENDER_PEARLS,
            Items.END_STONE_BRICKS, Items.END_STONE_BRICKS, Items.END_STONE_BRICKS);

        addShaped(Ench.Blocks.PEARL_ENDSHELF, 3, 3,
            Items.END_ROD, null, Items.END_ROD,
            Tags.Items.ENDER_PEARLS, Ench.Items.ENDSHELF, Tags.Items.ENDER_PEARLS,
            Items.END_ROD, null, Items.END_ROD);

        addShaped(Ench.Blocks.DRACONIC_ENDSHELF, 3, 3,
            null, Items.DRAGON_HEAD, null,
            Tags.Items.ENDER_PEARLS, Ench.Items.ENDSHELF, Tags.Items.ENDER_PEARLS,
            Tags.Items.ENDER_PEARLS, Tags.Items.ENDER_PEARLS, Tags.Items.ENDER_PEARLS);

        addShaped(Items.COBWEB, 3, 3,
            Tags.Items.STRINGS, Tags.Items.STRINGS, Tags.Items.STRINGS,
            Tags.Items.STRINGS, Items.HONEYCOMB, Tags.Items.STRINGS,
            Tags.Items.STRINGS, Tags.Items.STRINGS, Tags.Items.STRINGS);

        addShaped(Ench.Items.TREASURE_SHELF, 3, 3,
            Tags.Items.STORAGE_BLOCKS_GOLD, Ench.Items.DEEPSHELF, Tags.Items.STORAGE_BLOCKS_GOLD,
            Tags.Items.GEMS_DIAMOND, Tags.Items.STORAGE_BLOCKS_EMERALD, Tags.Items.GEMS_DIAMOND,
            Tags.Items.STORAGE_BLOCKS_GOLD, Ench.Items.DEEPSHELF, Tags.Items.STORAGE_BLOCKS_GOLD);

        addShaped(Ench.Items.INERT_TRIDENT, 3, 3,
            Items.NAUTILUS_SHELL, Items.NAUTILUS_SHELL, Items.NAUTILUS_SHELL,
            null, Items.HEART_OF_THE_SEA, null,
            null, Tags.Items.INGOTS_IRON, null);

        ItemStackTemplate pufferfish = new ItemStackTemplate(
            Items.PUFFERFISH.builtInRegistryHolder(),
            1,
            DataComponentPatch.builder()
                .set(DataComponents.CUSTOM_NAME,
                    Component.translatable("\"%s\"", Component.translatable(Items.PUFFERFISH.getDescriptionId()))
                        .withStyle(Style.EMPTY.withItalic(false)))
                .build());
        addShaped(pufferfish, 3, 3,
            null, Items.BAMBOO, null,
            Items.BAMBOO, ItemTags.FISHES, Items.BAMBOO,
            null, Items.BAMBOO, null);

        addShapeless(Ench.Items.FLIMSY_ENDER_LEAD, Tags.Items.ENDER_PEARLS, Items.LEAD, Items.GOLD_INGOT);

        addShaped(Ench.Blocks.STONESHELF, 3, 3,
            Items.POLISHED_ANDESITE, Items.POLISHED_ANDESITE, Items.POLISHED_ANDESITE,
            Items.BOOK, Items.BOOK, Items.BOOK,
            Items.POLISHED_ANDESITE, Items.POLISHED_ANDESITE, Items.POLISHED_ANDESITE);

        addShaped(Ench.Blocks.DORMANT_DEEPSHELF, 3, 3,
            Items.CRACKED_DEEPSLATE_TILES, Items.CRACKED_DEEPSLATE_TILES, Items.CRACKED_DEEPSLATE_TILES,
            Items.BOOK, Items.BOOK, Items.BOOK,
            Items.CRACKED_DEEPSLATE_TILES, Items.CRACKED_DEEPSLATE_TILES, Items.CRACKED_DEEPSLATE_TILES);

        addShaped(Ench.Blocks.ECHOING_DEEPSHELF, 3, 3,
            null, Items.ECHO_SHARD, null,
            null, Ench.Blocks.DEEPSHELF, null,
            ItemTags.CANDLES, ItemTags.CANDLES, ItemTags.CANDLES);

        addShaped(Ench.Blocks.SOUL_TOUCHED_DEEPSHELF, 3, 3,
            null, Items.SOUL_LANTERN, null,
            null, Ench.Blocks.DEEPSHELF, null,
            Items.SCULK, Items.SCULK, Items.SCULK);

        addShaped(Ench.Blocks.ECHOING_SCULKSHELF, 3, 3,
            null, Ench.Items.WARDEN_TENDRIL, null,
            Items.SCULK, Ench.Blocks.ECHOING_DEEPSHELF, Items.SCULK,
            Items.SCULK, Items.SCULK_CATALYST, Items.SCULK);

        addShaped(Ench.Blocks.SOUL_TOUCHED_SCULKSHELF, 3, 3,
            null, Ench.Items.WARDEN_TENDRIL, null,
            Items.SCULK, Ench.Blocks.SOUL_TOUCHED_DEEPSHELF, Items.SCULK,
            Items.SCULK, Items.SCULK_CATALYST, Items.SCULK);

        addShaped(Ench.Blocks.FILTERING_SHELF, 3, 3,
            Items.PRISMARINE_BRICKS, Ench.Blocks.INFUSED_SEASHELF, Items.PRISMARINE_BRICKS,
            Items.PRISMARINE_BRICK_SLAB, Items.PRISMARINE_BRICK_SLAB, Items.PRISMARINE_BRICK_SLAB,
            Items.PRISMARINE_BRICKS, Ench.Blocks.INFUSED_SEASHELF, Items.PRISMARINE_BRICKS);

        addShaped(Ench.Blocks.LIBRARY, 3, 3,
            Items.ENDER_CHEST, "apothic_enchanting:infused_shelves", Items.ENDER_CHEST,
            "apothic_enchanting:infused_shelves", Items.ENCHANTING_TABLE, "apothic_enchanting:infused_shelves",
            Items.ENDER_CHEST, "apothic_enchanting:infused_shelves", Items.ENDER_CHEST);

        addInfusion("infusion/deepshelf",
            new ItemStackTemplate(Ench.Blocks.DEEPSHELF.value().asItem()),
            Ench.Blocks.DORMANT_DEEPSHELF,
            req(60, 40, 40));

        addInfusion("infusion/infused_hellshelf",
            new ItemStackTemplate(Ench.Blocks.INFUSED_HELLSHELF.value().asItem()),
            Ench.Blocks.HELLSHELF,
            req(45, 30, 0));

        addInfusion("infusion/infused_seashelf",
            new ItemStackTemplate(Ench.Blocks.INFUSED_SEASHELF.value().asItem()),
            Ench.Blocks.SEASHELF,
            req(45, 15, 10));

        addInfusion("infusion/infused_breath",
            new ItemStackTemplate(Ench.Items.INFUSED_BREATH.value(), 3),
            Items.DRAGON_BREATH,
            req(80, 15, 60),
            max(-1, 30, -1));

        addInfusion("infusion/improved_scrap_tome",
            new ItemStackTemplate(Ench.Items.IMPROVED_SCRAP_TOME.value(), 4),
            Ench.Items.SCRAP_TOME,
            req(45, 25, 35),
            max(-1, 50, -1));

        addInfusion("infusion/extraction_tome",
            new ItemStackTemplate(Ench.Items.EXTRACTION_TOME.value(), 4),
            Ench.Items.IMPROVED_SCRAP_TOME,
            req(60, 25, 45),
            max(-1, 75, -1));

        addInfusion("infusion/trident",
            new ItemStackTemplate(Items.TRIDENT),
            Ench.Items.INERT_TRIDENT,
            req(40, 20, 35),
            max(-1, 50, -1));

        addInfusion("infusion/xp_bottle", new ItemStackTemplate(Items.EXPERIENCE_BOTTLE), Items.HONEY_BOTTLE, req(20, 25, 25));
        addInfusion("infusion/xp_bottle_2", new ItemStackTemplate(Items.EXPERIENCE_BOTTLE, 8), Items.HONEY_BOTTLE, req(60, 25, 25));
        addInfusion("infusion/xp_bottle_3", new ItemStackTemplate(Items.EXPERIENCE_BOTTLE, 32), Items.HONEY_BOTTLE, req(100, 25, 25));

        addInfusion("infusion/carrot",
            new ItemStackTemplate(Items.GOLDEN_CARROT),
            Items.CARROT,
            req(20, 10, 0),
            max(20, 30, -1));

        addInfusion("infusion/budding_amethyst",
            new ItemStackTemplate(Items.BUDDING_AMETHYST),
            Items.AMETHYST_BLOCK,
            req(60, 30, 50),
            max(-1, 50, -1));

        addInfusion("infusion/echo_shard",
            new ItemStackTemplate(Items.ECHO_SHARD, 4),
            Items.ECHO_SHARD,
            req(70, 50, 50));

        addInfusion("infusion/ender_lead",
            new ItemStackTemplate(Ench.Items.ENDER_LEAD.value()),
            Ench.Items.FLIMSY_ENDER_LEAD,
            req(45, 25, 40));

        addInfusion("infusion/music_disc_eterna",
            new ItemStackTemplate(Ench.Items.MUSIC_DISC_ETERNA.value()),
            ItemTags.CREEPER_DROP_MUSIC_DISCS,
            req(40, 0, 0));

        addInfusion("infusion/music_disc_quanta",
            new ItemStackTemplate(Ench.Items.MUSIC_DISC_QUANTA.value()),
            ItemTags.CREEPER_DROP_MUSIC_DISCS,
            req(10, 40, 0));

        addInfusion("infusion/music_disc_arcana",
            new ItemStackTemplate(Ench.Items.MUSIC_DISC_ARCANA.value()),
            ItemTags.CREEPER_DROP_MUSIC_DISCS,
            req(10, 0, 40));

        Ingredient occultInput = DataComponentIngredient.of(false, Ench.Components.LEASHED_ENTITY_TYPE, EntityType.WITCH, Ench.Items.ENDER_LEAD.value());
        addInfusion("infusion/occult_ender_lead",
            new ItemStackTemplate(Ench.Items.OCCULT_ENDER_LEAD.value()),
            occultInput,
            req(75, 85, 60),
            max(-1, -1, 85));

        Ingredient libraryInput = createInput(false, Ench.Blocks.LIBRARY).get(0);
        KeepNBTInfusionRecipe enderLibrary = new KeepNBTInfusionRecipe(
            new ItemStackTemplate(Ench.Blocks.ENDER_LIBRARY.value().asItem()),
            libraryInput,
            req(100, 45, 100),
            max(100, 50, 100));
        emitRecipe("infusion/ender_library", enderLibrary);
    }

    private static Stats req(float eterna, float quanta, float arcana) {
        return new Stats(30F, eterna, quanta, arcana, 0);
    }

    private static Stats max(float maxEterna, float maxQuanta, float maxArcana) {
        return new Stats(30F, maxEterna, maxQuanta, maxArcana, 0);
    }

    private void addInfusion(String path, ItemStackTemplate output, Object input, Stats requirements) {
        addInfusion(path, output, input, requirements, InfusionRecipe.NO_MAX);
    }

    private void addInfusion(String path, ItemStackTemplate output, Object input, Stats requirements, Stats maxRequirements) {
        Ingredient ingredient = createInput(false, input).get(0);
        InfusionRecipe recipe = new InfusionRecipe(output, ingredient, requirements, maxRequirements);
        emitRecipe(path, recipe);
    }

    private void emitRecipe(String path, Recipe<?> recipe) {
        Identifier id = Identifier.fromNamespaceAndPath(ApothicEnchanting.MODID, path);
        this.recipeOutput.accept(ResourceKey.create(Registries.RECIPE, id), recipe, null);
    }
}
