package com.diaithir.addons.newage.content.nuclear;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import com.diaithir.addons.newage.CNASounds;
import com.diaithir.addons.newage.config.CNAConfig;

public class RadiationPoisoningEffect extends MobEffect {
    public RadiationPoisoningEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(net.minecraft.server.level.ServerLevel serverLevel, LivingEntity livingEntity, int amplifier) {
        if (!livingEntity.level().isClientSide()) {
            if (CNAConfig.getServer().geigerCounterSounds.get()) {
                float clickChance = 0.15f + (amplifier * 0.10f);
                if (livingEntity.getRandom().nextFloat() < clickChance && livingEntity.getType() == EntityType.PLAYER) {
                    float pitch = 0.95f + livingEntity.getRandom().nextFloat() * 0.1f;
                    CNASounds.GEIGER_COUNTER.playOnServer(livingEntity.level(), livingEntity.blockPosition(), .75f, pitch);
                }
            }
            if (CNAConfig.getServer().nauseaInducingRadiation.get()) {
                MobEffectInstance nausea = livingEntity.getEffect(MobEffects.NAUSEA);
                if (nausea == null || nausea.getDuration() < 100) {
                    livingEntity.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 400, amplifier, false, false, false));
                }
            }

            MobEffectInstance fatigue = livingEntity.getEffect(MobEffects.MINING_FATIGUE);
            if (fatigue == null || fatigue.getDuration() < 100) {
                livingEntity.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 300, amplifier + 1, false, false, false));
            }

            if (livingEntity.tickCount % 40 == 0) {
                livingEntity.hurt(livingEntity.damageSources().magic(), 1.0F + amplifier);
            }
        }

        return super.applyEffectTick(serverLevel, livingEntity, amplifier);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
