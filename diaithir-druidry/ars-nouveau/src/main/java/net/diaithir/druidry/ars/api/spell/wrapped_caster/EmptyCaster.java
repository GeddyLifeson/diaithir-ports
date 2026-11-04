package net.diaithir.druidry.ars.api.spell.wrapped_caster;

import net.diaithir.druidry.ars.api.spell.SpellContext;

public class EmptyCaster implements IWrappedCaster{
    @Override
    public SpellContext.CasterType getCasterType() {
        return SpellContext.CasterType.OTHER;
    }

    @Override
    public void expendMana(int totalCost) {

    }
}
