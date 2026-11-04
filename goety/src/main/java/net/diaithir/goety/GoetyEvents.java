package net.diaithir.goety;

import net.diaithir.goety.entity.Servant;
import net.diaithir.goety.magic.Souls;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

/** Game events: a death at the hands of a caster, or of a caster's servant, feeds the totems. */
public final class GoetyEvents {
    private GoetyEvents() {}

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent e) {
        LivingEntity dead = e.getEntity();
        if (dead.level().isClientSide() || dead instanceof Player || dead instanceof Servant) return;
        Entity killer = e.getSource().getEntity();
        Player owner = null;
        if (killer instanceof Player p) owner = p;
        else if (killer instanceof Servant s && s.core().owner() instanceof Player p) owner = p;
        if (owner == null) return;
        int souls = Math.max(4, Math.round(dead.getMaxHealth() * 2));
        int stored = Souls.add(owner, souls);
        if (stored > 0 && dead.level() instanceof net.minecraft.server.level.ServerLevel sl) {
            sl.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL, dead.getX(), dead.getY() + 0.8, dead.getZ(), 6, 0.3, 0.4, 0.3, 0.02);
        }
    }
}
