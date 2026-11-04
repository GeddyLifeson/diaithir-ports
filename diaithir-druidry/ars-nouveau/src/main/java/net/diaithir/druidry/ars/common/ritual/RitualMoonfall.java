package net.diaithir.druidry.ars.common.ritual;

import net.diaithir.druidry.ars.ArsNouveau;
import net.diaithir.druidry.ars.api.ritual.AbstractRitual;
import net.diaithir.druidry.ars.client.particle.ParticleColor;
import net.diaithir.druidry.ars.client.particle.ParticleUtil;
import net.diaithir.druidry.ars.common.lib.RitualLib;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;

public class RitualMoonfall extends AbstractRitual {
    private static final long NIGHT_TIME = 13000L;

    private static long currentDayTime(ServerLevel world) {
        return world.getDefaultClockTime();
    }

    private static void changeDayTime(ServerLevel world, long time) {
        // the clock manager synchronises the new time to the clients itself
        world.dimensionType().defaultClock().ifPresent(clock -> world.clockManager().setTotalTicks(clock, time));
    }

    private static long nextNightTime(ServerLevel world) {
        long t = currentDayTime(world);
        long target = t - Math.floorMod(t, 24000L) + NIGHT_TIME;
        if (target <= t) {
            target += 24000L;
        }
        return target;
    }

    @Override
    protected void tick() {
        ParticleUtil.spawnRitualSkyEffect(this, tile, rand, new ParticleColor.IntWrapper(50 + rand.nextInt(50), 50 + rand.nextInt(50), 200 + rand.nextInt(55)));
        if (getWorld() instanceof ServerLevel world) {
            // credits to Elucent for this trick
            long dayTime = currentDayTime(world);
            if (dayTime % 24000 < 13000 && dayTime % 24000 >= 0) {
                changeDayTime(world, dayTime + 100);
            } else {
                //speed up since the target is reached
                incrementProgress();
            }
            if (world.getGameTime() % 20 == 0) {
                incrementProgress();
                if (getProgress() >= 18) {
                    changeDayTime(world, nextNightTime(world));
                    setFinished();
                }
            }
        }

    }

    @Override
    public String getLangName() {
        return "Moonfall";
    }

    @Override
    public String getLangDescription() {
        return "Sets the time to night.";
    }

    @Override
    public Identifier getRegistryName() {
        return ArsNouveau.prefix( RitualLib.MOONFALL);
    }

    @Override
    public ParticleColor getCenterColor() {
        return new ParticleColor(
                rand.nextInt(25),
                rand.nextInt(25),
                rand.nextInt(255));
    }
}
