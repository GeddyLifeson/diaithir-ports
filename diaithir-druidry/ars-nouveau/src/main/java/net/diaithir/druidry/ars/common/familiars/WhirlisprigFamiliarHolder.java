package net.diaithir.druidry.ars.common.familiars;

import net.diaithir.druidry.ars.api.familiar.AbstractFamiliarHolder;
import net.diaithir.druidry.ars.api.familiar.IFamiliar;
import net.diaithir.druidry.ars.setup.registry.ModEntities;
import net.diaithir.druidry.ars.common.entity.Whirlisprig;
import net.diaithir.druidry.ars.common.entity.familiar.FamiliarWhirlisprig;
import net.diaithir.druidry.ars.common.lib.LibEntityNames;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class WhirlisprigFamiliarHolder extends AbstractFamiliarHolder {
    public WhirlisprigFamiliarHolder() {
        super(LibEntityNames.FAMILIAR_WHIRLISPRIG, (e) -> e instanceof Whirlisprig);
    }

    @Override
    public IFamiliar getSummonEntity(Level world, CompoundTag tag) {
        FamiliarWhirlisprig whirlisprig = new FamiliarWhirlisprig(ModEntities.ENTITY_FAMILIAR_SYLPH.get(), world);
        whirlisprig.setTagData(tag);
        return whirlisprig;
    }

    @Override
    public String getBookName() {
        return "Whirlisprig";
    }

    @Override
    public String getBookDescription() {
        return "Whirlisprigs will reduce the cost of Elemental Earth glyphs by half, and grants bonus saturation when consuming food. Obtained by performing the Ritual of Binding near a Whirlisprig.";
    }
}
