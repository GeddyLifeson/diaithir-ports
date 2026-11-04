package net.diaithir.druidry.ars.common.spell.augment;

import net.diaithir.druidry.ars.api.spell.AbstractAugment;
import net.diaithir.druidry.ars.api.spell.AbstractSpellPart;
import net.diaithir.druidry.ars.api.spell.SpellStats;
import net.diaithir.druidry.ars.api.spell.SpellTier;
import net.diaithir.druidry.ars.common.lib.GlyphLib;

public class AugmentExtendTime extends AbstractAugment {
    public static AugmentExtendTime INSTANCE = new AugmentExtendTime();

    private AugmentExtendTime() {
        super(GlyphLib.AugmentExtendTimeID, "Extend Time");
    }

    @Override
    public int getDefaultManaCost() {
        return 10;
    }

    @Override
    public SpellTier defaultTier() {
        return SpellTier.TWO;
    }

    @Override
    public SpellStats.Builder applyModifiers(SpellStats.Builder builder, AbstractSpellPart spellPart) {
        builder.addDurationModifier(1.0);
        return super.applyModifiers(builder, spellPart);
    }

    @Override
    public String getBookDescription() {
        return "Extends the time that spells last, including buffs, fangs, and summons";
    }
}
