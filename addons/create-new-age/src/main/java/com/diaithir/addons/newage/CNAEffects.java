package com.diaithir.addons.newage;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.diaithir.addons.newage.content.nuclear.RadiationPoisoningEffect;

public class CNAEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, CreateNewAge.MOD_ID);

    public static final DeferredHolder<MobEffect, RadiationPoisoningEffect> RADIATION_POISONING =
            EFFECTS.register("radiation_poisoning", () -> new RadiationPoisoningEffect(MobEffectCategory.HARMFUL, 0x48F542));

    public static void load() {}
}
