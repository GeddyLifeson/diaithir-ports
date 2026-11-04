package net.diaithir.druidry.ars.common.familiars;

import net.diaithir.druidry.ars.api.familiar.AbstractFamiliarHolder;
import net.diaithir.druidry.ars.api.familiar.IFamiliar;
import net.diaithir.druidry.ars.common.entity.EntityDrygmy;
import net.diaithir.druidry.ars.setup.registry.ModEntities;
import net.diaithir.druidry.ars.common.entity.familiar.FamiliarDrygmy;
import net.diaithir.druidry.ars.common.lib.LibEntityNames;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class DrygmyFamiliarHolder extends AbstractFamiliarHolder {
    public DrygmyFamiliarHolder() {
        super(LibEntityNames.FAMILIAR_DRYGMY, (e) -> e instanceof EntityDrygmy);
    }

    @Override
    public IFamiliar getSummonEntity(Level world, CompoundTag tag) {
        FamiliarDrygmy familiarDrygmy = new FamiliarDrygmy(ModEntities.ENTITY_FAMILIAR_DRYGMY.get(), world);
        familiarDrygmy.setTagData(tag);
        return familiarDrygmy;
    }

    @Override
    public String getBookName() {
        return "Drygmy";
    }

    @Override
    public String getBookDescription() {
        return "A Drygmy familiar will increase the damage of Earth spells by 2, and has a chance to increase the amount of looting when slaying enemies. Obtained by performing the Ritual of Binding near a Drygmy.";
    }
}
