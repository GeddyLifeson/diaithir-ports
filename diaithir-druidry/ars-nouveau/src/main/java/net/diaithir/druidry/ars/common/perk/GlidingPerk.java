package net.diaithir.druidry.ars.common.perk;

import net.diaithir.druidry.ars.ArsNouveau;
import net.diaithir.druidry.ars.api.perk.Perk;
import net.diaithir.druidry.ars.api.perk.PerkSlot;
import net.minecraft.resources.Identifier;

public class GlidingPerk extends Perk {

    public static final GlidingPerk INSTANCE = new GlidingPerk(ArsNouveau.prefix( "thread_gliding"));

    public GlidingPerk(Identifier key) {
        super(key);
    }

    @Override
    public PerkSlot minimumSlot() {
        return PerkSlot.THREE;
    }

    @Override
    public String getLangName() {
        return "Gliding";
    }

    @Override
    public String getLangDescription() {
        return "Allows you to glide as if you are wearing an elytra. Must be equipped in a slot of at least level 3.";
    }
}
