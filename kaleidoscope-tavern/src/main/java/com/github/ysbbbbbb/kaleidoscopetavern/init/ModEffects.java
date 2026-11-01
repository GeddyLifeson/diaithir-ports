package com.github.ysbbbbbb.kaleidoscopetavern.init;

import com.github.ysbbbbbb.kaleidoscopetavern.KaleidoscopeTavern;
import com.github.ysbbbbbb.kaleidoscopetavern.effect.BaseEffect;
import com.github.ysbbbbbb.kaleidoscopetavern.effect.GrassStealthEffect;
import com.github.ysbbbbbb.kaleidoscopetavern.effect.HighHeelsEffect;
import com.github.ysbbbbbb.kaleidoscopetavern.effect.VisionEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public interface ModEffects {
    DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, KaleidoscopeTavern.MOD_ID);

    DeferredHolder<MobEffect, MobEffect> SLIGHTLY_TIPSY = EFFECTS.register("slightly_tipsy", () -> new BaseEffect(MobEffectCategory.NEUTRAL, 0xFFD94A));
    DeferredHolder<MobEffect, MobEffect> HIGH_HEELS = EFFECTS.register("high_heels", () -> new HighHeelsEffect(0xE85BAA));
    DeferredHolder<MobEffect, MobEffect> GRASS_STEALTH = EFFECTS.register("grass_stealth", () -> new GrassStealthEffect(0x71BDE7));
    DeferredHolder<MobEffect, MobEffect> VISION = EFFECTS.register("vision", () -> new VisionEffect(0x408997));
    DeferredHolder<MobEffect, MobEffect> BLOODY_MARY = EFFECTS.register("bloody_mary", () -> new BaseEffect(0xF73A36));
}