package com.gregtechceu.gtceu.common.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForgeMod;

public class GTPoisonEffect extends MobEffect {

    public GTPoisonEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    
    
    public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) { return false; /* ported stub */ }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        int j = 25 >> amplifier;
        if (j > 0) {
            return duration % j == 0;
        } else {
            return true;
        }
    }
}
