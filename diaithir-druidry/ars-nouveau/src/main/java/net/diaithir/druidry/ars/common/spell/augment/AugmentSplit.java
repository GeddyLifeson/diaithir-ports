package net.diaithir.druidry.ars.common.spell.augment;

import net.diaithir.druidry.ars.api.spell.AbstractAugment;
import net.diaithir.druidry.ars.api.spell.SpellTier;
import net.diaithir.druidry.ars.common.lib.GlyphLib;

public class AugmentSplit extends AbstractAugment {
    public static AugmentSplit INSTANCE = new AugmentSplit();

    private AugmentSplit() {
        super(GlyphLib.AugmentSplitID, "Split");
    }

    @Override
    public int getDefaultManaCost() {
        return 20;
    }

    @Override
    public SpellTier defaultTier() {
        return SpellTier.THREE;
    }

    @Override
    public String getBookDescription() {
        return "Causes multiple projectiles to be cast at once. Each projectile applies a set of effects.";
    }
}
