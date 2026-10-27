package com.github.alexmodguy.alexscaves.server.potion;


import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.server.item.ACItemRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ACEffectRegistry {

    public static final DeferredRegister<MobEffect> DEF_REG = DeferredRegister.create(Registries.MOB_EFFECT, AlexsCaves.MODID);
    public static final DeferredRegister<Potion> POTION_DEF_REG = DeferredRegister.create(Registries.POTION, AlexsCaves.MODID);
    public static final Holder<MobEffect> MAGNETIZING = DEF_REG.register("magnetizing", () -> new MagnetizedEffect());
    public static final Holder<MobEffect> STUNNED = DEF_REG.register("stunned", () -> new StunnedEffect());
    public static final Holder<MobEffect> RAGE = DEF_REG.register("rage", () -> new RageEffect());
    public static final Holder<MobEffect> IRRADIATED = DEF_REG.register("irradiated", () -> new IrradiatedEffect());
    public static final Holder<MobEffect> BUBBLED = DEF_REG.register("bubbled", () -> new BubbledEffect());
    public static final Holder<MobEffect> DEEPSIGHT = DEF_REG.register("deepsight", () -> new DeepsightEffect());
    public static final Holder<MobEffect> DARKNESS_INCARNATE = DEF_REG.register("darkness_incarnate", () -> new DarknessIncarnateEffect());
    public static final Holder<MobEffect> SUGAR_RUSH = DEF_REG.register("sugar_rush", () -> new SugarRushEffect());
    public static final DeferredHolder<Potion, Potion> MAGNETIZING_POTION = POTION_DEF_REG.register("magnetizing", () -> new Potion("magnetizing", new MobEffectInstance(MAGNETIZING, 3600)));
    public static final DeferredHolder<Potion, Potion> LONG_MAGNETIZING_POTION = POTION_DEF_REG.register("long_magnetizing", () -> new Potion("long_magnetizing", new MobEffectInstance(MAGNETIZING, 9600)));
    public static final DeferredHolder<Potion, Potion> DEEPSIGHT_POTION = POTION_DEF_REG.register("deepsight", () -> new Potion("deepsight", new MobEffectInstance(DEEPSIGHT, 3600)));
    public static final DeferredHolder<Potion, Potion> LONG_DEEPSIGHT_POTION = POTION_DEF_REG.register("long_deepsight", () -> new Potion("long_deepsight", new MobEffectInstance(DEEPSIGHT, 9600)));
    public static final DeferredHolder<Potion, Potion> GLOWING_POTION = POTION_DEF_REG.register("glowing", () -> new Potion("glowing", new MobEffectInstance(MobEffects.GLOWING, 3600)));
    public static final DeferredHolder<Potion, Potion> LONG_GLOWING_POTION = POTION_DEF_REG.register("long_glowing", () -> new Potion("long_glowing", new MobEffectInstance(MobEffects.GLOWING, 9600)));
    public static final DeferredHolder<Potion, Potion> HASTE_POTION = POTION_DEF_REG.register("haste", () -> new Potion("haste", new MobEffectInstance(MobEffects.HASTE, 3600)));
    public static final DeferredHolder<Potion, Potion> LONG_HASTE_POTION = POTION_DEF_REG.register("long_haste", () -> new Potion("long_haste", new MobEffectInstance(MobEffects.HASTE, 9600)));
    public static final DeferredHolder<Potion, Potion> STRONG_HASTE_POTION = POTION_DEF_REG.register("strong_haste", () -> new Potion("strong_haste", new MobEffectInstance(MobEffects.HASTE, 1800, 1)));
    public static final DeferredHolder<Potion, Potion> STRONG_HUNGER_POTION = POTION_DEF_REG.register("strong_hunger", () -> new Potion("strong_hunger", new MobEffectInstance(MobEffects.HUNGER, 1800, 4)));
    public static final DeferredHolder<Potion, Potion> SUGAR_RUSH_POTION = POTION_DEF_REG.register("sugar_rush", () -> new Potion("sugar_rush", new MobEffectInstance(SUGAR_RUSH, 1800)));
    public static final DeferredHolder<Potion, Potion> LONG_SUGAR_RUSH_POTION = POTION_DEF_REG.register("long_sugar_rush", () -> new Potion("long_sugar_rush", new MobEffectInstance(SUGAR_RUSH, 3600)));


    /** Brewing recipes for the cave potions are not ported yet (the 26.1 brewing builder works on potion holders). */
    public static void registerBrewingRecipes(net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent event) {
    }

    public static void setup() {
        // Brewing recipes are now registered via RegisterBrewingRecipesEvent
    }

    public static ItemStack createPotion(Holder<Potion> potion) {
        ItemStack stack = new ItemStack(Items.POTION);
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
        return stack;
    }

    public static ItemStack createPotion(DeferredHolder<Potion, Potion> potion) {
        return createPotion((Holder<Potion>) potion);
    }

    public static ItemStack createPotion(Potion potion) {
        // For Potion instances, we need to create a direct holder
        ItemStack stack = new ItemStack(Items.POTION);
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(Holder.direct(potion)));
        return stack;
    }

    public static ItemStack createSplashPotion(Potion potion) {
        ItemStack stack = new ItemStack(Items.SPLASH_POTION);
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(Holder.direct(potion)));
        return stack;
    }

    public static ItemStack createLingeringPotion(Potion potion) {
        ItemStack stack = new ItemStack(Items.LINGERING_POTION);
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(Holder.direct(potion)));
        return stack;
    }

    public static ItemStack createJellybean(Potion potion) {
        ItemStack stack = new ItemStack(ACItemRegistry.JELLY_BEAN.get());
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(Holder.direct(potion)));
        return stack;
    }
}
