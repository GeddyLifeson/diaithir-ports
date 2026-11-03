package net.diaithir.druidry.ars.setup.registry;

import net.diaithir.druidry.ars.ArsNouveau;
import net.diaithir.druidry.ars.api.perk.PerkAttributes;
import net.diaithir.druidry.ars.client.particle.ParticleColor;
import net.diaithir.druidry.ars.setup.config.StartupConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static net.diaithir.druidry.ars.ArsNouveau.MODID;

@EventBusSubscriber(modid = MODID)
public class ModPotions {

    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, MODID);
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(BuiltInRegistries.POTION, MODID);













    @SubscribeEvent
    private static void addRecipes(final RegisterBrewingRecipesEvent event) {
        // TODO(port): Ars potion effects/brewing mixes not ported (common.potions missing); vanilla mixes only
        PotionBrewing.Builder builder = event.getBuilder();
        builder.addMix(Potions.WATER, ItemsRegistry.WILDEN_WING.get(), Potions.LEAPING);
        builder.addMix(Potions.WATER, ItemsRegistry.WILDEN_HORN.get(), Potions.STRENGTH);
        builder.addMix(Potions.WATER, ItemsRegistry.WILDEN_SPIKE.get(), Potions.LONG_WATER_BREATHING);
    }
}
