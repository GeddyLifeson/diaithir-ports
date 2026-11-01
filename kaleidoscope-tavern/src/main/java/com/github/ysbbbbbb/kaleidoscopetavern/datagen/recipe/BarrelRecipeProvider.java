package com.github.ysbbbbbb.kaleidoscopetavern.datagen.recipe;

import com.github.ysbbbbbb.kaleidoscopetavern.datagen.builder.BarrelBuilder;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModFluids;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

public class BarrelRecipeProvider extends ModRecipeProvider {
    public BarrelRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        // 葡萄酒
        BarrelBuilder.builder()
                .setFluid(ModFluids.GRAPE_JUICE.get())
                .setResult(ModItems.WINE.get())
                .save(this.output);

        // 香槟
        BarrelBuilder.builder()
                .setFluid(ModFluids.GRAPE_JUICE.get())
                .addIngredient(Items.SUGAR)
                .setResult(ModItems.CHAMPAGNE.get())
                .save(this.output);

        // 樱花葡萄酒
        BarrelBuilder.builder()
                .setFluid(ModFluids.GRAPE_JUICE.get())
                .addIngredient(Items.PINK_PETALS)
                .setResult(ModItems.SAKURA_WINE.get())
                .save(this.output);

        // 白兰地
        BarrelBuilder.builder()
                .setFluid(ModFluids.GRAPE_JUICE.get())
                .addIngredient(Items.APPLE)
                .setResult(ModItems.BRANDY.get())
                .save(this.output);

        // 佳丽酿
        BarrelBuilder.builder()
                .setFluid(ModFluids.GRAPE_JUICE.get())
                .addIngredient(Items.SWEET_BERRIES)
                .setResult(ModItems.CARIGNAN.get())
                .save(this.output);

        // 冰葡萄酒
        BarrelBuilder.builder()
                .setFluid(ModFluids.ICE_GRAPE_JUICE.get())
                .setResult(ModItems.ICE_WINE.get())
                .save(this.output);

        // 北极星甜白
        BarrelBuilder.builder()
                .setFluid(ModFluids.ICE_GRAPE_JUICE.get())
                .addIngredient(Items.PACKED_ICE)
                .setResult(ModItems.POLARIS_SWEET_WHITE.get())
                .save(this.output);

        // 雪婆婆
        BarrelBuilder.builder()
                .setFluid(ModFluids.ICE_GRAPE_JUICE.get())
                .addIngredient(Items.ICE)
                .setResult(ModItems.MOTHER_SNOW.get())
                .save(this.output);

        // 雪莉
        BarrelBuilder.builder()
                .setFluid(ModFluids.ICE_GRAPE_JUICE.get())
                .addIngredient(Items.BLUE_ICE)
                .setResult(ModItems.SHERRY.get())
                .save(this.output);

        // 矿工之星
        BarrelBuilder.builder()
                .setFluid(ModFluids.GOLD_GRAPE_JUICE.get())
                .addIngredient(Items.IRON_NUGGET)
                .setResult(ModItems.MINERS_STAR.get())
                .save(this.output);

        // 蜂蜜葡萄酒
        BarrelBuilder.builder()
                .setFluid(ModFluids.GOLD_GRAPE_JUICE.get())
                .addIngredient(Items.HONEYCOMB)
                .setResult(ModItems.HONEY_WINE.get())
                .save(this.output);

        // 奢香夫人
        BarrelBuilder.builder()
                .setFluid(ModFluids.GOLD_GRAPE_JUICE.get())
                .addIngredient(Items.GOLD_NUGGET)
                .setResult(ModItems.MADAME_SHEXIANG.get())
                .save(this.output);

        // 落日余晖
        BarrelBuilder.builder()
                .setFluid(ModFluids.GOLD_GRAPE_JUICE.get())
                .addIngredient(Items.BLAZE_POWDER)
                .setResult(ModItems.SUNSET_GLOW.get())
                .save(this.output);

        // 长相思干白
        BarrelBuilder.builder()
                .setFluid(ModFluids.GREEN_GRAPE_JUICE.get())
                .addIngredient(Items.SUGAR_CANE)
                .addIngredient(Items.SUGAR)
                .setResult(ModItems.SAUVIGNON_BLANC_DRY_WHITE.get())
                .save(this.output);

        // 雷司令干白
        BarrelBuilder.builder()
                .setFluid(ModFluids.GREEN_GRAPE_JUICE.get())
                .addIngredient(Items.GUNPOWDER)
                .addIngredient(Items.SUGAR)
                .setResult(ModItems.RIESLING_DRY_WHITE.get())
                .save(this.output);

        // 夜光新娘
        BarrelBuilder.builder()
                .setFluid(ModFluids.GLOW_BERRIES_JUICE.get())
                .addIngredient(Items.GLOW_INK_SAC)
                .setResult(ModItems.LUMINOUS_BRIDE.get())
                .save(this.output);

        // 萤花酿
        BarrelBuilder.builder()
                .setFluid(ModFluids.GLOW_BERRIES_JUICE.get())
                .addIngredient(Items.GLOWSTONE_DUST)
                .setResult(ModItems.GLOWFLOWER_BREW.get())
                .save(this.output);

        // 梅酒
        BarrelBuilder.builder()
                .setFluid(ModFluids.SWEET_BERRIES_JUICE.get())
                .addIngredient(Items.SUGAR)
                .setResult(ModItems.PLUM_WINE.get())
                .save(this.output);

        // 甜浆果酒
        BarrelBuilder.builder()
                .setFluid(ModFluids.SWEET_BERRIES_JUICE.get())
                .addIngredient(Items.SWEET_BERRIES)
                .setResult(ModItems.SWEET_BERRY_WINE.get())
                .save(this.output);

        // 红皇后
        BarrelBuilder.builder()
                .setFluid(ModFluids.SWEET_BERRIES_JUICE.get())
                .addIngredient(Items.REDSTONE)
                .setResult(ModItems.RED_QUEEN.get())
                .save(this.output);

        // 伏特加
        BarrelBuilder.builder()
                .setFluid(Fluids.WATER)
                .addIngredient(Items.POTATO)
                .setResult(ModItems.VODKA.get())
                .save(this.output);

        // 威士忌
        BarrelBuilder.builder()
                .setFluid(Fluids.WATER)
                .addIngredient(Items.WHEAT)
                .setResult(ModItems.WHISKEY.get())
                .save(this.output);

        // 朗姆酒
        BarrelBuilder.builder()
                .setFluid(Fluids.WATER)
                .addIngredient(Items.SUGAR_CANE)
                .setResult(ModItems.RUM.get())
                .save(this.output);

        // 燃烧瓶
        BarrelBuilder.builder()
                .setFluid(Fluids.LAVA)
                .setResult(ModItems.MOLOTOV.get())
                .save(this.output);
    }
}
