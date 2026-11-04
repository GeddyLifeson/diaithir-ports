package net.diaithir.druidry.ars.common.ritual;

import net.diaithir.druidry.ars.ArsNouveau;
import net.diaithir.druidry.ars.api.ritual.AbstractRitual;
import net.diaithir.druidry.ars.client.particle.ParticleColor;
import net.diaithir.druidry.ars.client.particle.ParticleUtil;
import net.diaithir.druidry.ars.common.lib.RitualLib;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.clock.WorldClock;

import java.util.Optional;

public class RitualSunrise extends AbstractRitual {

    private static final long DAY_TIME = 1000L;

    private static Optional<Holder<WorldClock>> clockOf(ServerLevel world) {
        return world.dimensionType().defaultClock();
    }

    private static long getTime(ServerLevel world) {
        Optional<Holder<WorldClock>> clock = clockOf(world);
        if (clock.isEmpty()) {
            return 0L;
        }
        return world.clockManager().getTotalTicks(clock.get());
    }

    private static void setTime(ServerLevel world, long time) {
        Optional<Holder<WorldClock>> clock = clockOf(world);
        if (clock.isPresent()) {
            // ServerClockManager syncs the new time to clients automatically
            world.clockManager().setTotalTicks(clock.get(), time);
        }
    }

    private static long getNextDaysTime(long current, long timeOfDay) {
        long base = current - Math.floorMod(current, 24000L);
        if (Math.floorMod(current, 24000L) < timeOfDay) {
            return base + timeOfDay;
        }
        return base + 24000L + timeOfDay;
    }

    @Override
    protected void tick() {
        ParticleUtil.spawnRitualSkyEffect(this, tile, rand, new ParticleColor.IntWrapper(255 + rand.nextInt(1), 255 + rand.nextInt(1), 25 + rand.nextInt(1)));
        if (getWorld() instanceof ServerLevel world) {
            // credits to Elucent for this trick
            long time = getTime(world);
            if (time % 24000 < 1000 || time % 24000 >= 12000) {
                setTime(world, time + 100);
            } else {
                //speed up since the target is reached
                incrementProgress();
            }
            if (world.getGameTime() % 20 == 0) {
                incrementProgress();
                if (getProgress() >= 18) {
                    setTime(world, getNextDaysTime(getTime(world), DAY_TIME));
                    setFinished();
                }
            }
        }

    }

    @Override
    public String getLangName() {
        return "Sunrise";
    }

    @Override
    public String getLangDescription() {
        return "Sets the time to day.";
    }

    @Override
    public ParticleColor getCenterColor() {
        return new ParticleColor(
                rand.nextInt(255),
                rand.nextInt(255),
                rand.nextInt(25));
    }

    @Override
    public Identifier getRegistryName() {
        return ArsNouveau.prefix( RitualLib.SUNRISE);
    }

}
