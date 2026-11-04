package net.diaithir.druidry.ars.common.familiars;

import net.diaithir.druidry.ars.api.familiar.AbstractFamiliarHolder;
import net.diaithir.druidry.ars.api.familiar.IFamiliar;
import net.diaithir.druidry.ars.common.entity.Starbuncle;
import net.diaithir.druidry.ars.common.entity.familiar.FamiliarStarbuncle;
import net.diaithir.druidry.ars.common.lib.LibEntityNames;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class StarbuncleFamiliarHolder extends AbstractFamiliarHolder {

    private static EntityType<FamiliarStarbuncle> familiarType;

    public StarbuncleFamiliarHolder() {
        super(LibEntityNames.FAMILIAR_STARBUNCLE, (e) -> e instanceof Starbuncle);
    }

    @SuppressWarnings("unchecked")
    private static EntityType<FamiliarStarbuncle> getFamiliarType() {
        if (familiarType == null) {
            for (Identifier id : BuiltInRegistries.ENTITY_TYPE.keySet()) {
                if (id.getPath().equals(LibEntityNames.FAMILIAR_STARBUNCLE)) {
                    familiarType = (EntityType<FamiliarStarbuncle>) (EntityType<?>) BuiltInRegistries.ENTITY_TYPE.getValue(id);
                    break;
                }
            }
        }
        return familiarType;
    }

    @Override
    public IFamiliar getSummonEntity(Level world, CompoundTag tag) {
        FamiliarStarbuncle familiarStarbuncle = new FamiliarStarbuncle(getFamiliarType(), world);
        familiarStarbuncle.setTagData(tag);
        return familiarStarbuncle;
    }

    @Override
    public String getBookName() {
        return "Starbuncle";
    }

    @Override
    public String getBookDescription() {
        return "A Starbuncle familiar that will grant you Speed 2. Additionally, using a Golden Nugget on the starbuncle will consume it and grant the owner a short duration of Scrying for Gold Ore. Obtained by performing the Ritual of Binding near a Starbuncle.";
    }
}
