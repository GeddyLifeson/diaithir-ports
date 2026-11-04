package net.diaithir.druidry.ars.common.spell.augment;

import net.diaithir.druidry.ars.api.spell.AbstractAugment;
import net.diaithir.druidry.ars.api.spell.SpellTier;
import net.diaithir.druidry.ars.common.lib.GlyphLib;

public class AugmentFortune extends AbstractAugment {
    public static AugmentFortune INSTANCE = new AugmentFortune();

    private AugmentFortune() {
        super(GlyphLib.AugmentFortuneID, "Luck");
    }

    @Override
    public int getDefaultManaCost() {
        return 80;
    }

    @Override
    public SpellTier defaultTier() {
        return SpellTier.TWO;
    }

    @Override
    public String getBookDescription() {
        return "Increases the drop chance from mobs killed by Damage and blocks that are destroyed by Break. Cannot be combined with Extract.";
    }
}
