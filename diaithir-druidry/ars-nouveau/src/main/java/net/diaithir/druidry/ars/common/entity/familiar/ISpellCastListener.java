package net.diaithir.druidry.ars.common.entity.familiar;

import net.diaithir.druidry.ars.api.event.SpellCastEvent;
import net.diaithir.druidry.ars.api.event.SpellCostCalcEvent;
import net.diaithir.druidry.ars.api.event.SpellModifierEvent;

public interface ISpellCastListener {

    default void onCast(SpellCastEvent event) {
    }

    default void onModifier(SpellModifierEvent event) {
    }

    default void onCostCalc(SpellCostCalcEvent event) {
    }

}
