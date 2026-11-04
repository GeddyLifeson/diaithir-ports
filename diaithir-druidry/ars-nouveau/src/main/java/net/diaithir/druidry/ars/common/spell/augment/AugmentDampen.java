package net.diaithir.druidry.ars.common.spell.augment;

import net.diaithir.druidry.ars.api.spell.AbstractAugment;
import net.diaithir.druidry.ars.api.spell.AbstractSpellPart;
import net.diaithir.druidry.ars.api.spell.SpellStats;
import net.diaithir.druidry.ars.api.spell.SpellTier;
import net.diaithir.druidry.ars.common.lib.GlyphLib;

public class AugmentDampen extends AbstractAugment {
    public static AugmentDampen INSTANCE = new AugmentDampen();

    private AugmentDampen() {
        super(GlyphLib.AugmentDampenID, "Dampen");
    }

    @Override
    public int getDefaultManaCost() {
        return 0;
    }

    @Override
    public SpellTier defaultTier() {
        return SpellTier.TWO;
    }

    @Override
    public String getBookDescription() {
        return "Decreases the power of most spells.";
    }

    @Override
    public SpellStats.Builder applyModifiers(SpellStats.Builder builder, AbstractSpellPart spellPart) {
        builder.addAmplification(-1.0);
        return super.applyModifiers(builder, spellPart);
    }
}
