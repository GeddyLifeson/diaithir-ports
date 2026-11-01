package com.github.ysbbbbbb.kaleidoscopetavern.init;

import com.github.ysbbbbbb.kaleidoscopetavern.KaleidoscopeTavern;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.*;
import com.github.ysbbbbbb.kaleidoscopetavern.block.deco.*;
import com.github.ysbbbbbb.kaleidoscopetavern.block.plant.*;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.*;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.deco.BarStoolBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.deco.ChalkboardBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.deco.SandwichBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@SuppressWarnings("DataFlowIssue")
public interface ModBlocks {
    DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(KaleidoscopeTavern.MOD_ID);
    DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, KaleidoscopeTavern.MOD_ID);

    // 装饰部分
    // 沙发
    DeferredBlock<Block> WHITE_SOFA = BLOCKS.register("white_sofa", SofaBlock::new);
    DeferredBlock<Block> LIGHT_GRAY_SOFA = BLOCKS.register("light_gray_sofa", SofaBlock::new);
    DeferredBlock<Block> GRAY_SOFA = BLOCKS.register("gray_sofa", SofaBlock::new);
    DeferredBlock<Block> BLACK_SOFA = BLOCKS.register("black_sofa", SofaBlock::new);
    DeferredBlock<Block> BROWN_SOFA = BLOCKS.register("brown_sofa", SofaBlock::new);
    DeferredBlock<Block> RED_SOFA = BLOCKS.register("red_sofa", SofaBlock::new);
    DeferredBlock<Block> ORANGE_SOFA = BLOCKS.register("orange_sofa", SofaBlock::new);
    DeferredBlock<Block> YELLOW_SOFA = BLOCKS.register("yellow_sofa", SofaBlock::new);
    DeferredBlock<Block> LIME_SOFA = BLOCKS.register("lime_sofa", SofaBlock::new);
    DeferredBlock<Block> GREEN_SOFA = BLOCKS.register("green_sofa", SofaBlock::new);
    DeferredBlock<Block> CYAN_SOFA = BLOCKS.register("cyan_sofa", SofaBlock::new);
    DeferredBlock<Block> LIGHT_BLUE_SOFA = BLOCKS.register("light_blue_sofa", SofaBlock::new);
    DeferredBlock<Block> BLUE_SOFA = BLOCKS.register("blue_sofa", SofaBlock::new);
    DeferredBlock<Block> PURPLE_SOFA = BLOCKS.register("purple_sofa", SofaBlock::new);
    DeferredBlock<Block> MAGENTA_SOFA = BLOCKS.register("magenta_sofa", SofaBlock::new);
    DeferredBlock<Block> PINK_SOFA = BLOCKS.register("pink_sofa", SofaBlock::new);

    // 高脚凳
    DeferredBlock<Block> WHITE_BAR_STOOL = BLOCKS.register("white_bar_stool", id -> new BarStoolBlock(id, DyeColor.WHITE));
    DeferredBlock<Block> LIGHT_GRAY_BAR_STOOL = BLOCKS.register("light_gray_bar_stool", id -> new BarStoolBlock(id, DyeColor.LIGHT_GRAY));
    DeferredBlock<Block> GRAY_BAR_STOOL = BLOCKS.register("gray_bar_stool", id -> new BarStoolBlock(id, DyeColor.GRAY));
    DeferredBlock<Block> BLACK_BAR_STOOL = BLOCKS.register("black_bar_stool", id -> new BarStoolBlock(id, DyeColor.BLACK));
    DeferredBlock<Block> BROWN_BAR_STOOL = BLOCKS.register("brown_bar_stool", id -> new BarStoolBlock(id, DyeColor.BROWN));
    DeferredBlock<Block> RED_BAR_STOOL = BLOCKS.register("red_bar_stool", id -> new BarStoolBlock(id, DyeColor.RED));
    DeferredBlock<Block> ORANGE_BAR_STOOL = BLOCKS.register("orange_bar_stool", id -> new BarStoolBlock(id, DyeColor.ORANGE));
    DeferredBlock<Block> YELLOW_BAR_STOOL = BLOCKS.register("yellow_bar_stool", id -> new BarStoolBlock(id, DyeColor.YELLOW));
    DeferredBlock<Block> LIME_BAR_STOOL = BLOCKS.register("lime_bar_stool", id -> new BarStoolBlock(id, DyeColor.LIME));
    DeferredBlock<Block> GREEN_BAR_STOOL = BLOCKS.register("green_bar_stool", id -> new BarStoolBlock(id, DyeColor.GREEN));
    DeferredBlock<Block> CYAN_BAR_STOOL = BLOCKS.register("cyan_bar_stool", id -> new BarStoolBlock(id, DyeColor.CYAN));
    DeferredBlock<Block> LIGHT_BLUE_BAR_STOOL = BLOCKS.register("light_blue_bar_stool", id -> new BarStoolBlock(id, DyeColor.LIGHT_BLUE));
    DeferredBlock<Block> BLUE_BAR_STOOL = BLOCKS.register("blue_bar_stool", id -> new BarStoolBlock(id, DyeColor.BLUE));
    DeferredBlock<Block> PURPLE_BAR_STOOL = BLOCKS.register("purple_bar_stool", id -> new BarStoolBlock(id, DyeColor.PURPLE));
    DeferredBlock<Block> MAGENTA_BAR_STOOL = BLOCKS.register("magenta_bar_stool", id -> new BarStoolBlock(id, DyeColor.MAGENTA));
    DeferredBlock<Block> PINK_BAR_STOOL = BLOCKS.register("pink_bar_stool", id -> new BarStoolBlock(id, DyeColor.PINK));

    // 黑板
    DeferredBlock<Block> CHALKBOARD = BLOCKS.register("chalkboard", ChalkboardBlock::new);
    DeferredBlock<Block> TABLE = BLOCKS.register("table", TableBlock::new);

    // 展板
    DeferredBlock<Block> BASE_SANDWICH_BOARD = BLOCKS.register("base_sandwich_board", SandwichBoardBlock::new);
    DeferredBlock<Block> GRASS_SANDWICH_BOARD = BLOCKS.register("grass_sandwich_board", id -> new SandwichBoardBlock(id, Items.SHORT_GRASS));
    DeferredBlock<Block> ALLIUM_SANDWICH_BOARD = BLOCKS.register("allium_sandwich_board", id -> new SandwichBoardBlock(id, Items.ALLIUM));
    DeferredBlock<Block> AZURE_BLUET_SANDWICH_BOARD = BLOCKS.register("azure_bluet_sandwich_board", id -> new SandwichBoardBlock(id, Items.AZURE_BLUET, Items.OXEYE_DAISY, Items.LILY_OF_THE_VALLEY));
    DeferredBlock<Block> CORNFLOWER_SANDWICH_BOARD = BLOCKS.register("cornflower_sandwich_board", id -> new SandwichBoardBlock(id, Items.CORNFLOWER));
    DeferredBlock<Block> ORCHID_SANDWICH_BOARD = BLOCKS.register("orchid_sandwich_board", id -> new SandwichBoardBlock(id, Items.BLUE_ORCHID));
    DeferredBlock<Block> PEONY_SANDWICH_BOARD = BLOCKS.register("peony_sandwich_board", id -> new SandwichBoardBlock(id, Items.PEONY, Items.LILAC));
    DeferredBlock<Block> PINK_PETALS_SANDWICH_BOARD = BLOCKS.register("pink_petals_sandwich_board", id -> new SandwichBoardBlock(id, Items.PINK_PETALS));
    DeferredBlock<Block> PITCHER_PLANT_SANDWICH_BOARD = BLOCKS.register("pitcher_plant_sandwich_board", id -> new SandwichBoardBlock(id, Items.PITCHER_PLANT));
    DeferredBlock<Block> POPPY_SANDWICH_BOARD = BLOCKS.register("poppy_sandwich_board", id -> new SandwichBoardBlock(id, Items.POPPY, Items.ROSE_BUSH));
    DeferredBlock<Block> SUNFLOWER_SANDWICH_BOARD = BLOCKS.register("sunflower_sandwich_board", id -> new SandwichBoardBlock(id, Items.SUNFLOWER, Items.DANDELION));
    DeferredBlock<Block> TORCHFLOWER_SANDWICH_BOARD = BLOCKS.register("torchflower_sandwich_board", id -> new SandwichBoardBlock(id, Items.TORCHFLOWER));
    DeferredBlock<Block> TULIP_SANDWICH_BOARD = BLOCKS.register("tulip_sandwich_board", id -> new SandwichBoardBlock(id, Items.RED_TULIP, Items.ORANGE_TULIP, Items.WHITE_TULIP, Items.PINK_TULIP));
    DeferredBlock<Block> WITHER_ROSE_SANDWICH_BOARD = BLOCKS.register("wither_rose_sandwich_board", id -> new SandwichBoardBlock(id, Items.WITHER_ROSE));

    // 彩灯
    DeferredBlock<Block> STRING_LIGHTS_COLORLESS = BLOCKS.register("string_lights_colorless", id -> new StringLightsBlock(id, null));
    DeferredBlock<Block> STRING_LIGHTS_WHITE = BLOCKS.register("string_lights_white", id -> new StringLightsBlock(id, DyeColor.WHITE));
    DeferredBlock<Block> STRING_LIGHTS_LIGHT_GRAY = BLOCKS.register("string_lights_light_gray", id -> new StringLightsBlock(id, DyeColor.LIGHT_GRAY));
    DeferredBlock<Block> STRING_LIGHTS_GRAY = BLOCKS.register("string_lights_gray", id -> new StringLightsBlock(id, DyeColor.GRAY));
    DeferredBlock<Block> STRING_LIGHTS_BLACK = BLOCKS.register("string_lights_black", id -> new StringLightsBlock(id, DyeColor.BLACK));
    DeferredBlock<Block> STRING_LIGHTS_BROWN = BLOCKS.register("string_lights_brown", id -> new StringLightsBlock(id, DyeColor.BROWN));
    DeferredBlock<Block> STRING_LIGHTS_RED = BLOCKS.register("string_lights_red", id -> new StringLightsBlock(id, DyeColor.RED));
    DeferredBlock<Block> STRING_LIGHTS_ORANGE = BLOCKS.register("string_lights_orange", id -> new StringLightsBlock(id, DyeColor.ORANGE));
    DeferredBlock<Block> STRING_LIGHTS_YELLOW = BLOCKS.register("string_lights_yellow", id -> new StringLightsBlock(id, DyeColor.YELLOW));
    DeferredBlock<Block> STRING_LIGHTS_LIME = BLOCKS.register("string_lights_lime", id -> new StringLightsBlock(id, DyeColor.LIME));
    DeferredBlock<Block> STRING_LIGHTS_GREEN = BLOCKS.register("string_lights_green", id -> new StringLightsBlock(id, DyeColor.GREEN));
    DeferredBlock<Block> STRING_LIGHTS_CYAN = BLOCKS.register("string_lights_cyan", id -> new StringLightsBlock(id, DyeColor.CYAN));
    DeferredBlock<Block> STRING_LIGHTS_LIGHT_BLUE = BLOCKS.register("string_lights_light_blue", id -> new StringLightsBlock(id, DyeColor.LIGHT_BLUE));
    DeferredBlock<Block> STRING_LIGHTS_BLUE = BLOCKS.register("string_lights_blue", id -> new StringLightsBlock(id, DyeColor.BLUE));
    DeferredBlock<Block> STRING_LIGHTS_PURPLE = BLOCKS.register("string_lights_purple", id -> new StringLightsBlock(id, DyeColor.PURPLE));
    DeferredBlock<Block> STRING_LIGHTS_MAGENTA = BLOCKS.register("string_lights_magenta", id -> new StringLightsBlock(id, DyeColor.MAGENTA));
    DeferredBlock<Block> STRING_LIGHTS_PINK = BLOCKS.register("string_lights_pink", id -> new StringLightsBlock(id, DyeColor.PINK));

    // 挂画
    DeferredBlock<Block> YSBB_PAINTING = BLOCKS.register("ysbb_painting", PaintingBlock::new);
    DeferredBlock<Block> TARTARIC_ACID_PAINTING = BLOCKS.register("tartaric_acid_painting", PaintingBlock::new);
    DeferredBlock<Block> CR019_PAINTING = BLOCKS.register("cr019_painting", PaintingBlock::new);
    DeferredBlock<Block> UNKNOWN_PAINTING = BLOCKS.register("unknown_painting", PaintingBlock::new);
    DeferredBlock<Block> MASTER_MARISA_PAINTING = BLOCKS.register("master_marisa_painting", PaintingBlock::new);
    DeferredBlock<Block> SON_OF_MAN_PAINTING = BLOCKS.register("son_of_man_painting", PaintingBlock::new);
    DeferredBlock<Block> DAVID_PAINTING = BLOCKS.register("david_painting", PaintingBlock::new);
    DeferredBlock<Block> GIRL_WITH_PEARL_EARRING_PAINTING = BLOCKS.register("girl_with_pearl_earring_painting", PaintingBlock::new);
    DeferredBlock<Block> STARRY_NIGHT_PAINTING = BLOCKS.register("starry_night_painting", PaintingBlock::new);
    DeferredBlock<Block> VAN_GOGH_SELF_PORTRAIT_PAINTING = BLOCKS.register("van_gogh_self_portrait_painting", PaintingBlock::new);
    DeferredBlock<Block> FATHER_PAINTING = BLOCKS.register("father_painting", PaintingBlock::new);
    DeferredBlock<Block> GREAT_WAVE_PAINTING = BLOCKS.register("great_wave_painting", PaintingBlock::new);
    DeferredBlock<Block> MONA_LISA_PAINTING = BLOCKS.register("mona_lisa_painting", PaintingBlock::new);
    DeferredBlock<Block> MONDRIAN_PAINTING = BLOCKS.register("mondrian_painting", PaintingBlock::new);

    // 吧台
    DeferredBlock<Block> BAR_COUNTER = BLOCKS.register("bar_counter", BarCounterBlock::new);
    // 人字梯
    DeferredBlock<Block> STEPLADDER = BLOCKS.register("stepladder", StepladderBlock::new);
    // 野生葡萄藤
    DeferredBlock<Block> WILD_GRAPEVINE = BLOCKS.register("wild_grapevine", WildGrapevineBlock::new);
    DeferredBlock<Block> WILD_GRAPEVINE_PLANT = BLOCKS.register("wild_grapevine_plant", WildGrapevinePlantBlock::new);
    // 藤架
    DeferredBlock<Block> TRELLIS = BLOCKS.register("trellis", TrellisBlock::new);
    // 葡萄藤
    DeferredBlock<Block> GRAPEVINE_TRELLIS = BLOCKS.register("grapevine_trellis", id -> new GrapevineTrellisBlock(
            id,
            (state, level, pos, random) -> 0.25F,
            () -> ModBlocks.GRAPE_CROP.get().defaultBlockState()
    ));
    DeferredBlock<Block> ICE_GRAPEVINE_TRELLIS = BLOCKS.register("ice_grapevine_trellis", id -> new GrapevineTrellisBlock(
            id,
            (state, level, pos, random) ->
                    level.getBiome(pos).value().getBaseTemperature() < 0.15F ? 0.8F : 0.25F,
            () -> ModBlocks.ICE_GRAPE_CROP.get().defaultBlockState()
    ));
    DeferredBlock<Block> GOLD_GRAPEVINE_TRELLIS = BLOCKS.register("gold_grapevine_trellis", id -> new GrapevineTrellisBlock(
            id,
            (state, level, pos, random) ->
                    level.getBiome(pos).value().getBaseTemperature() > 1.0F ? 0.8F : 0.25F,
            () -> ModBlocks.GOLD_GRAPE_CROP.get().defaultBlockState()
    ));
    // 葡萄
    DeferredBlock<Block> GRAPE_CROP = BLOCKS.register("grape_crop", id -> new GrapeCropBlock(
            id,
            (state, level, pos, random) -> 0.25F,
            () -> new ItemStack(ModItems.GRAPE.get(), 3)
    ));
    DeferredBlock<Block> ICE_GRAPE_CROP = BLOCKS.register("ice_grape_crop", id -> new GrapeCropBlock(
            id,
            (state, level, pos, random) ->
                    level.getBiome(pos).value().getBaseTemperature() < 0.15F ? 0.8F : 0.25F,
            () -> new ItemStack(ModItems.ICE_GRAPE.get(), 3)
    ));
    DeferredBlock<Block> GOLD_GRAPE_CROP = BLOCKS.register("gold_grape_crop", id -> new GrapeCropBlock(
            id,
            (state, level, pos, random) ->
                    level.getBiome(pos).value().getBaseTemperature() > 1.0F ? 0.8F : 0.25F,
            () -> new ItemStack(ModItems.GOLD_GRAPE.get(), 3)
    ));

    // 果盆
    DeferredBlock<Block> PRESSING_TUB = BLOCKS.register("pressing_tub", PressingTubBlock::new);
    // 龙头
    DeferredBlock<Block> TAP = BLOCKS.register("tap", TapBlock::new);
    // 空瓶
    DeferredBlock<Block> EMPTY_BOTTLE = BLOCKS.register("empty_bottle", BottleBlock::new);
    // 燃烧瓶
    DeferredBlock<Block> MOLOTOV = BLOCKS.register("molotov", MolotovBlock::new);
    // 酒桶
    DeferredBlock<Block> BARREL = BLOCKS.register("barrel", BarrelBlock::new);
    // 酒柜
    DeferredBlock<Block> BAR_CABINET = BLOCKS.register("bar_cabinet", BarCabinetBlock::new);
    DeferredBlock<Block> GLASS_BAR_CABINET = BLOCKS.register("glass_bar_cabinet", BarCabinetBlock::new);

    // 杂项的瓶子
    DeferredBlock<Block> WATER_BOTTLE = BLOCKS.register("water_bottle", BottleBlock::new);
    DeferredBlock<Block> HONEY_BOTTLE = BLOCKS.register("honey_bottle", BottleBlock::new);
    DeferredBlock<Block> DRAGON_BREATH_BOTTLE = BLOCKS.register("dragon_breath_bottle", BottleBlock::new);

    // 酒
    DeferredBlock<Block> WINE = BLOCKS.register("wine", id -> DrinkBlock.create().maxCount(4).shapes(
            Block.box(6, 0, 6, 10, 16, 10),
            Block.box(2, 0, 6, 14, 16, 10),
            Shapes.or(
                    Block.box(2, 0, 10, 14, 16, 14),
                    Block.box(6, 0, 2, 10, 16, 14)
            ),
            Block.box(2, 0, 2, 14, 16, 14)
    ).build(id));

    DeferredBlock<Block> CHAMPAGNE = BLOCKS.register("champagne", id -> DrinkBlock.create().maxCount(4).shapes(
            Block.box(6, 0, 6, 10, 16, 10),
            Block.box(2, 0, 6, 14, 16, 10),
            Shapes.or(
                    Block.box(2, 0, 10, 14, 16, 14),
                    Block.box(6, 0, 2, 10, 16, 14)
            ),
            Block.box(2, 0, 2, 14, 16, 14)
    ).build(id));

    DeferredBlock<Block> VODKA = BLOCKS.register("vodka", id -> DrinkBlock.create().maxCount(4).shapes(
            Block.box(4, 0, 4, 12, 15, 12),
            Block.box(0, 0, 4, 16, 15, 12),
            Shapes.or(
                    Block.box(0, 0, 8, 16, 15, 16),
                    Block.box(4, 0, 0, 12, 15, 16)
            ),
            Block.box(0, 0, 0, 16, 16, 16)
    ).build(id));

    DeferredBlock<Block> BRANDY = BLOCKS.register("brandy", id -> DrinkBlock.create().irregular().maxCount(3).shapes(
            Block.box(3, 0, 6, 13, 13, 10),
            Block.box(1, 0, 3, 15, 12, 12),
            Block.box(1, 0, 1, 16, 12, 13)
    ).build(id));

    DeferredBlock<Block> CARIGNAN = BLOCKS.register("carignan", id -> DrinkBlock.create().irregular().maxCount(3).shapes(
            Block.box(3, 0, 6, 13, 12, 10),
            Block.box(1, 0, 3, 15, 12, 12),
            Block.box(0, 0, 1, 16, 12, 13)
    ).build(id));

    DeferredBlock<Block> SAKURA_WINE = BLOCKS.register("sakura_wine", id -> DrinkBlock.create().maxCount(4).shapes(
            Block.box(6, 0, 6, 10, 16, 10),
            Block.box(2, 0, 6, 14, 16, 10),
            Shapes.or(
                    Block.box(2, 0, 10, 14, 16, 14),
                    Block.box(6, 0, 2, 10, 16, 14)
            ),
            Block.box(2, 0, 2, 14, 16, 14)
    ).build(id));

    DeferredBlock<Block> PLUM_WINE = BLOCKS.register("plum_wine", id -> DrinkBlock.create().maxCount(4).shapes(
            Block.box(6, 0, 6, 10, 12, 10),
            Block.box(3, 0, 6, 13, 12, 10),
            Shapes.or(
                    Block.box(3, 0, 9, 13, 12, 13),
                    Block.box(6, 0, 3, 10, 12, 13)
            ),
            Block.box(3, 0, 3, 13, 12, 13)
    ).build(id));

    DeferredBlock<Block> WHISKEY = BLOCKS.register("whiskey", id -> DrinkBlock.create().maxCount(4).shapes(
            Block.box(6, 0, 6, 10, 16, 10),
            Block.box(2, 0, 6, 14, 16, 10),
            Shapes.or(
                    Block.box(2, 0, 10, 14, 16, 14),
                    Block.box(6, 0, 2, 10, 16, 14)
            ),
            Block.box(2, 0, 2, 14, 16, 14)
    ).build(id));

    DeferredBlock<Block> ICE_WINE = BLOCKS.register("ice_wine", id -> DrinkBlock.create().maxCount(4).shapes(
            Block.box(6, 0, 6, 10, 16, 10),
            Block.box(2, 0, 6, 14, 16, 10),
            Shapes.or(
                    Block.box(2, 0, 10, 14, 16, 14),
                    Block.box(6, 0, 2, 10, 16, 14)
            ),
            Block.box(2, 0, 2, 14, 16, 14)
    ).build(id));

    DeferredBlock<Block> POLARIS_SWEET_WHITE = BLOCKS.register("polaris_sweet_white", id -> DrinkBlock.create().maxCount(4).shapes(
            Block.box(6, 0, 6, 10, 16, 10),
            Block.box(2, 0, 6, 14, 16, 10),
            Shapes.or(
                    Block.box(2, 0, 10, 14, 16, 14),
                    Block.box(6, 0, 2, 10, 16, 14)
            ),
            Block.box(2, 0, 2, 14, 16, 14)
    ).build(id));

    DeferredBlock<Block> HONEY_WINE = BLOCKS.register("honey_wine", id -> DrinkBlock.create().maxCount(4).shapes(
            Block.box(6, 0, 6, 10, 16, 10),
            Block.box(2, 0, 6, 14, 16, 10),
            Shapes.or(
                    Block.box(2, 0, 10, 14, 16, 14),
                    Block.box(6, 0, 2, 10, 16, 14)
            ),
            Block.box(2, 0, 2, 14, 16, 14)
    ).build(id));

    DeferredBlock<Block> RED_QUEEN = BLOCKS.register("red_queen", id -> DrinkBlock.create().maxCount(4).shapes(
            Block.box(6, 0, 6, 10, 16, 10),
            Block.box(2, 0, 6, 14, 16, 10),
            Shapes.or(
                    Block.box(2, 0, 10, 14, 16, 14),
                    Block.box(6, 0, 2, 10, 16, 14)
            ),
            Block.box(2, 0, 2, 14, 16, 14)
    ).build(id));

    DeferredBlock<Block> MINERS_STAR = BLOCKS.register("miners_star", id -> DrinkBlock.create().maxCount(4).shapes(
            Block.box(6, 0, 6, 10, 16, 10),
            Block.box(2, 0, 6, 14, 16, 10),
            Shapes.or(
                    Block.box(2, 0, 10, 14, 16, 14),
                    Block.box(6, 0, 2, 10, 16, 14)
            ),
            Block.box(2, 0, 2, 14, 16, 14)
    ).build(id));

    DeferredBlock<Block> RUM = BLOCKS.register("rum", id -> DrinkBlock.create().maxCount(4).shapes(
            Block.box(6, 0, 6, 10, 16, 10),
            Block.box(2, 0, 6, 14, 16, 10),
            Shapes.or(
                    Block.box(2, 0, 10, 14, 16, 14),
                    Block.box(6, 0, 2, 10, 16, 14)
            ),
            Block.box(2, 0, 2, 14, 16, 14)
    ).build(id));

    DeferredBlock<Block> RIESLING_DRY_WHITE = BLOCKS.register("riesling_dry_white", id -> DrinkBlock.create().maxCount(4).shapes(
            Block.box(4, 0, 4, 12, 15, 12),
            Block.box(0, 0, 4, 16, 15, 12),
            Shapes.or(
                    Block.box(0, 0, 8, 16, 15, 16),
                    Block.box(4, 0, 0, 12, 15, 16)
            ),
            Block.box(0, 0, 0, 16, 16, 16)
    ).build(id));

    DeferredBlock<Block> SUNSET_GLOW = BLOCKS.register("sunset_glow", id -> DrinkBlock.create().maxCount(3).shapes(
            Block.box(3, 0, 6, 13, 13, 10),
            Block.box(1, 0, 3, 15, 12, 12),
            Block.box(1, 0, 1, 16, 12, 13)
    ).build(id));

    DeferredBlock<Block> MADAME_SHEXIANG = BLOCKS.register("madame_shexiang", id -> DrinkBlock.create().maxCount(4).shapes(
            Block.box(4, 0, 4, 12, 15, 12),
            Block.box(0, 0, 4, 16, 15, 12),
            Shapes.or(
                    Block.box(0, 0, 8, 16, 15, 16),
                    Block.box(4, 0, 0, 12, 15, 16)
            ),
            Block.box(0, 0, 0, 16, 16, 16)
    ).build(id));

    DeferredBlock<Block> SWEET_BERRY_WINE = BLOCKS.register("sweet_berry_wine", id -> DrinkBlock.create().maxCount(4).shapes(
            Block.box(4, 0, 4, 12, 15, 12),
            Block.box(0, 0, 4, 16, 15, 12),
            Shapes.or(
                    Block.box(0, 0, 8, 16, 15, 16),
                    Block.box(4, 0, 0, 12, 15, 16)
            ),
            Block.box(0, 0, 0, 16, 16, 16)
    ).build(id));

    DeferredBlock<Block> SHERRY = BLOCKS.register("sherry", id -> DrinkBlock.create().maxCount(4).shapes(
            Block.box(6, 0, 6, 10, 16, 10),
            Block.box(2, 0, 6, 14, 16, 10),
            Shapes.or(
                    Block.box(2, 0, 10, 14, 16, 14),
                    Block.box(6, 0, 2, 10, 16, 14)
            ),
            Block.box(2, 0, 2, 14, 16, 14)
    ).build(id));

    DeferredBlock<Block> MOTHER_SNOW = BLOCKS.register("mother_snow", id -> DrinkBlock.create().maxCount(4).shapes(
            Block.box(4, 0, 4, 12, 15, 12),
            Block.box(0, 0, 4, 16, 15, 12),
            Shapes.or(
                    Block.box(0, 0, 8, 16, 15, 16),
                    Block.box(4, 0, 0, 12, 15, 16)
            ),
            Block.box(0, 0, 0, 16, 16, 16)
    ).build(id));

    DeferredBlock<Block> LUMINOUS_BRIDE = BLOCKS.register("luminous_bride", id -> DrinkBlock.create().maxCount(4).shapes(
            Block.box(6, 0, 6, 10, 16, 10),
            Block.box(2, 0, 6, 14, 16, 10),
            Shapes.or(
                    Block.box(2, 0, 10, 14, 16, 14),
                    Block.box(6, 0, 2, 10, 16, 14)
            ),
            Block.box(2, 0, 2, 14, 16, 14)
    ).build(id));

    DeferredBlock<Block> GLOWFLOWER_BREW = BLOCKS.register("glowflower_brew", id -> DrinkBlock.create().maxCount(4).shapes(
            Block.box(6, 0, 6, 10, 16, 10),
            Block.box(2, 0, 6, 14, 16, 10),
            Shapes.or(
                    Block.box(2, 0, 10, 14, 16, 14),
                    Block.box(6, 0, 2, 10, 16, 14)
            ),
            Block.box(2, 0, 2, 14, 16, 14)
    ).build(id));

    DeferredBlock<Block> SAUVIGNON_BLANC_DRY_WHITE = BLOCKS.register("sauvignon_blanc_dry_white", id -> DrinkBlock.create().maxCount(4).shapes(
            Block.box(6, 0, 6, 10, 16, 10),
            Block.box(2, 0, 6, 14, 16, 10),
            Shapes.or(
                    Block.box(2, 0, 10, 14, 16, 14),
                    Block.box(6, 0, 2, 10, 16, 14)
            ),
            Block.box(2, 0, 2, 14, 16, 14)
    ).build(id));
    DeferredBlock<Block> VINEGAR = BLOCKS.register("vinegar", id -> DrinkBlock.create().maxCount(4).shapes(
            Block.box(6, 0, 6, 10, 16, 10),
            Block.box(2, 0, 6, 14, 16, 10),
            Shapes.or(
                    Block.box(2, 0, 10, 14, 16, 14),
                    Block.box(6, 0, 2, 10, 16, 14)
            ),
            Block.box(2, 0, 2, 14, 16, 14)
    ).build(id));

    DeferredBlock<Block> WATERMELON_JUICE = BLOCKS.register("watermelon_juice", id -> DrinkBlock.create().maxCount(4).shapes(
            Block.box(6, 0, 6, 10, 16, 10),
            Block.box(2, 0, 6, 14, 16, 10),
            Shapes.or(
                    Block.box(2, 0, 10, 14, 16, 14),
                    Block.box(6, 0, 2, 10, 16, 14)
            ),
            Block.box(2, 0, 2, 14, 16, 14)
    ).build(id));

    // BlockEntity
    Supplier<BlockEntityType<ChalkboardBlockEntity>> CHALKBOARD_BE = BLOCK_ENTITIES.register(
            "chalkboard", () -> new BlockEntityType<>(ChalkboardBlockEntity::new, CHALKBOARD.get())
    );


    Supplier<BlockEntityType<BarStoolBlockEntity>> BAR_STOOL_BE = BLOCK_ENTITIES.register(
            "bar_stool", () -> new BlockEntityType<>(BarStoolBlockEntity::new,
                    BLUE_BAR_STOOL.get(),
                    GREEN_BAR_STOOL.get(),
                    ORANGE_BAR_STOOL.get(),
                    PURPLE_BAR_STOOL.get(),
                    YELLOW_BAR_STOOL.get(),
                    BLACK_BAR_STOOL.get(),
                    WHITE_BAR_STOOL.get(),
                    GRAY_BAR_STOOL.get(),
                    BROWN_BAR_STOOL.get(),
                    LIME_BAR_STOOL.get(),
                    MAGENTA_BAR_STOOL.get(),
                    CYAN_BAR_STOOL.get(),
                    LIGHT_BLUE_BAR_STOOL.get(),
                    PINK_BAR_STOOL.get(),
                    LIGHT_GRAY_BAR_STOOL.get(),
                    RED_BAR_STOOL.get())
    );

    Supplier<BlockEntityType<SandwichBlockEntity>> SANDWICH_BOARD_BE = BLOCK_ENTITIES.register(
            "sandwich_board", () -> new BlockEntityType<>(SandwichBlockEntity::new,
                    BASE_SANDWICH_BOARD.get(),
                    GRASS_SANDWICH_BOARD.get(),
                    ALLIUM_SANDWICH_BOARD.get(),
                    AZURE_BLUET_SANDWICH_BOARD.get(),
                    CORNFLOWER_SANDWICH_BOARD.get(),
                    ORCHID_SANDWICH_BOARD.get(),
                    PEONY_SANDWICH_BOARD.get(),
                    PINK_PETALS_SANDWICH_BOARD.get(),
                    PITCHER_PLANT_SANDWICH_BOARD.get(),
                    POPPY_SANDWICH_BOARD.get(),
                    SUNFLOWER_SANDWICH_BOARD.get(),
                    TORCHFLOWER_SANDWICH_BOARD.get(),
                    TULIP_SANDWICH_BOARD.get(),
                    WITHER_ROSE_SANDWICH_BOARD.get()
            )
    );

    Supplier<BlockEntityType<PressingTubBlockEntity>> PRESSING_TUB_BE = BLOCK_ENTITIES.register(
            "pressing_tub", () -> new BlockEntityType<>(PressingTubBlockEntity::new, PRESSING_TUB.get())
    );

    Supplier<BlockEntityType<DrinkBlockEntity>> DRINK_BE = BLOCK_ENTITIES.register(
            "drink", () -> new BlockEntityType<>(DrinkBlockEntity::new,
                    WINE.get(), CHAMPAGNE.get(), VODKA.get(), BRANDY.get(), CARIGNAN.get(),
                    SAKURA_WINE.get(), PLUM_WINE.get(), WHISKEY.get(), ICE_WINE.get(),
                    POLARIS_SWEET_WHITE.get(), HONEY_WINE.get(), RED_QUEEN.get(), MINERS_STAR.get(),
                    RUM.get(), RIESLING_DRY_WHITE.get(), SUNSET_GLOW.get(), MADAME_SHEXIANG.get(),
                    SWEET_BERRY_WINE.get(), SHERRY.get(), MOTHER_SNOW.get(), LUMINOUS_BRIDE.get(),
                    GLOWFLOWER_BREW.get(), SAUVIGNON_BLANC_DRY_WHITE.get(), VINEGAR.get(),
                    WATERMELON_JUICE.get()
            )
    );

    Supplier<BlockEntityType<BarrelBlockEntity>> BARREL_BE = BLOCK_ENTITIES.register(
            "barrel", () -> new BlockEntityType<>(BarrelBlockEntity::new, BARREL.get())
    );

    Supplier<BlockEntityType<TapBlockEntity>> TAP_BE = BLOCK_ENTITIES.register(
            "tap", () -> new BlockEntityType<>(TapBlockEntity::new, TAP.get())
    );

    Supplier<BlockEntityType<BarCabinetBlockEntity>> BAR_CABINET_BE = BLOCK_ENTITIES.register(
            "bar_cabinet", () -> new BlockEntityType<>(BarCabinetBlockEntity::new, BAR_CABINET.get(), GLASS_BAR_CABINET.get())
    );
}