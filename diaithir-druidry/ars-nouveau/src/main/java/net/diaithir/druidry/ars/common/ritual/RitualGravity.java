package net.diaithir.druidry.ars.common.ritual;

import net.diaithir.druidry.ars.ArsNouveau;
import net.diaithir.druidry.ars.api.ritual.RangeEffectRitual;
import net.diaithir.druidry.ars.common.lib.RitualLib;
import net.diaithir.druidry.ars.setup.registry.ModPotions;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;

public class RitualGravity extends RangeEffectRitual {
    public static int renewInterval = 20;
    public static int renewThreshold = 10 * 20;

    @Override
    public Holder<MobEffect> getEffect() {
        return ModPotions.GRAVITY_EFFECT;
    }

    @Override
    public int getRange() {
        return 60;
    }

    @Override
    public int getDuration() {
        return 60 * 20;
    }

    @Override
    public int getSourceCost() {
        return 200;
    }

    @Override
    public Identifier getRegistryName() {
        return ArsNouveau.prefix( RitualLib.GRAVITY);
    }

    @Override
    public String getLangDescription() {
        return "Grants nearby players the Gravity effect, forcing them to the ground for a short time. If the player is nearby, this ritual will refresh their gravity debuff. Each time this ritual grants or refreshes gravity, it will expend source from nearby jars.";
    }

    @Override
    public String getLangName() {
        return "Gravity";
    }
}
