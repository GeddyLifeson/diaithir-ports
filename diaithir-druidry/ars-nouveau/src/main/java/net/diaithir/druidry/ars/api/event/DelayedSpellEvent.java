package net.diaithir.druidry.ars.api.event;

import net.diaithir.druidry.ars.api.spell.SpellResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class DelayedSpellEvent implements ITimedEvent {
    public int duration;
    public final SpellResolver resolver;
    public final HitResult result;
    public final Level world;
    public final boolean showParticles;

    public DelayedSpellEvent(int delay, HitResult result, Level world, SpellResolver resolver) {
        this(delay, result, world, resolver, true);
    }

    public DelayedSpellEvent(int delay, HitResult result, Level world, SpellResolver resolver, boolean showParticles) {
        this.duration = delay;
        this.result = result;
        this.world = world;
        this.resolver = resolver;
        this.showParticles = showParticles;
    }

    @Override
    public void tick(boolean serverSide) {
        duration--;
        if (duration <= 0 && serverSide) {
            resolveSpell();
        }
    }

    public void resolveSpell() {
        if (world == null)
            return;
        if (result instanceof EntityHitResult ehr && ehr.getEntity().isRemoved()) {
            return;
        }
        resolver.resume(world);
    }

    @Override
    public boolean isExpired() {
        return duration <= 0 || world == null;
    }
}
