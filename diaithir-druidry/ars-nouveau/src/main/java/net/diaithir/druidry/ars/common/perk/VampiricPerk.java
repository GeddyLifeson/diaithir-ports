package net.diaithir.druidry.ars.common.perk;

import net.diaithir.druidry.ars.ArsNouveau;
import net.diaithir.druidry.ars.api.event.SpellDamageEvent;
import net.diaithir.druidry.ars.api.perk.IEffectResolvePerk;
import net.diaithir.druidry.ars.api.perk.Perk;
import net.diaithir.druidry.ars.api.perk.PerkInstance;
import net.diaithir.druidry.ars.api.util.PerkUtil;
import net.minecraft.resources.Identifier;

public class VampiricPerk extends Perk implements IEffectResolvePerk {
    public static VampiricPerk INSTANCE = new VampiricPerk(ArsNouveau.prefix( "thread_life_drain"));
    public VampiricPerk(Identifier key) {
        super(key);
    }

    @Override
    public String getLangDescription() {
        return "Dealing damage with spells heals you for 20%% per level of the damage dealt.";
    }

    @Override
    public String getLangName() {
        return "Life Drain";
    }

    @Override
    public void onPostSpellDamageEvent(SpellDamageEvent.Post event, PerkInstance perkInstance) {
        float healAmount = event.damage * (0.2f * PerkUtil.countForPerk(perkInstance.getPerk(), event.caster));
        event.caster.heal(healAmount);
    }
}
