package net.diaithir.druidry.ars.common.ritual;

import net.diaithir.druidry.ars.ArsNouveau;
import net.diaithir.druidry.ars.api.ritual.AbstractRitual;
import net.diaithir.druidry.ars.client.particle.ParticleColor;
import net.diaithir.druidry.ars.client.particle.ParticleUtil;
import net.diaithir.druidry.ars.common.lib.RitualLib;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class RitualBreed extends AbstractRitual {
    @Override
    protected void tick() {
        if (getWorld().isClientSide()) {
            ParticleUtil.spawnRitualAreaEffect(getPos(), getWorld(), rand, getCenterColor(), 5);
        } else {
            if (getWorld().getGameTime() % 200 == 0) {
                List<Animal> animals = getWorld().getEntitiesOfClass(Animal.class, new AABB(getPos()).inflate(5));
                if (animals.size() >= 20)
                    return;
                boolean didWorkOnce = false;
                for (Animal a : animals) {
                    if (a.getAge() == 0 && a.canFallInLove()) {
                        didWorkOnce = true;
                        a.setInLove(null);
                    }
                }
                if (didWorkOnce)
                    setNeedsSource(true);
            }
        }
    }

    @Override
    public String getLangDescription() {
        return "Periodically causes nearby animals to breed if possible. This ritual requires source to operate, and will have no effect if there are twenty or more animals nearby.";
    }

    @Override
    public ParticleColor getCenterColor() {
        return new ParticleColor(100, 255, 100);
    }

    @Override
    public String getLangName() {
        return "Fertility";
    }

    @Override
    public int getSourceCost() {
        return 500;
    }

    @Override
    public Identifier getRegistryName() {
        return ArsNouveau.prefix( RitualLib.FERTILITY);
    }
}
