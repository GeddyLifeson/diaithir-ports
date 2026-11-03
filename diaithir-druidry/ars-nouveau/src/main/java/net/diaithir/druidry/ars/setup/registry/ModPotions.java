package net.diaithir.druidry.ars.setup.registry;

import net.diaithir.druidry.ars.ArsNouveau;
import net.diaithir.druidry.ars.api.perk.PerkAttributes;
import net.diaithir.druidry.ars.client.particle.ParticleColor;
import net.diaithir.druidry.ars.common.potions.*;
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
import static net.diaithir.druidry.ars.common.lib.LibPotions.*;

@EventBusSubscriber(modid = MODID)
public class ModPotions {

    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, MODID);
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(BuiltInRegistries.POTION, MODID);

    public static final DeferredHolder<MobEffect, ShockedEffect> SHOCKED_EFFECT = EFFECTS.register(SHOCKED, ShockedEffect::new);

    public static final DeferredHolder<MobEffect, MobEffect> MANA_REGEN_EFFECT = EFFECTS.register(MANA_REGEN, () -> new PublicEffect(MobEffectCategory.BENEFICIAL, 8080895).addAttributeModifier(PerkAttributes.MANA_REGEN_BONUS, ArsNouveau.prefix("mana_regen_bonus"), StartupConfig.MANA_REGEN_POTION.get(), AttributeModifier.Operation.ADD_VALUE));
    public static final DeferredHolder<MobEffect, SummoningSicknessEffect> SUMMONING_SICKNESS_EFFECT = EFFECTS.register(SUMMONING_SICKNESS, SummoningSicknessEffect::new);

    public static final DeferredHolder<MobEffect, MobEffect> HEX_EFFECT = EFFECTS.register(HEX, () -> new PublicEffect(MobEffectCategory.HARMFUL, 8080895)
            .addAttributeModifier(PerkAttributes.MANA_REGEN_BONUS, ArsNouveau.prefix("hex_regen_penalty"), -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    public static final DeferredHolder<MobEffect, MobEffect> LOOTING_EFFECT = EFFECTS.register(LOOTING, () -> new PublicEffect(MobEffectCategory.BENEFICIAL, 7158556).addAttributeModifier(PerkAttributes.DRYGMY, ArsNouveau.prefix("looting_bonus"), 1.0, AttributeModifier.Operation.ADD_VALUE));
    public static final DeferredHolder<MobEffect, PublicEffect> SCRYING_EFFECT = EFFECTS.register(SCRYING, () -> new PublicEffect(MobEffectCategory.BENEFICIAL, 2039587));
    public static final DeferredHolder<MobEffect, PublicEffect> GLIDE_EFFECT = EFFECTS.register(GLIDE, () -> new PublicEffect(MobEffectCategory.BENEFICIAL, 8080895));
    public static final DeferredHolder<MobEffect, SnareEffect> SNARE_EFFECT = EFFECTS.register(SNARE, SnareEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> FLIGHT_EFFECT = EFFECTS.register(FLIGHT, () -> new PublicEffect(MobEffectCategory.BENEFICIAL, 2039587).addAttributeModifier(NeoForgeMod.CREATIVE_FLIGHT, ArsNouveau.prefix("flight"), 1.0, AttributeModifier.Operation.ADD_VALUE));
    public static final DeferredHolder<MobEffect, GravityEffect> GRAVITY_EFFECT = EFFECTS.register(GRAVITY, GravityEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> SPELL_DAMAGE_EFFECT = EFFECTS.register(SPELL_DAMAGE, () -> new PublicEffect(MobEffectCategory.BENEFICIAL, new ParticleColor(30, 200, 200).getColor()).addAttributeModifier(PerkAttributes.SPELL_DAMAGE_BONUS, ArsNouveau.prefix("spell_damage_bonus"), 1.0, AttributeModifier.Operation.ADD_VALUE));
    public static final DeferredHolder<MobEffect, ImmolateEffect> IMMOLATE_EFFECT = EFFECTS.register(IMMOLATE, ImmolateEffect::new);
    public static final DeferredHolder<MobEffect, BounceEffect> BOUNCE_EFFECT = EFFECTS.register(BOUNCE, BounceEffect::new);
    public static final DeferredHolder<MobEffect, MagicFindEffect> MAGIC_FIND_EFFECT = EFFECTS.register(MAGIC_FIND, MagicFindEffect::new);
    public static final DeferredHolder<MobEffect, SoakedEffect> SOAKED_EFFECT = EFFECTS.register(SOAKED, SoakedEffect::new);


    public static final DeferredHolder<MobEffect, PublicEffect> RECOVERY_EFFECT = EFFECTS.register(RECOVERY, () -> new PublicEffect(MobEffectCategory.BENEFICIAL, new ParticleColor(0, 200, 40).getColor()));
    public static final DeferredHolder<MobEffect, BlastEffect> BLAST_EFFECT = EFFECTS.register(BLAST, BlastEffect::new);
    public static final DeferredHolder<MobEffect, FreezingEffect> FREEZING_EFFECT = EFFECTS.register(FREEZING, FreezingEffect::new);
    public static final DeferredHolder<MobEffect, PublicEffect> DEFENCE_EFFECT = EFFECTS.register(DEFENCE, () -> new PublicEffect(MobEffectCategory.BENEFICIAL, new ParticleColor(150, 0, 150).getColor()));

    public static final DeferredHolder<Potion, Potion> MANA_REGEN_POTION = POTIONS.register(potion(MANA_REGEN), () -> new Potion(potion(MANA_REGEN), new MobEffectInstance(MANA_REGEN_EFFECT, 3600)));
    public static final DeferredHolder<Potion, Potion> LONG_MANA_REGEN_POTION = POTIONS.register(longPotion(MANA_REGEN), () -> new Potion(longPotion(MANA_REGEN), new MobEffectInstance(MANA_REGEN_EFFECT, 9600)));
    public static final DeferredHolder<Potion, Potion> STRONG_MANA_REGEN_POTION = POTIONS.register(strongPotion(MANA_REGEN), () -> new Potion(strongPotion(MANA_REGEN), new MobEffectInstance(MANA_REGEN_EFFECT, 3600, 1)));

    public static final DeferredHolder<Potion, Potion> SPELL_DAMAGE_POTION = POTIONS.register(potion(SPELL_DAMAGE), () -> new Potion(potion(SPELL_DAMAGE), new MobEffectInstance(SPELL_DAMAGE_EFFECT, 3600)));
    public static final DeferredHolder<Potion, Potion> SPELL_DAMAGE_POTION_LONG = POTIONS.register(longPotion(SPELL_DAMAGE), () -> new Potion(longPotion(SPELL_DAMAGE), new MobEffectInstance(SPELL_DAMAGE_EFFECT, 9600)));
    public static final DeferredHolder<Potion, Potion> SPELL_DAMAGE_POTION_STRONG = POTIONS.register(strongPotion(SPELL_DAMAGE), () -> new Potion(strongPotion(SPELL_DAMAGE), new MobEffectInstance(SPELL_DAMAGE_EFFECT, 3600, 1)));

    public static final DeferredHolder<Potion, Potion> RECOVERY_POTION = POTIONS.register(potion(RECOVERY), () -> new Potion(potion(RECOVERY), new MobEffectInstance(RECOVERY_EFFECT, 3600)));
    public static final DeferredHolder<Potion, Potion> LONG_RECOVERY_POTION = POTIONS.register(longPotion(RECOVERY), () -> new Potion(longPotion(RECOVERY), new MobEffectInstance(RECOVERY_EFFECT, 9600)));
    public static final DeferredHolder<Potion, Potion> STRONG_RECOVERY_POTION = POTIONS.register(strongPotion(RECOVERY), () -> new Potion(strongPotion(RECOVERY), new MobEffectInstance(RECOVERY_EFFECT, 3600, 1)));

    public static final DeferredHolder<Potion, Potion> BLAST_POTION = POTIONS.register(potion(BLAST), () -> new Potion(potion(BLAST), new MobEffectInstance(BLAST_EFFECT, 200)));
    public static final DeferredHolder<Potion, Potion> LONG_BLAST_POTION = POTIONS.register(longPotion(BLAST), () -> new Potion(longPotion(BLAST), new MobEffectInstance(BLAST_EFFECT, 400)));
    public static final DeferredHolder<Potion, Potion> STRONG_BLAST_POTION = POTIONS.register(strongPotion(BLAST), () -> new Potion(strongPotion(BLAST), new MobEffectInstance(BLAST_EFFECT, 140, 1)));

    public static final DeferredHolder<Potion, Potion> FREEZING_POTION = POTIONS.register(potion(FREEZING), () -> new Potion(potion(FREEZING), new MobEffectInstance(FREEZING_EFFECT, 1800)));
    public static final DeferredHolder<Potion, Potion> LONG_FREEZING_POTION = POTIONS.register(longPotion(FREEZING), () -> new Potion(longPotion(FREEZING), new MobEffectInstance(FREEZING_EFFECT, 3600)));
    public static final DeferredHolder<Potion, Potion> STRONG_FREEZING_POTION = POTIONS.register(strongPotion(FREEZING), () -> new Potion(strongPotion(FREEZING), new MobEffectInstance(FREEZING_EFFECT, 1800, 1)));

    public static final DeferredHolder<Potion, Potion> DEFENCE_POTION = POTIONS.register(potion(DEFENCE), () -> new Potion(potion(DEFENCE), new MobEffectInstance(DEFENCE_EFFECT, 3600)));
    public static final DeferredHolder<Potion, Potion> LONG_DEFENCE_POTION = POTIONS.register(longPotion(DEFENCE), () -> new Potion(longPotion(DEFENCE), new MobEffectInstance(DEFENCE_EFFECT, 9600)));
    public static final DeferredHolder<Potion, Potion> STRONG_DEFENCE_POTION = POTIONS.register(strongPotion(DEFENCE), () -> new Potion(strongPotion(DEFENCE), new MobEffectInstance(DEFENCE_EFFECT, 3600, 1)));

    @SubscribeEvent
    private static void addRecipes(final RegisterBrewingRecipesEvent event) {
        PotionBrewing.Builder builder = event.getBuilder();
        builder.addMix(Potions.WATER, ItemsRegistry.ABJURATION_ESSENCE.get(), Potions.AWKWARD);
        builder.addMix(Potions.AWKWARD, ItemsRegistry.MAGE_BLOOM.get(), ModPotions.SPELL_DAMAGE_POTION);

        builder.addMix(ModPotions.SPELL_DAMAGE_POTION, Items.GLOWSTONE_DUST, ModPotions.SPELL_DAMAGE_POTION_STRONG);
        builder.addMix(ModPotions.SPELL_DAMAGE_POTION, Items.REDSTONE, ModPotions.SPELL_DAMAGE_POTION_LONG);

        builder.addMix(Potions.AWKWARD, BlockRegistry.SOURCEBERRY_BUSH.asItem(), ModPotions.MANA_REGEN_POTION);
        builder.addMix(ModPotions.MANA_REGEN_POTION, Items.GLOWSTONE_DUST, ModPotions.STRONG_MANA_REGEN_POTION);
        builder.addMix(ModPotions.MANA_REGEN_POTION, Items.REDSTONE, ModPotions.LONG_MANA_REGEN_POTION);

        builder.addMix(Potions.AWKWARD, BlockRegistry.MENDOSTEEN_POD.asItem(), ModPotions.RECOVERY_POTION);
        builder.addMix(ModPotions.RECOVERY_POTION, Items.GLOWSTONE_DUST, ModPotions.STRONG_RECOVERY_POTION);
        builder.addMix(ModPotions.RECOVERY_POTION, Items.REDSTONE, ModPotions.LONG_RECOVERY_POTION);

        builder.addMix(Potions.AWKWARD, BlockRegistry.BOMBEGRANTE_POD.asItem(), ModPotions.BLAST_POTION);
        builder.addMix(ModPotions.BLAST_POTION, Items.GLOWSTONE_DUST, ModPotions.STRONG_BLAST_POTION);
        builder.addMix(ModPotions.BLAST_POTION, Items.REDSTONE, ModPotions.LONG_BLAST_POTION);

        builder.addMix(Potions.AWKWARD, BlockRegistry.FROSTAYA_POD.asItem(), ModPotions.FREEZING_POTION);
        builder.addMix(ModPotions.FREEZING_POTION, Items.GLOWSTONE_DUST, ModPotions.STRONG_FREEZING_POTION);
        builder.addMix(ModPotions.FREEZING_POTION, Items.REDSTONE, ModPotions.LONG_FREEZING_POTION);

        builder.addMix(Potions.AWKWARD, BlockRegistry.BASTION_POD.asItem(), ModPotions.DEFENCE_POTION);
        builder.addMix(ModPotions.DEFENCE_POTION, Items.GLOWSTONE_DUST, ModPotions.STRONG_DEFENCE_POTION);
        builder.addMix(ModPotions.DEFENCE_POTION, Items.REDSTONE, ModPotions.LONG_DEFENCE_POTION);

        builder.addMix(Potions.WATER, ItemsRegistry.WILDEN_WING.get(), Potions.LEAPING);
        builder.addMix(Potions.WATER, ItemsRegistry.WILDEN_HORN.get(), Potions.STRENGTH);
        builder.addMix(Potions.WATER, ItemsRegistry.WILDEN_SPIKE.get(), Potions.LONG_WATER_BREATHING);
    }
}