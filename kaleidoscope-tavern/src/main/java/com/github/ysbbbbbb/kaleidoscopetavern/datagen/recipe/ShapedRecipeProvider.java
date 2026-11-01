package com.github.ysbbbbbb.kaleidoscopetavern.datagen.recipe;

import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;

import java.util.function.Supplier;

public class ShapedRecipeProvider extends ModRecipeProvider {
    public ShapedRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        // 沙发
        sofa(ModItems.WHITE_SOFA, Items.WHITE_WOOL);
        sofa(ModItems.ORANGE_SOFA, Items.ORANGE_WOOL);
        sofa(ModItems.MAGENTA_SOFA, Items.MAGENTA_WOOL);
        sofa(ModItems.LIGHT_BLUE_SOFA, Items.LIGHT_BLUE_WOOL);
        sofa(ModItems.YELLOW_SOFA, Items.YELLOW_WOOL);
        sofa(ModItems.LIME_SOFA, Items.LIME_WOOL);
        sofa(ModItems.PINK_SOFA, Items.PINK_WOOL);
        sofa(ModItems.GRAY_SOFA, Items.GRAY_WOOL);
        sofa(ModItems.LIGHT_GRAY_SOFA, Items.LIGHT_GRAY_WOOL);
        sofa(ModItems.CYAN_SOFA, Items.CYAN_WOOL);
        sofa(ModItems.PURPLE_SOFA, Items.PURPLE_WOOL);
        sofa(ModItems.BLUE_SOFA, Items.BLUE_WOOL);
        sofa(ModItems.BROWN_SOFA, Items.BROWN_WOOL);
        sofa(ModItems.GREEN_SOFA, Items.GREEN_WOOL);
        sofa(ModItems.BLACK_SOFA, Items.BLACK_WOOL);
        sofa(ModItems.RED_SOFA, Items.RED_WOOL);

        // 高脚凳
        barStool(ModItems.WHITE_BAR_STOOL, Items.WHITE_WOOL);
        barStool(ModItems.ORANGE_BAR_STOOL, Items.ORANGE_WOOL);
        barStool(ModItems.MAGENTA_BAR_STOOL, Items.MAGENTA_WOOL);
        barStool(ModItems.LIGHT_BLUE_BAR_STOOL, Items.LIGHT_BLUE_WOOL);
        barStool(ModItems.YELLOW_BAR_STOOL, Items.YELLOW_WOOL);
        barStool(ModItems.LIME_BAR_STOOL, Items.LIME_WOOL);
        barStool(ModItems.PINK_BAR_STOOL, Items.PINK_WOOL);
        barStool(ModItems.GRAY_BAR_STOOL, Items.GRAY_WOOL);
        barStool(ModItems.LIGHT_GRAY_BAR_STOOL, Items.LIGHT_GRAY_WOOL);
        barStool(ModItems.CYAN_BAR_STOOL, Items.CYAN_WOOL);
        barStool(ModItems.PURPLE_BAR_STOOL, Items.PURPLE_WOOL);
        barStool(ModItems.BLUE_BAR_STOOL, Items.BLUE_WOOL);
        barStool(ModItems.BROWN_BAR_STOOL, Items.BROWN_WOOL);
        barStool(ModItems.GREEN_BAR_STOOL, Items.GREEN_WOOL);
        barStool(ModItems.BLACK_BAR_STOOL, Items.BLACK_WOOL);
        barStool(ModItems.RED_BAR_STOOL, Items.RED_WOOL);

        // 黑板
        this.shaped(RecipeCategory.DECORATIONS, ModItems.CHALKBOARD.get())
                .pattern("III")
                .pattern("ISI")
                .pattern("III")
                .define('I', Items.INK_SAC)
                .define('S', ItemTags.SIGNS)
                .unlockedBy("has_ink_sac", has(Items.INK_SAC))
                .save(this.output);

        // 展板
        this.shaped(RecipeCategory.DECORATIONS, ModItems.BASE_SANDWICH_BOARD.get())
                .pattern("I")
                .pattern("S")
                .define('I', Items.INK_SAC)
                .define('S', ItemTags.WOODEN_SLABS)
                .unlockedBy("has_ink_sac", has(Items.INK_SAC))
                .save(this.output);

        // 灯串
        // 无色的
        this.shaped(RecipeCategory.DECORATIONS, ModItems.STRING_LIGHTS_COLORLESS.get(), 8)
                .pattern("CCC")
                .pattern("LLL")
                .define('C', Items.IRON_CHAIN)
                .define('L', Items.LANTERN)
                .unlockedBy("has_chain", has(Items.IRON_CHAIN))
                .save(this.output);

        // 有色灯串
        stringLights(ModItems.STRING_LIGHTS_WHITE, Items.WHITE_DYE);
        stringLights(ModItems.STRING_LIGHTS_ORANGE, Items.ORANGE_DYE);
        stringLights(ModItems.STRING_LIGHTS_MAGENTA, Items.MAGENTA_DYE);
        stringLights(ModItems.STRING_LIGHTS_LIGHT_BLUE, Items.LIGHT_BLUE_DYE);
        stringLights(ModItems.STRING_LIGHTS_YELLOW, Items.YELLOW_DYE);
        stringLights(ModItems.STRING_LIGHTS_LIME, Items.LIME_DYE);
        stringLights(ModItems.STRING_LIGHTS_PINK, Items.PINK_DYE);
        stringLights(ModItems.STRING_LIGHTS_GRAY, Items.GRAY_DYE);
        stringLights(ModItems.STRING_LIGHTS_LIGHT_GRAY, Items.LIGHT_GRAY_DYE);
        stringLights(ModItems.STRING_LIGHTS_CYAN, Items.CYAN_DYE);
        stringLights(ModItems.STRING_LIGHTS_PURPLE, Items.PURPLE_DYE);
        stringLights(ModItems.STRING_LIGHTS_BLUE, Items.BLUE_DYE);
        stringLights(ModItems.STRING_LIGHTS_BROWN, Items.BROWN_DYE);
        stringLights(ModItems.STRING_LIGHTS_GREEN, Items.GREEN_DYE);
        stringLights(ModItems.STRING_LIGHTS_BLACK, Items.BLACK_DYE);
        stringLights(ModItems.STRING_LIGHTS_RED, Items.RED_DYE);

