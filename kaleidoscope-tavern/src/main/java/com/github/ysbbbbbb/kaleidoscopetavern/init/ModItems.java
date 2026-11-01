package com.github.ysbbbbbb.kaleidoscopetavern.init;

import com.github.ysbbbbbb.kaleidoscopetavern.KaleidoscopeTavern;
import com.github.ysbbbbbb.kaleidoscopetavern.item.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public interface ModItems {
    DeferredRegister.Items ITEMS = DeferredRegister.createItems(KaleidoscopeTavern.MOD_ID);

    // 沙发
    DeferredItem<Item> WHITE_SOFA = ITEMS.register("white_sofa", id -> new SofaBlockItem(id, ModBlocks.WHITE_SOFA));
    DeferredItem<Item> LIGHT_GRAY_SOFA = ITEMS.register("light_gray_sofa", id -> new SofaBlockItem(id, ModBlocks.LIGHT_GRAY_SOFA));
    DeferredItem<Item> GRAY_SOFA = ITEMS.register("gray_sofa", id -> new SofaBlockItem(id, ModBlocks.GRAY_SOFA));
    DeferredItem<Item> BLACK_SOFA = ITEMS.register("black_sofa", id -> new SofaBlockItem(id, ModBlocks.BLACK_SOFA));
    DeferredItem<Item> BROWN_SOFA = ITEMS.register("brown_sofa", id -> new SofaBlockItem(id, ModBlocks.BROWN_SOFA));
    DeferredItem<Item> RED_SOFA = ITEMS.register("red_sofa", id -> new SofaBlockItem(id, ModBlocks.RED_SOFA));
    DeferredItem<Item> ORANGE_SOFA = ITEMS.register("orange_sofa", id -> new SofaBlockItem(id, ModBlocks.ORANGE_SOFA));
    DeferredItem<Item> YELLOW_SOFA = ITEMS.register("yellow_sofa", id -> new SofaBlockItem(id, ModBlocks.YELLOW_SOFA));
    DeferredItem<Item> LIME_SOFA = ITEMS.register("lime_sofa", id -> new SofaBlockItem(id, ModBlocks.LIME_SOFA));
    DeferredItem<Item> GREEN_SOFA = ITEMS.register("green_sofa", id -> new SofaBlockItem(id, ModBlocks.GREEN_SOFA));
    DeferredItem<Item> CYAN_SOFA = ITEMS.register("cyan_sofa", id -> new SofaBlockItem(id, ModBlocks.CYAN_SOFA));
    DeferredItem<Item> LIGHT_BLUE_SOFA = ITEMS.register("light_blue_sofa", id -> new SofaBlockItem(id, ModBlocks.LIGHT_BLUE_SOFA));
    DeferredItem<Item> BLUE_SOFA = ITEMS.register("blue_sofa", id -> new SofaBlockItem(id, ModBlocks.BLUE_SOFA));
    DeferredItem<Item> PURPLE_SOFA = ITEMS.register("purple_sofa", id -> new SofaBlockItem(id, ModBlocks.PURPLE_SOFA));
    DeferredItem<Item> MAGENTA_SOFA = ITEMS.register("magenta_sofa", id -> new SofaBlockItem(id, ModBlocks.MAGENTA_SOFA));
    DeferredItem<Item> PINK_SOFA = ITEMS.register("pink_sofa", id -> new SofaBlockItem(id, ModBlocks.PINK_SOFA));

    // 高脚凳
    DeferredItem<Item> WHITE_BAR_STOOL = ITEMS.register("white_bar_stool", id -> new BarStoolBlockItem(id, ModBlocks.WHITE_BAR_STOOL));
    DeferredItem<Item> LIGHT_GRAY_BAR_STOOL = ITEMS.register("light_gray_bar_stool", id -> new BarStoolBlockItem(id, ModBlocks.LIGHT_GRAY_BAR_STOOL));
    DeferredItem<Item> GRAY_BAR_STOOL = ITEMS.register("gray_bar_stool", id -> new BarStoolBlockItem(id, ModBlocks.GRAY_BAR_STOOL));
    DeferredItem<Item> BLACK_BAR_STOOL = ITEMS.register("black_bar_stool", id -> new BarStoolBlockItem(id, ModBlocks.BLACK_BAR_STOOL));
    DeferredItem<Item> BROWN_BAR_STOOL = ITEMS.register("brown_bar_stool", id -> new BarStoolBlockItem(id, ModBlocks.BROWN_BAR_STOOL));
    DeferredItem<Item> RED_BAR_STOOL = ITEMS.register("red_bar_stool", id -> new BarStoolBlockItem(id, ModBlocks.RED_BAR_STOOL));
    DeferredItem<Item> ORANGE_BAR_STOOL = ITEMS.register("orange_bar_stool", id -> new BarStoolBlockItem(id, ModBlocks.ORANGE_BAR_STOOL));
    DeferredItem<Item> YELLOW_BAR_STOOL = ITEMS.register("yellow_bar_stool", id -> new BarStoolBlockItem(id, ModBlocks.YELLOW_BAR_STOOL));
    DeferredItem<Item> LIME_BAR_STOOL = ITEMS.register("lime_bar_stool", id -> new BarStoolBlockItem(id, ModBlocks.LIME_BAR_STOOL));
    DeferredItem<Item> GREEN_BAR_STOOL = ITEMS.register("green_bar_stool", id -> new BarStoolBlockItem(id, ModBlocks.GREEN_BAR_STOOL));
    DeferredItem<Item> CYAN_BAR_STOOL = ITEMS.register("cyan_bar_stool", id -> new BarStoolBlockItem(id, ModBlocks.CYAN_BAR_STOOL));
    DeferredItem<Item> LIGHT_BLUE_BAR_STOOL = ITEMS.register("light_blue_bar_stool", id -> new BarStoolBlockItem(id, ModBlocks.LIGHT_BLUE_BAR_STOOL));
    DeferredItem<Item> BLUE_BAR_STOOL = ITEMS.register("blue_bar_stool", id -> new BarStoolBlockItem(id, ModBlocks.BLUE_BAR_STOOL));
    DeferredItem<Item> PURPLE_BAR_STOOL = ITEMS.register("purple_bar_stool", id -> new BarStoolBlockItem(id, ModBlocks.PURPLE_BAR_STOOL));
    DeferredItem<Item> MAGENTA_BAR_STOOL = ITEMS.register("magenta_bar_stool", id -> new BarStoolBlockItem(id, ModBlocks.MAGENTA_BAR_STOOL));
    DeferredItem<Item> PINK_BAR_STOOL = ITEMS.register("pink_bar_stool", id -> new BarStoolBlockItem(id, ModBlocks.PINK_BAR_STOOL));

    // 黑板
    DeferredItem<Item> CHALKBOARD = ITEMS.register("chalkboard", id -> new BlockItem(ModBlocks.CHALKBOARD.get(), new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    DeferredItem<Item> TABLE = ITEMS.register("table", id -> new BlockItem(ModBlocks.TABLE.get(), new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));

    // 展板
    DeferredItem<Item> BASE_SANDWICH_BOARD = ITEMS.register("base_sandwich_board", id -> new SandwichBoardBlockItem(id, ModBlocks.BASE_SANDWICH_BOARD));
    DeferredItem<Item> GRASS_SANDWICH_BOARD = ITEMS.register("grass_sandwich_board", id -> new SandwichBoardBlockItem(id, ModBlocks.GRASS_SANDWICH_BOARD));
    DeferredItem<Item> ALLIUM_SANDWICH_BOARD = ITEMS.register("allium_sandwich_board", id -> new SandwichBoardBlockItem(id, ModBlocks.ALLIUM_SANDWICH_BOARD));
    DeferredItem<Item> AZURE_BLUET_SANDWICH_BOARD = ITEMS.register("azure_bluet_sandwich_board", id -> new SandwichBoardBlockItem(id, ModBlocks.AZURE_BLUET_SANDWICH_BOARD));
    DeferredItem<Item> CORNFLOWER_SANDWICH_BOARD = ITEMS.register("cornflower_sandwich_board", id -> new SandwichBoardBlockItem(id, ModBlocks.CORNFLOWER_SANDWICH_BOARD));
    DeferredItem<Item> ORCHID_SANDWICH_BOARD = ITEMS.register("orchid_sandwich_board", id -> new SandwichBoardBlockItem(id, ModBlocks.ORCHID_SANDWICH_BOARD));
    DeferredItem<Item> PEONY_SANDWICH_BOARD = ITEMS.register("peony_sandwich_board", id -> new SandwichBoardBlockItem(id, ModBlocks.PEONY_SANDWICH_BOARD));
    DeferredItem<Item> PINK_PETALS_SANDWICH_BOARD = ITEMS.register("pink_petals_sandwich_board", id -> new SandwichBoardBlockItem(id, ModBlocks.PINK_PETALS_SANDWICH_BOARD));
    DeferredItem<Item> PITCHER_PLANT_SANDWICH_BOARD = ITEMS.register("pitcher_plant_sandwich_board", id -> new SandwichBoardBlockItem(id, ModBlocks.PITCHER_PLANT_SANDWICH_BOARD));
    DeferredItem<Item> POPPY_SANDWICH_BOARD = ITEMS.register("poppy_sandwich_board", id -> new SandwichBoardBlockItem(id, ModBlocks.POPPY_SANDWICH_BOARD));
    DeferredItem<Item> SUNFLOWER_SANDWICH_BOARD = ITEMS.register("sunflower_sandwich_board", id -> new SandwichBoardBlockItem(id, ModBlocks.SUNFLOWER_SANDWICH_BOARD));
    DeferredItem<Item> TORCHFLOWER_SANDWICH_BOARD = ITEMS.register("torchflower_sandwich_board", id -> new SandwichBoardBlockItem(id, ModBlocks.TORCHFLOWER_SANDWICH_BOARD));
    DeferredItem<Item> TULIP_SANDWICH_BOARD = ITEMS.register("tulip_sandwich_board", id -> new SandwichBoardBlockItem(id, ModBlocks.TULIP_SANDWICH_BOARD));
    DeferredItem<Item> WITHER_ROSE_SANDWICH_BOARD = ITEMS.register("wither_rose_sandwich_board", id -> new SandwichBoardBlockItem(id, ModBlocks.WITHER_ROSE_SANDWICH_BOARD));

    // 彩灯
    DeferredItem<Item> STRING_LIGHTS_COLORLESS = ITEMS.register("string_lights_colorless", id -> new StringLightsBlockItem(id, ModBlocks.STRING_LIGHTS_COLORLESS));
    DeferredItem<Item> STRING_LIGHTS_WHITE = ITEMS.register("string_lights_white", id -> new StringLightsBlockItem(id, ModBlocks.STRING_LIGHTS_WHITE));
    DeferredItem<Item> STRING_LIGHTS_LIGHT_GRAY = ITEMS.register("string_lights_light_gray", id -> new StringLightsBlockItem(id, ModBlocks.STRING_LIGHTS_LIGHT_GRAY));
    DeferredItem<Item> STRING_LIGHTS_GRAY = ITEMS.register("string_lights_gray", id -> new StringLightsBlockItem(id, ModBlocks.STRING_LIGHTS_GRAY));
    DeferredItem<Item> STRING_LIGHTS_BLACK = ITEMS.register("string_lights_black", id -> new StringLightsBlockItem(id, ModBlocks.STRING_LIGHTS_BLACK));
    DeferredItem<Item> STRING_LIGHTS_BROWN = ITEMS.register("string_lights_brown", id -> new StringLightsBlockItem(id, ModBlocks.STRING_LIGHTS_BROWN));
    DeferredItem<Item> STRING_LIGHTS_RED = ITEMS.register("string_lights_red", id -> new StringLightsBlockItem(id, ModBlocks.STRING_LIGHTS_RED));
    DeferredItem<Item> STRING_LIGHTS_ORANGE = ITEMS.register("string_lights_orange", id -> new StringLightsBlockItem(id, ModBlocks.STRING_LIGHTS_ORANGE));
    DeferredItem<Item> STRING_LIGHTS_YELLOW = ITEMS.register("string_lights_yellow", id -> new StringLightsBlockItem(id, ModBlocks.STRING_LIGHTS_YELLOW));
    DeferredItem<Item> STRING_LIGHTS_LIME = ITEMS.register("string_lights_lime", id -> new StringLightsBlockItem(id, ModBlocks.STRING_LIGHTS_LIME));
    DeferredItem<Item> STRING_LIGHTS_GREEN = ITEMS.register("string_lights_green", id -> new StringLightsBlockItem(id, ModBlocks.STRING_LIGHTS_GREEN));
    DeferredItem<Item> STRING_LIGHTS_CYAN = ITEMS.register("string_lights_cyan", id -> new StringLightsBlockItem(id, ModBlocks.STRING_LIGHTS_CYAN));
    DeferredItem<Item> STRING_LIGHTS_LIGHT_BLUE = ITEMS.register("string_lights_light_blue", id -> new StringLightsBlockItem(id, ModBlocks.STRING_LIGHTS_LIGHT_BLUE));
    DeferredItem<Item> STRING_LIGHTS_BLUE = ITEMS.register("string_lights_blue", id -> new StringLightsBlockItem(id, ModBlocks.STRING_LIGHTS_BLUE));
    DeferredItem<Item> STRING_LIGHTS_PURPLE = ITEMS.register("string_lights_purple", id -> new StringLightsBlockItem(id, ModBlocks.STRING_LIGHTS_PURPLE));
    DeferredItem<Item> STRING_LIGHTS_MAGENTA = ITEMS.register("string_lights_magenta", id -> new StringLightsBlockItem(id, ModBlocks.STRING_LIGHTS_MAGENTA));
    DeferredItem<Item> STRING_LIGHTS_PINK = ITEMS.register("string_lights_pink", id -> new StringLightsBlockItem(id, ModBlocks.STRING_LIGHTS_PINK));

    // 挂画
    DeferredItem<Item> YSBB_PAINTING = ITEMS.register("ysbb_painting", id -> new PaintingBlockItem(id, ModBlocks.YSBB_PAINTING));
    DeferredItem<Item> TARTARIC_ACID_PAINTING = ITEMS.register("tartaric_acid_painting", id -> new PaintingBlockItem(id, ModBlocks.TARTARIC_ACID_PAINTING));
    DeferredItem<Item> CR019_PAINTING = ITEMS.register("cr019_painting", id -> new PaintingBlockItem(id, ModBlocks.CR019_PAINTING));
    DeferredItem<Item> UNKNOWN_PAINTING = ITEMS.register("unknown_painting", id -> new PaintingBlockItem(id, ModBlocks.UNKNOWN_PAINTING));
    DeferredItem<Item> MASTER_MARISA_PAINTING = ITEMS.register("master_marisa_painting", id -> new PaintingBlockItem(id, ModBlocks.MASTER_MARISA_PAINTING));
    DeferredItem<Item> SON_OF_MAN_PAINTING = ITEMS.register("son_of_man_painting", id -> new PaintingBlockItem(id, ModBlocks.SON_OF_MAN_PAINTING));
    DeferredItem<Item> DAVID_PAINTING = ITEMS.register("david_painting", id -> new PaintingBlockItem(id, ModBlocks.DAVID_PAINTING));
    DeferredItem<Item> GIRL_WITH_PEARL_EARRING_PAINTING = ITEMS.register("girl_with_pearl_earring_painting", id -> new PaintingBlockItem(id, ModBlocks.GIRL_WITH_PEARL_EARRING_PAINTING));
    DeferredItem<Item> STARRY_NIGHT_PAINTING = ITEMS.register("starry_night_painting", id -> new PaintingBlockItem(id, ModBlocks.STARRY_NIGHT_PAINTING));
    DeferredItem<Item> VAN_GOGH_SELF_PORTRAIT_PAINTING = ITEMS.register("van_gogh_self_portrait_painting", id -> new PaintingBlockItem(id, ModBlocks.VAN_GOGH_SELF_PORTRAIT_PAINTING));
    DeferredItem<Item> FATHER_PAINTING = ITEMS.register("father_painting", id -> new PaintingBlockItem(id, ModBlocks.FATHER_PAINTING));
    DeferredItem<Item> GREAT_WAVE_PAINTING = ITEMS.register("great_wave_painting", id -> new PaintingBlockItem(id, ModBlocks.GREAT_WAVE_PAINTING));
    DeferredItem<Item> MONA_LISA_PAINTING = ITEMS.register("mona_lisa_painting", id -> new PaintingBlockItem(id, ModBlocks.MONA_LISA_PAINTING));
    DeferredItem<Item> MONDRIAN_PAINTING = ITEMS.register("mondrian_painting", id -> new PaintingBlockItem(id, ModBlocks.MONDRIAN_PAINTING));

    // 吧台
    DeferredItem<Item> BAR_COUNTER = ITEMS.register("bar_counter", id -> new BlockItem(ModBlocks.BAR_COUNTER.get(), new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    // 人字梯
    DeferredItem<Item> STEPLADDER = ITEMS.register("stepladder", id -> new BlockItem(ModBlocks.STEPLADDER.get(), new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    // 野生葡萄藤
    DeferredItem<Item> GRAPEVINE = ITEMS.register("grapevine", GrapevineItem::new);
    // 藤架
    DeferredItem<Item> TRELLIS = ITEMS.register("trellis", id -> new BlockItem(ModBlocks.TRELLIS.get(), new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    // 葡萄
    DeferredItem<Item> GRAPE = ITEMS.register("grape", id -> new Item(new Item.Properties().food(ModFoods.GRAPE).setId(ResourceKey.create(Registries.ITEM, id))));
    DeferredItem<Item> ICE_GRAPE = ITEMS.register("ice_grape", id -> new Item(new Item.Properties().food(ModFoods.GRAPE).setId(ResourceKey.create(Registries.ITEM, id))));
    DeferredItem<Item> GOLD_GRAPE = ITEMS.register("gold_grape", id -> new Item(new Item.Properties().food(ModFoods.GRAPE).setId(ResourceKey.create(Registries.ITEM, id))));
    DeferredItem<Item> GREEN_GRAPE = ITEMS.register("green_grape", id -> new Item(new Item.Properties().food(ModFoods.GRAPE).setId(ResourceKey.create(Registries.ITEM, id))));

    // 果盆
    DeferredItem<Item> PRESSING_TUB = ITEMS.register("pressing_tub", id -> new BlockItem(ModBlocks.PRESSING_TUB.get(), new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    // 果汁桶
    DeferredItem<Item> GRAPE_BUCKET = ITEMS.register("grape_bucket", id -> new JuiceBucketItem(id, ModFluids.GRAPE_JUICE));
    DeferredItem<Item> ICE_GRAPE_BUCKET = ITEMS.register("ice_grape_bucket", id -> new JuiceBucketItem(id, ModFluids.ICE_GRAPE_JUICE));
    DeferredItem<Item> GOLD_GRAPE_BUCKET = ITEMS.register("gold_grape_bucket", id -> new JuiceBucketItem(id, ModFluids.GOLD_GRAPE_JUICE));
    DeferredItem<Item> GREEN_GRAPE_BUCKET = ITEMS.register("green_grape_bucket", id -> new JuiceBucketItem(id, ModFluids.GREEN_GRAPE_JUICE));
    DeferredItem<Item> SWEET_BERRIES_BUCKET = ITEMS.register("sweet_berries_bucket", id -> new JuiceBucketItem(id, ModFluids.SWEET_BERRIES_JUICE));
    DeferredItem<Item> GLOW_BERRIES_BUCKET = ITEMS.register("glow_berries_bucket", id -> new JuiceBucketItem(id, ModFluids.GLOW_BERRIES_JUICE));
    // 龙头
    DeferredItem<Item> TAP = ITEMS.register("tap", id -> new BlockItem(ModBlocks.TAP.get(), new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    // 酒桶
    DeferredItem<Item> BARREL = ITEMS.register("barrel", id -> new BlockItem(ModBlocks.BARREL.get(), new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    // 酒柜
    DeferredItem<Item> BAR_CABINET = ITEMS.register("bar_cabinet", id -> new BlockItem(ModBlocks.BAR_CABINET.get(), new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    DeferredItem<Item> GLASS_BAR_CABINET = ITEMS.register("glass_bar_cabinet", id -> new BlockItem(ModBlocks.GLASS_BAR_CABINET.get(), new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));

    // 空瓶
    DeferredItem<Item> EMPTY_BOTTLE = ITEMS.register("empty_bottle", id -> new BottleBlockItem(id, ModBlocks.EMPTY_BOTTLE.get()));
    // 燃烧瓶
    DeferredItem<Item> MOLOTOV = ITEMS.register("molotov", id -> new MolotovBlockItem(id, ModBlocks.MOLOTOV.get()));
    // 酒
    DeferredItem<Item> WINE = ITEMS.register("wine", id -> new DrinkBlockItem(id, ModBlocks.WINE.get()));
    DeferredItem<Item> CHAMPAGNE = ITEMS.register("champagne", id -> new DrinkBlockItem(id, ModBlocks.CHAMPAGNE.get()));
    DeferredItem<Item> VODKA = ITEMS.register("vodka", id -> new DrinkBlockItem(id, ModBlocks.VODKA.get()));
    DeferredItem<Item> BRANDY = ITEMS.register("brandy", id -> new DrinkBlockItem(id, ModBlocks.BRANDY.get()));
    DeferredItem<Item> CARIGNAN = ITEMS.register("carignan", id -> new DrinkBlockItem(id, ModBlocks.CARIGNAN.get()));
    DeferredItem<Item> SAKURA_WINE = ITEMS.register("sakura_wine", id -> new DrinkBlockItem(id, ModBlocks.SAKURA_WINE.get()));
    DeferredItem<Item> PLUM_WINE = ITEMS.register("plum_wine", id -> new DrinkBlockItem(id, ModBlocks.PLUM_WINE.get()));
    DeferredItem<Item> WHISKEY = ITEMS.register("whiskey", id -> new DrinkBlockItem(id, ModBlocks.WHISKEY.get()));
    DeferredItem<Item> ICE_WINE = ITEMS.register("ice_wine", id -> new DrinkBlockItem(id, ModBlocks.ICE_WINE.get()));
    DeferredItem<Item> POLARIS_SWEET_WHITE = ITEMS.register("polaris_sweet_white", id -> new DrinkBlockItem(id, ModBlocks.POLARIS_SWEET_WHITE.get()));
    DeferredItem<Item> HONEY_WINE = ITEMS.register("honey_wine", id -> new DrinkBlockItem(id, ModBlocks.HONEY_WINE.get()));
    DeferredItem<Item> RED_QUEEN = ITEMS.register("red_queen", id -> new DrinkBlockItem(id, ModBlocks.RED_QUEEN.get()));
    DeferredItem<Item> MINERS_STAR = ITEMS.register("miners_star", id -> new DrinkBlockItem(id, ModBlocks.MINERS_STAR.get()));
    DeferredItem<Item> RUM = ITEMS.register("rum", id -> new DrinkBlockItem(id, ModBlocks.RUM.get()));
    DeferredItem<Item> RIESLING_DRY_WHITE = ITEMS.register("riesling_dry_white", id -> new DrinkBlockItem(id, ModBlocks.RIESLING_DRY_WHITE.get()));
    DeferredItem<Item> SUNSET_GLOW = ITEMS.register("sunset_glow", id -> new DrinkBlockItem(id, ModBlocks.SUNSET_GLOW.get()));
    DeferredItem<Item> MADAME_SHEXIANG = ITEMS.register("madame_shexiang", id -> new DrinkBlockItem(id, ModBlocks.MADAME_SHEXIANG.get()));
    DeferredItem<Item> SWEET_BERRY_WINE = ITEMS.register("sweet_berry_wine", id -> new DrinkBlockItem(id, ModBlocks.SWEET_BERRY_WINE.get()));
    DeferredItem<Item> SHERRY = ITEMS.register("sherry", id -> new DrinkBlockItem(id, ModBlocks.SHERRY.get()));
    DeferredItem<Item> MOTHER_SNOW = ITEMS.register("mother_snow", id -> new DrinkBlockItem(id, ModBlocks.MOTHER_SNOW.get()));
    DeferredItem<Item> LUMINOUS_BRIDE = ITEMS.register("luminous_bride", id -> new DrinkBlockItem(id, ModBlocks.LUMINOUS_BRIDE.get()));
    DeferredItem<Item> GLOWFLOWER_BREW = ITEMS.register("glowflower_brew", id -> new DrinkBlockItem(id, ModBlocks.GLOWFLOWER_BREW.get()));
    DeferredItem<Item> SAUVIGNON_BLANC_DRY_WHITE = ITEMS.register("sauvignon_blanc_dry_white", id -> new DrinkBlockItem(id, ModBlocks.SAUVIGNON_BLANC_DRY_WHITE.get()));
    DeferredItem<Item> VINEGAR = ITEMS.register("vinegar", id -> new DrinkBlockItem(id, ModBlocks.VINEGAR.get()));
    DeferredItem<Item> WATERMELON_JUICE = ITEMS.register("watermelon_juice", id -> new DrinkBlockItem(id, ModBlocks.WATERMELON_JUICE.get()));
}