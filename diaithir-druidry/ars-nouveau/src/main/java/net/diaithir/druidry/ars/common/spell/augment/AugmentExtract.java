package net.diaithir.druidry.ars.common.spell.augment;

import net.diaithir.druidry.ars.api.spell.AbstractAugment;
import net.diaithir.druidry.ars.api.spell.SpellTier;
import net.diaithir.druidry.ars.common.lib.GlyphLib;

public class AugmentExtract extends AbstractAugment {
    public static AugmentExtract INSTANCE = new AugmentExtract();

    private AugmentExtract() {
        super(GlyphLib.AugmentExtractID, "Extract");
    }

    @Override
    public int getDefaultManaCost() {
        return 30;
    }

    @Override
    public SpellTier defaultTier() {
        return SpellTier.TWO;
    }

    @Override
    public String getBookDescription() {
        return "Applies a silk-touch effect to Break and causes Explosion to not destroy blocks that drop. Cannot be combined with Fortune.";
    }
}
