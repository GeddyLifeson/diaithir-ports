package net.diaithir.druidry.ars.common.familiars;

import net.diaithir.druidry.ars.api.familiar.AbstractFamiliarHolder;
import net.diaithir.druidry.ars.api.familiar.IFamiliar;
import net.diaithir.druidry.ars.common.entity.Starbuncle;
import net.diaithir.druidry.ars.common.lib.LibEntityNames;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class JabberwogFamiliarHolder extends AbstractFamiliarHolder {

    public JabberwogFamiliarHolder() {
        super(LibEntityNames.FAMILIAR_JABBERWOG, (e) -> e instanceof Starbuncle);
    }

    @Override
    public IFamiliar getSummonEntity(Level world, CompoundTag tag) {
        return null;
    }

    @Override
    public String getBookName() {
        return "Jabberwog";
    }

    @Override
    public String getBookDescription() {
        return "";
    }
}
