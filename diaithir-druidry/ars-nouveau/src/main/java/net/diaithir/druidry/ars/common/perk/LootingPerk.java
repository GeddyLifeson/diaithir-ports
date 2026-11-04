package net.diaithir.druidry.ars.common.perk;

import net.diaithir.druidry.ars.ArsNouveau;
import net.diaithir.druidry.ars.api.perk.Perk;
import net.minecraft.resources.Identifier;

public class LootingPerk extends Perk {

    public static final LootingPerk INSTANCE = new LootingPerk(ArsNouveau.prefix( "thread_drygmy"));

    public LootingPerk(Identifier key) {
        super(key);
    }

    @Override
    public String getLangName() {
        return "The Drygmy";
    }

    @Override
    public String getLangDescription() {
        return "Grants an additional stack of looting.";
    }
}
