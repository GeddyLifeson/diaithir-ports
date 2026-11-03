package net.diaithir.druidry.ars.api.perk;

import net.diaithir.druidry.ars.api.event.EffectResolveEvent;
import net.diaithir.druidry.ars.api.event.SpellCastEvent;
import net.diaithir.druidry.ars.api.event.SpellDamageEvent;
import net.diaithir.druidry.ars.api.event.SpellResolveEvent;

public interface IEffectResolvePerk {

    default void onSpellCast(SpellCastEvent spellCastEvent, PerkInstance perkInstance){

    }

    default void onSpellPreResolve(SpellResolveEvent.Pre spellResolveEvent, PerkInstance perkInstance){

    }

    default void onSpellPostResolve(SpellResolveEvent.Post spellResolveEvent, PerkInstance perkInstance){

    }

    default void onEffectPreResolve(EffectResolveEvent.Pre event, PerkInstance perkInstance){
    }

    default void onEffectPostResolve(EffectResolveEvent.Post event, PerkInstance perkInstance){
    }

    default void onPreSpellDamageEvent(final SpellDamageEvent.Pre event, PerkInstance perkInstance){

    }

    default void onPostSpellDamageEvent(final SpellDamageEvent.Post event, PerkInstance perkInstance){

    }
}
