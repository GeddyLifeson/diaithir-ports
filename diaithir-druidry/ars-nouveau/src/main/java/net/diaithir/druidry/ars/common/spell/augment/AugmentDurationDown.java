package net.diaithir.druidry.ars.common.spell.augment;

import net.diaithir.druidry.ars.api.spell.AbstractAugment;
import net.diaithir.druidry.ars.api.spell.AbstractSpellPart;
import net.diaithir.druidry.ars.api.spell.SpellStats;
import net.diaithir.druidry.ars.api.spell.SpellTier;
import net.diaithir.druidry.ars.common.lib.GlyphLib;

public class AugmentDurationDown extends AbstractAugment {
    public static AugmentDurationDown INSTANCE = new AugmentDurationDown();

    private AugmentDurationDown() {
        super(GlyphLib.AugmentReduceTime, "Reduce Time");
    }

    @Override
    public SpellTier defaultTier() {
        return SpellTier.TWO;
    }


    @Override
    public int getDefaultManaCost() {
        return 15;
    }

    @Override
    public SpellStats.Builder applyModifiers(SpellStats.Builder builder, AbstractSpellPart spellPart) {
        builder.addDurationModifier(-1.0d);
        return super.applyModifiers(builder, spellPart);
    }

    @Override
    public String getBookDescription() {
        return "Reduces the duration of spells like potion effects, delay, redstone signal, and others.";
    }
}
