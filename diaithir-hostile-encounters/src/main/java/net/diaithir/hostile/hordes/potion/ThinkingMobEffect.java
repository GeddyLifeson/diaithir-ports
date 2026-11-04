package net.diaithir.hostile.hordes.potion;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class ThinkingMobEffect extends MobEffect {

    public ThinkingMobEffect() {
        super(MobEffectCategory.NEUTRAL, -13434727);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(ServerLevel serverLevel, LivingEntity entity, int amplifier) {
        return true;
    }
}
