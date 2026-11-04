package net.diaithir.hostile.hordes.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.arrow.SpectralArrow;

/**
 * Source event (1.20.1 Forge): LivingAttackEvent (cancelable).
 * Wired in 26.1.2 from per-loader handlers. Returns true when the source event should
 * be cancelled (60% chance to deflect arrow damage on iron golems).
 *
 * NOTE on cancellation translation: source had the MCreator-generated boilerplate
 *     if (event.isCancelable()) event.setCanceled(true);
 *     else if (event.hasResult()) event.setResult(Event.Result.DENY);
 * The Event.Result.DENY branch was already dead code in modern Forge/NeoForge (the
 * Result type was removed). The mod only ever fires this against LivingAttackEvent
 * which has always been cancelable, so the second branch never ran in practice.
 * Collapsed to a boolean return.
 */
public class IronGolemAttackedProcedure {

    public static boolean execute(Entity entity, Entity immediatesourceentity) {
        if (entity == null || immediatesourceentity == null)
            return false;
        boolean cancel = false;
        if (entity instanceof IronGolem) {
            if (immediatesourceentity instanceof Arrow || immediatesourceentity instanceof SpectralArrow) {
                if (Math.random() < 0.6) {
                    cancel = true;
                }
            }
        }
        return cancel;
    }
}
