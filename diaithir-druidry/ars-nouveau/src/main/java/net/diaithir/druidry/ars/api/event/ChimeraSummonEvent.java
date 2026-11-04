package net.diaithir.druidry.ars.api.event;

import net.diaithir.druidry.ars.ArsNouveau;
import net.diaithir.druidry.ars.api.util.NBTUtil;
import net.diaithir.druidry.ars.client.particle.ParticleColor;
import net.diaithir.druidry.ars.client.particle.ParticleUtil;
import net.diaithir.druidry.ars.common.entity.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class ChimeraSummonEvent implements ITimedEvent {

    int duration;
    int phase;
    Level world;
    BlockPos pos;
    int ownerID;

    public ChimeraSummonEvent(int duration, int phase, Level world, BlockPos pos, int ownerID) {
        this.duration = duration;
        this.phase = phase;
        this.world = world;
        this.pos = pos;
        this.ownerID = ownerID;
    }

    public static ChimeraSummonEvent get(CompoundTag tag) {
        return new ChimeraSummonEvent(tag.getIntOr("duration", 0),
                tag.getIntOr("phase", 0), ArsNouveau.proxy.getClientWorld(), NBTUtil.getBlockPos(tag, "loc"), -1);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Entity> EntityType<T> lookupType(String path) {
        return (EntityType<T>) BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.fromNamespaceAndPath(ArsNouveau.MODID, path));
    }

    @Override
    public void tick(boolean serverSide) {
        duration--;
        if (serverSide) {
            Entity owner = world.getEntity(ownerID);
            if (!(owner instanceof WildenChimera boss)) {
                duration = 0;
                return;
            }
            boolean summonedWilden = false;
            if (duration % 20 == 0) {
                RandomSource random = boss.getRandom();
                SummonWolf wolf = new SummonWolf(ChimeraSummonEvent.<SummonWolf>lookupType("summon_wolf"), world);

                wolf.setPos(getPos().getX(), getPos().getY(), getPos().getZ());
                wolf.isWildenSummon = true;
                wolf.ticksLeft = 600 + phase * 60;
                summon(wolf, getPos(), boss.getTarget());

                int randBound = 8 - boss.getPhase();
                if (boss.hasWings() && random.nextInt(randBound) == 0) {
                    WildenStalker stalker = new WildenStalker(ChimeraSummonEvent.<WildenStalker>lookupType("wilden_stalker"), world);
                    summon(stalker, getPos(), boss.getTarget());
                    summonedWilden = true;
                }

                if (!summonedWilden && boss.hasHorns() && random.nextInt(randBound) == 0) {
                    WildenHunter hunter = new WildenHunter(ChimeraSummonEvent.<WildenHunter>lookupType("wilden_hunter"), world);
                    summon(hunter, getPos(), boss.getTarget());
                    summonedWilden = true;
                }

                if (!summonedWilden && boss.hasSpikes() && random.nextInt(randBound) == 0) {
                    WildenGuardian guardian = new WildenGuardian(ChimeraSummonEvent.<WildenGuardian>lookupType("wilden_guardian"), world);
                    summon(guardian, getPos(), boss.getTarget());
                    summonedWilden = true;
                }
            }
        } else {
            ParticleUtil.spawnRitualAreaEffect(pos, world, world.getRandom(), ParticleColor.defaultParticleColor(), 1 + phase * 2);
        }
    }

    public void summon(Mob mob, BlockPos pos, @Nullable LivingEntity target) {
        mob.setPos(pos.getX(), pos.getY(), pos.getZ());
        mob.setTarget(target);
        mob.setAggressive(true);
        mob.level().addFreshEntity(mob);
    }

    public BlockPos getPos() {
        double spawnArea = 2.5 + phase * 2;
        return BlockPos.containing(pos.getX() + ParticleUtil.inRange(-spawnArea, spawnArea), pos.getY() + 2, pos.getZ() + ParticleUtil.inRange(-spawnArea, spawnArea));
    }

    @Override
    public boolean isExpired() {
        return duration <= 0;
    }

    @Override
    public CompoundTag serialize(CompoundTag tag) {
        ITimedEvent.super.serialize(tag);
        tag.putInt("duration", duration);
        tag.putInt("phase", phase);
        NBTUtil.storeBlockPos(tag, "loc", pos);
        return tag;
    }

    public static final String ID = "chimera";

    @Override
    public String getID() {
        return ID;
    }
}
