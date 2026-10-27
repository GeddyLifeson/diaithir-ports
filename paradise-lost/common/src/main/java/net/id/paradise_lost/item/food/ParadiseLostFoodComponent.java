package net.id.paradise_lost.item.food;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

@SuppressWarnings("unused")
public class ParadiseLostFoodComponent {
    /** Food plus the consumable (eat time and effects) and optional use-remainder, as split in 26.1. */
    public record Food(FoodProperties food, Consumable consumable, Item convertsTo) {
    }

    private static final float FAST = 0.8F;

    private static Food of(int nutrition, float saturation) {
        return new Food(new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation).build(), Consumable.builder().build(), null);
    }

    private static Food of(int nutrition, float saturation, float seconds, Item convertsTo, boolean alwaysEdible, MobEffectInstance effect, float chance) {
        FoodProperties.Builder b = new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation);
        if (alwaysEdible) b.alwaysEdible();
        Consumable.Builder c = Consumable.builder().consumeSeconds(seconds);
        if (effect != null) c.onConsume(new ApplyStatusEffectsConsumeEffect(effect, chance));
        return new Food(b.build(), c.build(), convertsTo);
    }

    private static final float DEF = Consumable.DEFAULT_CONSUME_SECONDS;

    public static final Food BLACKCURRANT = of(2, 0.5F, FAST, null, false, null, 0);
    public static final Food MOA_MEAT = of(3, 0.3F);
    public static final Food COOKED_MOA_MEAT = of(6, 1F);
    public static final Food POPOM_JELLY = of(1, 1.0F, DEF, null, true, new MobEffectInstance(MobEffects.INSTANT_HEALTH, 1, 0), 1.0F);
    public static final Food SWEDROOT = of(2, 1.5F);
    public static final Food GENERIC_WORSE = of(1, 0.25F, DEF, null, false, new MobEffectInstance(MobEffects.NAUSEA, 100, 0), 0.075F);
    public static final Food AMADRYS_NOODLES = of(7, 0.5F, DEF, Items.BOWL, false, null, 0);
    public static final Food AMADRYS_BREAD = of(5, 1.2F);
    public static final Food AMADRYS_BREAD_GLAZED = of(8, 1.4F);
    public static final Food AMADRYS_BREAD_GLAZED_FILLED = of(9, 1.4F, DEF, null, false, new MobEffectInstance(MobEffects.INSTANT_HEALTH, 1, 0), 1.0F);
    public static final Food BLACKCURRANT_PIE = of(9, 0.2F);
    public static final Food BLACKCURRANT_COOKIE = of(5, 1.2F);
    public static final Food ROOT_STEW = of(10, 1.5F, DEF, Items.BOWL, false, null, 0);
}
