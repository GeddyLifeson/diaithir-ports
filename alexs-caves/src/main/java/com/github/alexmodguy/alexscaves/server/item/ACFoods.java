package com.github.alexmodguy.alexscaves.server.item;

import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

/** Foods in the 26.1 form: FoodProperties plus a Consumable carrying the status effects. */
public class ACFoods {
    public record Food(FoodProperties food, Consumable consumable, ItemStack remainder) {
        public net.minecraft.world.item.Item.Properties apply(net.minecraft.world.item.Item.Properties properties) {
            properties = properties.food(food, consumable);
            return remainder.isEmpty() ? properties : properties.usingConvertsTo(remainder.getItem());
        }
    }

    private static Food make(int nutrition, float sat, boolean always, MobEffectInstance effect, float prob, boolean bowl) {
        FoodProperties.Builder b = new FoodProperties.Builder().nutrition(nutrition).saturationModifier(sat);
        if (always) b.alwaysEdible();
        Consumable.Builder c = Consumable.builder();
        if (effect != null) c.onConsume(new ApplyStatusEffectsConsumeEffect(effect, prob));
        return new Food(b.build(), c.build(), bowl ? new ItemStack(Items.BOWL) : ItemStack.EMPTY);
    }

    public static final Food TRILOCARIS_TAIL = make(2, 0.3F, false, null, 1.0F, false);
    public static final Food TRILOCARIS_TAIL_COOKED = make(5, 0.5F, false, null, 1.0F, false);
    public static final Food PINE_NUTS = make(2, 0.175F, false, null, 1.0F, false);
    public static final Food DINOSAUR_NUGGETS = make(3, 0.3F, false, null, 1.0F, false);
    public static final Food SERENE_SALAD = make(5, 0.35F, false, null, 1.0F, false);
    public static final Food SEETHING_STEW = make(6, 0.6F, false, new MobEffectInstance(ACEffectRegistry.RAGE, 2200), 1.0F, false);
    public static final Food PRIMORDIAL_SOUP = make(6, 0.6F, false, new MobEffectInstance(MobEffects.DIG_SPEED, 800), 1.0F, false);
    public static final Food RADGILL = make(2, 0.2F, false, new MobEffectInstance(ACEffectRegistry.IRRADIATED, 2000), 1.0F, false);
    public static final Food RADGILL_COOKED = make(5, 0.3F, false, new MobEffectInstance(ACEffectRegistry.IRRADIATED, 1000), 0.1F, false);
    public static final Food SPELUNKIE = make(2, 0.1F, false, null, 1.0F, false);
    public static final Food SLAM = make(4, 0.5F, false, new MobEffectInstance(MobEffects.DAMAGE_BOOST, 400), 1.0F, false);
    public static final Food SOYLENT_GREEN = make(3, 0.35F, true, null, 1.0F, false);
    public static final Food LANTERNFISH = make(1, 0.175F, false, null, 1.0F, false);
    public static final Food LANTERNFISH_COOKED = make(2, 0.3F, false, null, 1.0F, false);
    public static final Food TRIPODFISH = make(2, 0.2F, false, null, 1.0F, false);
    public static final Food TRIPODFISH_COOKED = make(5, 0.34F, false, null, 1.0F, false);
    public static final Food SEA_PIG = make(1, 0.2F, false, new MobEffectInstance(MobEffects.HUNGER, 1200), 0.7F, false);
    public static final Food MUSSEL_COOKED = make(4, 0.3F, false, null, 1.0F, false);
    public static final Food DEEP_SEA_SUSHI_ROLL = make(7, 0.4F, false, null, 1.0F, false);
    public static final Food STINKY_FISH = make(1, 0.1F, false, new MobEffectInstance(ACEffectRegistry.STUNNED, 100), 1.0F, false);
    public static final Food VESPER_WING = make(3, 0.2F, false, new MobEffectInstance(MobEffects.HUNGER, 1200), 1.0F, false);
    public static final Food VESPER_SOUP = make(5, 0.3F, true, new MobEffectInstance(MobEffects.NIGHT_VISION, 2400), 1.0F, true);
    public static final Food DARKENED_APPLE = make(4, 0.35F, true, null, 1.0F, false);
    public static final Food BLOCK_OF_CHOCOLATE = make(2, 0.1F, false, new MobEffectInstance(ACEffectRegistry.SUGAR_RUSH, 200), 0.01F, false);
    public static final Food BLOCK_OF_FROSTING = make(1, 0.1F, false, new MobEffectInstance(ACEffectRegistry.SUGAR_RUSH, 200), 0.02F, false);
    public static final Food SWEET_PUFF = make(1, 0.1F, false, new MobEffectInstance(ACEffectRegistry.SUGAR_RUSH, 200), 0.02F, false);
    public static final Food CAKE_LAYER = make(1, 0.1F, false, new MobEffectInstance(ACEffectRegistry.SUGAR_RUSH, 200), 0.02F, false);
    public static final Food COOKIE = make(2, 0.1F, false, new MobEffectInstance(ACEffectRegistry.SUGAR_RUSH, 200), 0.01F, false);
    public static final Food COOKIE_HALF = make(1, 0.05F, false, new MobEffectInstance(ACEffectRegistry.SUGAR_RUSH, 200), 0.01F, false);
    public static final Food DOUGH = make(2, 0.1F, false, new MobEffectInstance(ACEffectRegistry.SUGAR_RUSH, 200), 0.01F, false);
    public static final Food LICOROOT = make(3, 0.1F, false, new MobEffectInstance(MobEffects.CONFUSION, 200), 0.1F, false);
    public static final Food LICOROOT_VINE = make(1, 0.1F, false, new MobEffectInstance(MobEffects.CONFUSION, 200), 0.1F, false);
    public static final Food SMALL_PEPPERMINT = make(3, 0.15F, false, new MobEffectInstance(ACEffectRegistry.SUGAR_RUSH, 200), 0.01F, false);
    public static final Food LARGE_PEPPERMINT = make(5, 0.15F, false, new MobEffectInstance(ACEffectRegistry.SUGAR_RUSH, 200), 0.01F, false);
    public static final Food VANILLA_ICE_CREAM = make(4, 0.2F, false, new MobEffectInstance(ACEffectRegistry.SUGAR_RUSH, 200), 0.03F, false);
    public static final Food CHOCOLATE_ICE_CREAM = make(4, 0.2F, false, new MobEffectInstance(ACEffectRegistry.SUGAR_RUSH, 200), 0.03F, false);
    public static final Food SWEETBERRY_ICE_CREAM = make(4, 0.2F, false, new MobEffectInstance(ACEffectRegistry.SUGAR_RUSH, 200), 0.03F, false);
    public static final Food SUNDAE = make(12, 0.35F, false, new MobEffectInstance(ACEffectRegistry.SUGAR_RUSH, 400), 0.2F, true);
    public static final Food SPRINKLES = make(1, 0.1F, false, new MobEffectInstance(ACEffectRegistry.SUGAR_RUSH, 200), 0.01F, false);
    public static final Food GIANT_SWEETBERRY = make(5, 0.2F, false, null, 1.0F, false);
    public static final Food CANDY_CANE = make(2, 0.1F, false, new MobEffectInstance(ACEffectRegistry.SUGAR_RUSH, 200), 0.01F, false);
    public static final Food CANDY_CANE_POLE = make(1, 0.1F, false, new MobEffectInstance(ACEffectRegistry.SUGAR_RUSH, 200), 0.01F, false);
    public static final Food LOLLIPOP_BUNCH = make(1, 0.1F, false, new MobEffectInstance(ACEffectRegistry.SUGAR_RUSH, 200), 0.01F, false);
    public static final Food FROSTMINT = make(3, 0.1F, false, new MobEffectInstance(ACEffectRegistry.SUGAR_RUSH, 200), 0.01F, false);
    public static final Food SUGAR_GLASS = make(1, 0.1F, false, new MobEffectInstance(ACEffectRegistry.SUGAR_RUSH, 200), 0.01F, false);
    public static final Food SUNDROP = make(5, 0.2F, false, new MobEffectInstance(ACEffectRegistry.SUGAR_RUSH, 200), 0.05F, false);
    public static final Food GUMMY_RING = make(3, 0.15F, false, new MobEffectInstance(ACEffectRegistry.SUGAR_RUSH, 200), 0.01F, false);
    public static final Food ROCK_CANDY = make(2, 0.1F, false, new MobEffectInstance(ACEffectRegistry.SUGAR_RUSH, 200), 0.01F, false);
    public static final Food JELLY_BEAN = make(1, 0.05F, false, null, 1.0F, false);
    public static final Food GINGERBREAD = make(4, 0.1F, false, new MobEffectInstance(ACEffectRegistry.SUGAR_RUSH, 200), 0.01F, false);
    public static final Food GINGERBREAD_HALF = make(2, 0.1F, false, new MobEffectInstance(ACEffectRegistry.SUGAR_RUSH, 200), 0.01F, false);
    public static final Food GINGERBREAD_CRUMBS = make(1, 0.1F, false, new MobEffectInstance(ACEffectRegistry.SUGAR_RUSH, 200), 0.01F, false);
    public static final Food PURPLE_SODA_BOTTLE = make(2, 0.1F, false, new MobEffectInstance(ACEffectRegistry.SUGAR_RUSH, 200), 0.01F, false);
    public static final Food SWEETISH_FISH = make(2, 0.2F, false, new MobEffectInstance(ACEffectRegistry.SUGAR_RUSH, 200), 0.01F, false);
    public static final Food GELATIN = make(3, 0.25F, false, new MobEffectInstance(ACEffectRegistry.SUGAR_RUSH, 200), 0.01F, false);
    public static final Food HOT_CHOCOLATE_BOTTLE = make(4, 0.25F, false, new MobEffectInstance(ACEffectRegistry.SUGAR_RUSH, 200), 0.02F, false);
    public static final Food PEPPERMINT_POWDER = make(1, 0.1F, false, new MobEffectInstance(ACEffectRegistry.SUGAR_RUSH, 200), 0.01F, false);
    public static final Food CARAMEL = make(2, 0.3F, false, new MobEffectInstance(ACEffectRegistry.SUGAR_RUSH, 200), 0.04F, false);
    public static final Food CARAMEL_APPLE = make(6, 0.2F, false, new MobEffectInstance(ACEffectRegistry.SUGAR_RUSH, 200), 0.02F, false);
    public static final Food GUMBALL_PILE = make(3, 0.2F, false, new MobEffectInstance(ACEffectRegistry.SUGAR_RUSH, 200), 0.01F, false);
    public static final Food ALEX_MEAL = make(40, 5.0F, false, new MobEffectInstance(MobEffects.CONFUSION, 200), 1.0F, false);
    public static final Food BIOME_TREAT = make(20, 0.1F, false, null, 1.0F, false);
    public static final Food BIOME_TREAT_DONE = make(1, 0.1F, false, null, 1.0F, false);
}
