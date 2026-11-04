package net.diaithir.druidry.ars.common.familiars;

import net.diaithir.druidry.ars.api.familiar.AbstractFamiliarHolder;
import net.diaithir.druidry.ars.api.familiar.IFamiliar;
import net.diaithir.druidry.ars.common.entity.EntityWixie;
import net.diaithir.druidry.ars.setup.registry.ModEntities;
import net.diaithir.druidry.ars.common.entity.familiar.FamiliarWixie;
import net.diaithir.druidry.ars.common.lib.LibEntityNames;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class WixieFamiliarHolder extends AbstractFamiliarHolder {
    public WixieFamiliarHolder() {
        super(LibEntityNames.FAMILIAR_WIXIE, (e) -> e instanceof EntityWixie);
    }

    @Override
    public IFamiliar getSummonEntity(Level world, CompoundTag tag) {
        FamiliarWixie wixie = new FamiliarWixie(ModEntities.ENTITY_FAMILIAR_WIXIE.get(), world);
        wixie.setTagData(tag);
        return wixie;
    }

    @Override
    public String getBookName() {
        return "Wixie";
    }

    @Override
    public String getBookDescription() {
        return "Wixies will increase the duration of potions that you apply, and wixies will accept basic potion reagents in exchange for applying the tier 1 potion result. Additionally, the Wixie will apply harmful potions to enemies during combat. Obtained by performing the Ritual of Binding near a Wixie.";
    }
}
