package cn.leolezury.eternalstarlight.common.registry;

import com.google.common.base.Suppliers;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class ESFoods {
	public static final Supplier<Food> JINGLESTEM_CRISP = Suppliers.memoize(() -> new F().nutrition(4).saturationModifier(0.8F).build());
	public static final Supplier<Food> JINGLESTEM_SANDWICH = Suppliers.memoize(() -> new F().nutrition(7).saturationModifier(0.8F).build());
	public static final Supplier<Food> LUNAR_BERRIES = Suppliers.memoize(() -> new F().nutrition(3).saturationModifier(0.8F).build());
	public static final Supplier<Food> BOULDERSHROOM_STEW = Suppliers.memoize(() -> new F().nutrition(6).saturationModifier(0.6F).effect(new MobEffectInstance(ESMobEffects.STICKY.asHolder(), 2400, 0), 0.8f).usingConvertsTo(Items.BOWL).build());
	public static final Supplier<Food> LUNARIS_CACTUS_FRUIT = Suppliers.memoize(() -> new F().nutrition(4).saturationModifier(0.1f).effect(new MobEffectInstance(MobEffects.GLOWING, 600, 0), 0.8F).effect(new MobEffectInstance(MobEffects.POISON, 200, 0), 0.8F).build());
	public static final Supplier<Food> LUNARIS_CACTUS_GEL = Suppliers.memoize(() -> new F().nutrition(3).saturationModifier(0.1f).build());
	public static final Supplier<Food> ABYSSAL_FRUIT = Suppliers.memoize(() -> new F().nutrition(4).saturationModifier(0.1F).effect(new MobEffectInstance(MobEffects.GLOWING, 600, 0), 0.3F).effect(new MobEffectInstance(MobEffects.WATER_BREATHING, 400, 0), 0.15F).alwaysEdible().build());
	public static final Supplier<Food> VELVETUMOSS_BALL = Suppliers.memoize(() -> new F().nutrition(1).saturationModifier(0.1F).effect(new MobEffectInstance(MobEffects.WATER_BREATHING, 400, 0), 0.2F).build());
	public static final Supplier<Food> CRINOA_BALL = Suppliers.memoize(() -> new F().nutrition(6).saturationModifier(0.8F).build());
	public static final Supplier<Food> CRINOA_PORRIDGE = Suppliers.memoize(() -> new F().nutrition(8).saturationModifier(0.8F).usingConvertsTo(Items.BOWL).build());
	public static final Supplier<Food> POPPED_NOCTURNAL_MILLET_BUCKET = Suppliers.memoize(() -> new F().nutrition(5).saturationModifier(0.8F).fast().build());
	public static final Supplier<Food> ROASTED_FORGOTTEN_NOCTURNAL_MILLET = Suppliers.memoize(() -> new F().nutrition(10).saturationModifier(0.8F).effect(new MobEffectInstance(ESMobEffects.OBLIVION.asHolder(), 300, 0), 1f).alwaysEdible().build());
	public static final Supplier<Food> PUNGENCY_FRUIT = Suppliers.memoize(() -> new F().nutrition(4).saturationModifier(0.6F).effect(new MobEffectInstance(MobEffects.NAUSEA, 120, 0), 0.8F).build());
	public static final Supplier<Food> PUNGENCY_STEW = Suppliers.memoize(() -> new F().nutrition(6).saturationModifier(0.6F).effect(new MobEffectInstance(MobEffects.NAUSEA, 120, 0), 0.8F).usingConvertsTo(Items.BOWL).build());
	public static final Supplier<Food> SILVER_PUNGENCY_FRUIT = Suppliers.memoize(() -> new F().nutrition(7).saturationModifier(0.6F).effect(new MobEffectInstance(MobEffects.NAUSEA, 120, 0), 0.8F).effect(new MobEffectInstance(ESMobEffects.NUMBNESS.asHolder(), 1200, 0), 1F).alwaysEdible().build());
	public static final Supplier<Food> ROTTEN_FLESH_JERKY = Suppliers.memoize(() -> new F().nutrition(5).saturationModifier(0.8F).fast().build());
	public static final Supplier<Food> ROOKFISH = Suppliers.memoize(() -> new F().nutrition(3).saturationModifier(0.1F).build());
	public static final Supplier<Food> COOKED_ROOKFISH = Suppliers.memoize(() -> new F().nutrition(7).saturationModifier(0.3F).build());
	public static final Supplier<Food> ROOKFISH_SKEWER = Suppliers.memoize(() -> new F().nutrition(3).saturationModifier(0.3F).fast().usingConvertsTo(Items.STICK).build());
	public static final Supplier<Food> LUMINOFISH = Suppliers.memoize(() -> new F().nutrition(2).saturationModifier(0.1F).build());
	public static final Supplier<Food> COOKED_LUMINOFISH = Suppliers.memoize(() -> new F().nutrition(6).saturationModifier(0.8F).build());
	public static final Supplier<Food> LUMINARIS = Suppliers.memoize(() -> new F().nutrition(2).saturationModifier(0.1F).build());
	public static final Supplier<Food> COOKED_LUMINARIS = Suppliers.memoize(() -> new F().nutrition(6).saturationModifier(0.8F).build());
	public static final Supplier<Food> CONICERAS_TENTACLES = Suppliers.memoize(() -> new F().nutrition(1).saturationModifier(0.1F).build());
	public static final Supplier<Food> GRILLED_CONICERAS_TENTACLES = Suppliers.memoize(() -> new F().nutrition(3).saturationModifier(0.6F).build());
	public static final Supplier<Food> AURORA_DEER_STEAK = Suppliers.memoize(() -> new F().nutrition(3).saturationModifier(0.6F).build());
	public static final Supplier<Food> COOKED_AURORA_DEER_STEAK = Suppliers.memoize(() -> new F().nutrition(8).saturationModifier(0.8F).build());
	public static final Supplier<Food> RATLIN_MEAT = Suppliers.memoize(() -> new F().nutrition(2).saturationModifier(0.3F).build());
	public static final Supplier<Food> COOKED_RATLIN_MEAT = Suppliers.memoize(() -> new F().nutrition(6).saturationModifier(0.8F).build());
	public static final Supplier<Food> SHADOW_SNAIL_MEAT = Suppliers.memoize(() -> new F().nutrition(2).saturationModifier(0.3F).effect(new MobEffectInstance(MobEffects.NAUSEA, 600, 0), 0.3F).build());
	public static final Supplier<Food> COOKED_SHADOW_SNAIL_MEAT = Suppliers.memoize(() -> new F().nutrition(5).saturationModifier(0.6F).build());
	public static final Supplier<Food> SHADOW_SNAIL_PIE = Suppliers.memoize(() -> new F().nutrition(9).saturationModifier(0.8F).build());
	public static final Supplier<Food> SHADOW_ESCARGOT = Suppliers.memoize(() -> new F().nutrition(10).saturationModifier(0.8F).usingConvertsTo(ESItems.SHADOW_SNAIL_SHELL.get()).build());
	public static final Supplier<Food> SEEKER_TENTACLE = Suppliers.memoize(() -> new F().nutrition(4).saturationModifier(0.1F).fast().build());
	public static final Supplier<Food> FUNGUS = Suppliers.memoize(() -> new F().nutrition(6).saturationModifier(1.2F).build());
	public static final Supplier<Food> DOOMEDEN_CARRION = Suppliers.memoize(() -> new F().nutrition(4).saturationModifier(0.1F).effect(new MobEffectInstance(MobEffects.HUNGER, 600, 0), 0.8F).build());
	public static final Supplier<Food> ROTTEN_HAM = Suppliers.memoize(() -> new F().nutrition(8).saturationModifier(0.8F).effect(new MobEffectInstance(MobEffects.HUNGER, 600, 0), 0.3F).build());

	public record Food(FoodProperties food, Consumable consumable, Item remainder) {
	}

	public static class F {
		private final FoodProperties.Builder b = new FoodProperties.Builder();
		private final Consumable.Builder c = Consumable.builder();
		private Item remainder;

		public F nutrition(int n) { b.nutrition(n); return this; }
		public F saturationModifier(float s) { b.saturationModifier(s); return this; }
		public F alwaysEdible() { b.alwaysEdible(); return this; }
		public F fast() { c.consumeSeconds(0.8F); return this; }
		public F effect(MobEffectInstance i, float p) { c.onConsume(new ApplyStatusEffectsConsumeEffect(i, p)); return this; }
		public F usingConvertsTo(Item i) { remainder = i; return this; }
		public Food build() { return new Food(b.build(), c.build(), remainder); }
	}
}
