package com.github.ysbbbbbb.kaleidoscopetavern.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

public class BaseEffect extends MobEffect {
    public BaseEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    public BaseEffect(int color) {
        this(MobEffectCategory.BENEFICIAL, color);
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity livingEntity, int amplifier) {
        return true;
    }

    @Override
    public void applyInstantenousEffect(ServerLevel level, @Nullable Entity source, @Nullable Entity indirectSource,
                                        LivingEntity livingEntity, int amplifier, double scale) {
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false;
    }
}
