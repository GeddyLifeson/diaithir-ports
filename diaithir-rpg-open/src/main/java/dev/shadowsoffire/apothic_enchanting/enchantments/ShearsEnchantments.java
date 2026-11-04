package dev.shadowsoffire.apothic_enchanting.enchantments;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;

import dev.shadowsoffire.apothic_enchanting.Ench;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Util;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

public class ShearsEnchantments {

    private static final Map<DyeColor, ItemLike> ITEM_BY_DYE = Util.make(Maps.newEnumMap(DyeColor.class), map -> {
        map.put(DyeColor.WHITE, Blocks.WHITE_WOOL);
        map.put(DyeColor.ORANGE, Blocks.ORANGE_WOOL);
        map.put(DyeColor.MAGENTA, Blocks.MAGENTA_WOOL);
        map.put(DyeColor.LIGHT_BLUE, Blocks.LIGHT_BLUE_WOOL);
        map.put(DyeColor.YELLOW, Blocks.YELLOW_WOOL);
        map.put(DyeColor.LIME, Blocks.LIME_WOOL);
        map.put(DyeColor.PINK, Blocks.PINK_WOOL);
        map.put(DyeColor.GRAY, Blocks.GRAY_WOOL);
        map.put(DyeColor.LIGHT_GRAY, Blocks.LIGHT_GRAY_WOOL);
        map.put(DyeColor.CYAN, Blocks.CYAN_WOOL);
        map.put(DyeColor.PURPLE, Blocks.PURPLE_WOOL);
        map.put(DyeColor.BLUE, Blocks.BLUE_WOOL);
        map.put(DyeColor.BROWN, Blocks.BROWN_WOOL);
        map.put(DyeColor.GREEN, Blocks.GREEN_WOOL);
        map.put(DyeColor.RED, Blocks.RED_WOOL);
        map.put(DyeColor.BLACK, Blocks.BLACK_WOOL);
    });

    /**
     * Restores the pre-26.1 behaviour where Fortune on shears inflated sheep wool drops.
     * The old {@code SheepMixin} widened {@code Sheep#shear}'s hardcoded {@code nextInt(3)} to
     * {@code nextInt(3 + fortune*2)}; 26.1 moved the drop count into a loot table so that trick
     * is no longer possible. Instead we duplicate a wool entry up to {@code fortune*2} extra times,
     * which reproduces the OLD upper bound of {@code 3 + 2*fortune} when stacked on top of the
     * vanilla 1..3 uniform roll.
     */
    public static List<ItemStack> applyFortune(Sheep sheep, ItemStack shears, List<ItemStack> items, int fortune) {
        if (fortune <= 0 || items.isEmpty()) return items;
        ItemStack wool = null;
        for (ItemStack s : items) {
            if (s.is(ItemTags.WOOL)) {
                wool = s;
                break;
            }
        }
        if (wool == null) return items;
        List<ItemStack> out = new ArrayList<>(items);
        int bonus = sheep.getRandom().nextInt(fortune * 2 + 1);
        for (int i = 0; i < bonus; i++) {
            out.add(wool.copy());
        }
        return out;
    }

    public static List<ItemStack> applyChromatic(Sheep sheep, ItemStack shears, List<ItemStack> items) {
        if (EnchantmentHelper.has(shears, Ench.EnchantEffects.CHROMATIC)) {
            if (!(items instanceof ArrayList)) {
                // We need to make the list mutable, but applyExploitation might have already done that.
                items = new ArrayList<>(items);
            }

            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).is(ItemTags.WOOL)) {
                    items.set(i, new ItemStack(ITEM_BY_DYE.get(DyeColor.byId(sheep.getRandom().nextInt(16)))));
                }
            }
        }
        return items;
    }

    public static List<ItemStack> applyExploitation(Sheep sheep, ItemStack shears, List<ItemStack> items) {
        if (EnchantmentHelper.has(shears, Ench.EnchantEffects.EXPLOITATION)) {
            items = new ArrayList<>(items);
            if (items.size() > 0) {
                items.addAll(items.stream().map(ItemStack::copy).toList());
                sheep.hurtServer((ServerLevel) sheep.level(), sheep.level().damageSources().generic(), 2);
            }
        }
        return items;
    }

    public static void applyGrowthSerum(Sheep sheep, ItemStack shears) {
        Pair<Float, Integer> serumLevel = EnchantmentHelper.getHighestLevel(shears, Ench.EnchantEffects.GROWTH_SERUM);
        if (serumLevel != null && sheep.getRandom().nextFloat() <= serumLevel.getFirst()) {
            sheep.setSheared(false);
        }
    }

}
