package net.diaithir.druidry.ars.common.potions;

import net.diaithir.druidry.ars.client.particle.ParticleColor;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class FreezingEffect extends MobEffect {

    public FreezingEffect() {
        super(MobEffectCategory.HARMFUL, new ParticleColor(0, 0, 250).getColor());
    }

    @Override
    public boolean applyEffectTick(net.minecraft.server.level.ServerLevel serverLevel, LivingEntity pLivingEntity, int pAmplifier) {
        pLivingEntity.setTicksFrozen(Math.min(pLivingEntity.getTicksRequiredToFreeze() + 3, pLivingEntity.getTicksFrozen() + 3 + pAmplifier));
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int pDuration, int pAmplifier) {
        return true;
    }
}
