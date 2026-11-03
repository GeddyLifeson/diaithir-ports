package net.diaithir.hostile.hordes.procedures;

import net.diaithir.hostile.hordes.init.HordeHoardModMobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.golem.IronGolem;

/**
 * Source event (1.20.1 Forge): LivingAttackEvent.
 * Wired in 26.1.2 from per-loader handlers. Does not cancel.
 *
 * Adds the THINKING effect (160 ticks, amp 0) to attacked iron golems — the THINKING
 * effect is what gates IronGolemTickProcedure's regeneration logic.
 */
public class IronGolemHealCooldownProcedure {

    public static void execute(Entity entity) {
        if (entity == null)
            return;
        if (entity instanceof IronGolem) {
            if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
                _entity.addEffect(new MobEffectInstance(HordeHoardModMobEffects.THINKING, 160, 0, false, false));
        }
    }
}
