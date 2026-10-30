package net.diaithir.relics;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** The small event handler: hits, hurts, the once-a-second tick and the Ring of Fate. */
public final class RelicEvents {
    private RelicEvents() {}

    static void register() {
        NeoForge.EVENT_BUS.addListener(LivingIncomingDamageEvent.class, RelicEvents::incoming);
        NeoForge.EVENT_BUS.addListener(PlayerTickEvent.Post.class, RelicEvents::tick);
        NeoForge.EVENT_BUS.addListener(LivingDeathEvent.class, RelicEvents::death);
    }

    private static void incoming(LivingIncomingDamageEvent e) {
        LivingEntity victim = e.getEntity();
        DamageSource src = e.getSource();
        if (!(victim.level() instanceof net.minecraft.server.level.ServerLevel)) return;
        if (src.getEntity() instanceof ServerPlayer atk && src.getDirectEntity() == atk && src.is(DamageTypes.PLAYER_ATTACK) && !Abilities.busy) {
            Equipped.Slot held = Equipped.held(atk);
            if (held != null) {
                boolean old = Abilities.busy;
                Abilities.busy = true;  // arcs and reflections caused by this blow do not start new ones
                try {
                    e.setAmount(Passives.hit(held.def(), atk, victim, e.getAmount(), held.stack()));
                } finally {
                    Abilities.busy = old;
                }
            }
        }
        if (victim instanceof ServerPlayer p && !Abilities.busy) {
            for (Equipped.Slot s : Equipped.trinkets(p)) {
                float a = Passives.hurt(s.def(), p, src, e.getAmount(), s.stack(), false);
                if (a <= 0) { e.setCanceled(true); return; }
                e.setAmount(a);
            }
            Equipped.Slot sh = Equipped.blocking(p);
            if (sh != null) {
                float a = Passives.hurt(sh.def(), p, src, e.getAmount(), sh.stack(), true);
                if (a <= 0) { e.setCanceled(true); return; }
                e.setAmount(a);
            }
        }
    }

    private static void tick(PlayerTickEvent.Post e) {
        if (!(e.getEntity() instanceof ServerPlayer p) || p.tickCount % 20 != 0) return;
        for (Equipped.Slot s : Equipped.trinkets(p)) Passives.tick(s.def(), p, s.stack());
        Equipped.Slot held = Equipped.held(p);
        if (held != null) Passives.tick(held.def(), p, held.stack());
    }

    private static void death(LivingDeathEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p) || e.getSource().is(DamageTypes.GENERIC_KILL) || e.getSource().is(DamageTypes.FELL_OUT_OF_WORLD)) return;
        for (Equipped.Slot s : Equipped.trinkets(p)) {
            if (Passives.fate(s.def(), p, s.stack())) {
                e.setCanceled(true);
                return;
            }
        }
    }
}
