package net.diaithir.druidry.ars.common.familiars;

import net.diaithir.druidry.ars.api.familiar.AbstractFamiliarHolder;
import net.diaithir.druidry.ars.api.familiar.IFamiliar;
import net.diaithir.druidry.ars.common.entity.AmethystGolem;
import net.diaithir.druidry.ars.setup.registry.ModEntities;
import net.diaithir.druidry.ars.common.entity.familiar.FamiliarAmethystGolem;
import net.diaithir.druidry.ars.common.lib.LibEntityNames;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.function.Predicate;

public class AmethystFamiliarHolder extends AbstractFamiliarHolder {
    public AmethystFamiliarHolder() {
        super(LibEntityNames.FAMILIAR_AMETHYST_GOLEM, (e) -> e instanceof AmethystGolem);
    }

    public AmethystFamiliarHolder(Identifier id, Predicate<Entity> isConversionEntity) {
        super(id, isConversionEntity);
    }

    @Override
    public IFamiliar getSummonEntity(Level world, CompoundTag tag) {
        FamiliarAmethystGolem golem = new FamiliarAmethystGolem(ModEntities.FAMILIAR_AMETHYST_GOLEM.get(), world);
        golem.setTagData(tag);
        return golem;
    }


    @Override
    public String getBookName() {
        return "Amethyst Golem";
    }

    @Override
    public String getBookDescription() {
        return "Reduces knockback taken by half and knocks back any attackers. Giving the golem an Amethyst Shard will grant 3 minutes of Shielding.";
    }
}
