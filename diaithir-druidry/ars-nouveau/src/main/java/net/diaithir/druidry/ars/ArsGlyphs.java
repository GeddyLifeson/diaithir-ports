package net.diaithir.druidry.ars;

import net.diaithir.druidry.ars.api.registry.GlyphRegistry;

/** Registers every spell part (methods, effects, augments) with the glyph registry. Generated list; deleted features are simply absent. */
public final class ArsGlyphs {
    private ArsGlyphs() {
    }

    /** Imbuement chamber recipe types and the rituals whose classes are in the port. */
    public static void registerRecipeTypesAndRituals() {
        var api = net.diaithir.druidry.ars.api.ArsNouveauAPI.getInstance();
        api.registerScryer(net.diaithir.druidry.ars.api.scrying.SingleBlockScryer.INSTANCE);
        api.registerScryer(net.diaithir.druidry.ars.api.scrying.CompoundScryer.INSTANCE);
        api.registerScryer(net.diaithir.druidry.ars.api.scrying.TagScryer.INSTANCE);
        net.diaithir.druidry.ars.api.registry.ImbuementRecipeRegistry.INSTANCE.addRecipeType(net.diaithir.druidry.ars.setup.registry.RecipeRegistry.IMBUEMENT_TYPE);
        net.diaithir.druidry.ars.api.registry.RitualRegistry.registerRitual(new net.diaithir.druidry.ars.common.ritual.RitualScrying());
    }

    public static void register() {
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.method.MethodPantomime.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.method.MethodProjectile.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.method.MethodSelf.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.method.MethodTouch.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.method.MethodUnderfoot.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.augment.AugmentAOE.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.augment.AugmentAccelerate.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.augment.AugmentAmplify.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.augment.AugmentDampen.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.augment.AugmentDecelerate.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.augment.AugmentDurationDown.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.augment.AugmentExtendTime.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.augment.AugmentExtract.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.augment.AugmentFortune.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.augment.AugmentPierce.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.augment.AugmentRandomize.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.augment.AugmentSensitive.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.augment.AugmentSplit.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectBlink.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectBounce.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectBreak.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectBurst.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectColdSnap.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectConjureWater.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectCraft.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectCrush.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectCut.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectDelay.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectDispel.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectEnderChest.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectEvaporate.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectExchange.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectExplosion.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectFangs.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectFell.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectFirework.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectFlare.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectFreeze.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectGlide.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectGravity.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectGrow.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectHarm.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectHarvest.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectHeal.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectHex.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectIgnite.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectInfuse.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectIntangible.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectInteract.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectInvisibility.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectKnockback.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectLaunch.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectLeap.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectLight.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectLightning.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectLinger.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectName.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectOrbit.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectPhantomBlock.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectPickup.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectPrestidigitation.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectPlaceBlock.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectPull.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectRedstone.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectReset.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectRewind.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectRotate.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectRune.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectSenseMagic.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectSlowfall.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectSmelt.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectSnare.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectToss.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectWall.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectWindshear.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectWither.INSTANCE);
        GlyphRegistry.registerSpell(net.diaithir.druidry.ars.common.spell.effect.EffectWololo.INSTANCE);
    }
}
