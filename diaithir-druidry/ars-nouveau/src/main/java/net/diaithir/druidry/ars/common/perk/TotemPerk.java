package net.diaithir.druidry.ars.common.perk;

import net.diaithir.druidry.ars.ArsNouveau;
import net.diaithir.druidry.ars.api.perk.Perk;
import net.diaithir.druidry.ars.api.perk.PerkSlot;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public class TotemPerk extends Perk {
    public static TotemPerk INSTANCE = new TotemPerk(ArsNouveau.prefix( "thread_undying"));

    public TotemPerk(Identifier key) {
        super(key);
    }

    @Override
    public String getLangDescription() {
        return "Once every time you sleep, you will nullify death a single time as if holding a Totem of the Undying. Requires a tier 3 slot.";
    }

    @Override
    public PerkSlot minimumSlot() {
        return PerkSlot.THREE;
    }

    @Override
    public String getLangName() {
        return "Undying";
    }

    @Override
    public @Nullable CompoundTag getInitTag() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("isActive", true);
        return tag;
    }
}