        // 蒙德里安挂画是有序合成
        this.shaped(RecipeCategory.DECORATIONS, ModItems.MONDRIAN_PAINTING.get())
                .pattern(" B ")
                .pattern("WFY")
                .pattern(" R ")
                .define('F', Items.ITEM_FRAME)
                .define('B', Tags.Items.DYES_BLUE)
                .define('W', Tags.Items.DYES_WHITE)
                .define('Y', Tags.Items.DYES_YELLOW)
                .define('R', Tags.Items.DYES_RED)
                .unlockedBy("has_item_frame", has(Items.ITEM_FRAME))
                .save(this.output);

        // 吧台
        this.shaped(RecipeCategory.DECORATIONS, ModItems.BAR_COUNTER.get())
                .pattern("NNN")
                .pattern("WWW")
                .pattern("WWW")
                .define('N', Tags.Items.NUGGETS_GOLD)
                .define('W', ItemTags.PLANKS)
                .unlockedBy("has_nugget", has(Tags.Items.NUGGETS_GOLD))
                .save(this.output);

        // 人字梯
        this.shaped(RecipeCategory.DECORATIONS, ModItems.STEPLADDER.get())
                .pattern("L  ")
                .pattern("LL ")
                .pattern("LLL")
                .define('L', Items.LADDER)
                .unlockedBy("has_ladder", has(Items.LADDER))
                .save(this.output);

        // 藤架
        this.shaped(RecipeCategory.DECORATIONS, ModItems.TRELLIS.get(), 8)
                .pattern("G")
                .pattern("G")
                .pattern("G")
                .define('G', ModItems.GRAPEVINE.get())
                .unlockedBy("has_grapevine", has(ModItems.GRAPEVINE.get()))
                .save(this.output);

        // 龙头
        this.shaped(RecipeCategory.DECORATIONS, ModItems.TAP.get())
                .pattern("L")
                .pattern("H")
                .define('L', Items.LEVER)
                .define('H', Items.HOPPER)
                .unlockedBy("has_lever", has(Items.LEVER))
                .save(this.output);

        // 酒桶
        this.shaped(RecipeCategory.DECORATIONS, ModItems.BARREL.get())
                .pattern("BBB")
                .pattern("BBB")
                .pattern("BBB")
                .define('B', Items.BARREL)
                .unlockedBy("has_barrel", has(Items.BARREL))
                .save(this.output);

        // 酒柜
        this.shaped(RecipeCategory.DECORATIONS, ModItems.BAR_CABINET.get())
                .pattern("GGG")
                .pattern("G G")
                .pattern("GGG")
                .define('G', ModItems.GRAPEVINE.get())
                .unlockedBy("has_grapevine", has(ModItems.GRAPEVINE.get()))
                .save(this.output);

        // 玻璃酒柜
        this.shaped(RecipeCategory.DECORATIONS, ModItems.GLASS_BAR_CABINET.get())
                .pattern("GGG")
                .pattern("GPG")
                .pattern("GGG")
                .define('G', ModItems.GRAPEVINE.get())
                .define('P', Tags.Items.GLASS_PANES)
                .unlockedBy("has_grapevine", has(ModItems.GRAPEVINE.get()))
                .save(this.output);

        // 桌子
        this.shaped(RecipeCategory.DECORATIONS, ModItems.TABLE.get())
                .pattern("WWW")
                .pattern(" F ")
                .pattern(" I ")
                .define('W', ItemTags.PLANKS)
                .define('F', ItemTags.FENCES)
                .define('I', Tags.Items.INGOTS_IRON)
                .unlockedBy("has_fence", has(ItemTags.FENCES))
                .save(this.output);
    }

    private void sofa(Supplier<? extends Item> item, Item wool) {
        this.shaped(RecipeCategory.DECORATIONS, item.get())
                .pattern("W W")
                .pattern("WWW")
                .pattern("L L")
                .define('W', wool)
                .define('L', Tags.Items.INGOTS_IRON)
                .unlockedBy("has_wool", has(wool))
                .save(this.output);
    }

    private void barStool(Supplier<? extends Item> item, Item wool) {
        this.shaped(RecipeCategory.DECORATIONS, item.get())
                .pattern("W")
                .pattern("C")
                .pattern("L")
                .define('W', wool)
                .define('C', Items.IRON_CHAIN)
                .define('L', Tags.Items.INGOTS_IRON)
                .unlockedBy("has_wool", has(wool))
                .save(this.output);
    }

    private void stringLights(Supplier<? extends Item> item, Item dye) {
        this.shaped(RecipeCategory.DECORATIONS, item.get(), 8)
                .pattern("CCC")
                .pattern("LLL")
                .pattern("DDD")
                .define('C', Items.IRON_CHAIN)
                .define('L', Items.LANTERN)
                .define('D', dye)
                .unlockedBy("has_dye", has(dye))
                .save(this.output);
    }
}

