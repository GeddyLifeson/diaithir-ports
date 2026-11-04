package net.diaithir.druidry.ars.common.perk;

import net.diaithir.druidry.ars.ArsNouveau;
import net.diaithir.druidry.ars.api.event.EffectResolveEvent;
import net.diaithir.druidry.ars.api.perk.IEffectResolvePerk;
import net.diaithir.druidry.ars.api.perk.Perk;
import net.diaithir.druidry.ars.api.perk.PerkInstance;
import net.diaithir.druidry.ars.api.spell.IDamageEffect;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.EntityHitResult;

public class IgnitePerk extends Perk implements IEffectResolvePerk {
    public static IgnitePerk INSTANCE = new IgnitePerk(ArsNouveau.prefix("thread_kindling"));

    public IgnitePerk(Identifier key) {
        super(key);
    }

    @Override
    public String getLangDescription() {
        return "Damaging effects cause the target to burn for a short duration before the effect resolves. Burn time is increased by 5 seconds per level.";
    }

    @Override
    public String getLangName() {
        return "Kindling";
    }

    @Override
    public void onEffectPreResolve(EffectResolveEvent.Pre event, PerkInstance perkInstance) {
        if (event.resolveEffect instanceof IDamageEffect damageEffect && event.rayTraceResult instanceof EntityHitResult entityHitResult) {
            if (damageEffect.canDamage(event.shooter, event.spellStats, event.resolver.spellContext, event.resolver, entityHitResult.getEntity()) && event.shooter != entityHitResult.getEntity()) {
                entityHitResult.getEntity().setRemainingFireTicks(20 * 5 * perkInstance.getSlot().value());
            }
        }
    }
}
