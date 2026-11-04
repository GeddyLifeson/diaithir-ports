package net.diaithir.hostile.hordes.procedures;

import net.diaithir.hostile.hordes.init.HordeHoardModGameRules;
import net.diaithir.hostile.hordes.init.HordeHoardModMobEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.LevelAccessor;

/**
 * Source event (1.20.1 Forge): LivingEvent.LivingTickEvent.
 * Wired in 26.1.2 from per-loader handlers.
 *
 * Iron golem regen + immunity tick: clears fire/wither/poison/levitation, applies
 * regeneration when no target (gated by IRONGOLEMREGENPOWER gamerule and the THINKING
 * cooldown effect set by IronGolemHealCooldownProcedure on attack), applies movement-
 * speed when chasing a villager-targeting mob.
 */
public class IronGolemTickProcedure {

    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (entity == null)
            return;
        if (!(world instanceof ServerLevel serverLevel))
            return;
        if (entity instanceof IronGolem) {
            if (!entity.isInLava() && entity.isOnFire()) {
                entity.clearFire();
            }
            if (entity instanceof LivingEntity _entity)
                _entity.removeEffect(MobEffects.WITHER);
            if (entity instanceof LivingEntity _entity)
                _entity.removeEffect(MobEffects.POISON);
            if (entity instanceof LivingEntity _entity)
                _entity.removeEffect(MobEffects.LEVITATION);
            if ((entity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null) == null) {
                if (!(entity instanceof LivingEntity _livEnt9 && _livEnt9.hasEffect(MobEffects.REGENERATION)) && !(entity instanceof LivingEntity _livEnt10 && _livEnt10.hasEffect(HordeHoardModMobEffects.THINKING))
                        && !((serverLevel.getGameRules().get(HordeHoardModGameRules.IRONGOLEMREGENPOWER)) < 1)) {
                    if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
                        _entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20, (int) ((serverLevel.getGameRules().get(HordeHoardModGameRules.IRONGOLEMREGENPOWER)) - 1), false, false));
                    if (!((entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1) == (entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1))) {
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(ParticleTypes.HAPPY_VILLAGER, (x + 0.8), (y + 1), (z + 0.8), 1, 1, 1, 1, 0.2);
                    }
                }
            } else if (!(entity instanceof LivingEntity _livEnt17 && _livEnt17.hasEffect(MobEffects.SPEED))) {
                if (((entity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null) instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null) instanceof Villager) {
                    if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
                        _entity.addEffect(new MobEffectInstance(MobEffects.SPEED, 100, 1, false, false));
                }
            }
        }
    }
}
