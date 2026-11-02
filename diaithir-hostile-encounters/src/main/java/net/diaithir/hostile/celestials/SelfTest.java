package net.diaithir.hostile.celestials;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/** Runs on server start when -Dlunarselftest=true, then stops the server. Checks the forecast, forcing an event and the event data. */
public final class SelfTest {
    private static int fails;

    private static void check(boolean ok, String what) {
        if (!ok) fails++;
        Lunar.LOG.info("SELFTEST {} {}", ok ? "ok  " : "FAIL", what);
    }

    @SubscribeEvent
    public static void run(ServerStartedEvent e) {
        if (!Boolean.getBoolean("lunarselftest")) return;
        ServerLevel lvl = e.getServer().getLevel(Level.OVERWORLD);
        Forecast fc = Forecast.of(lvl);
        fc.update(lvl);
        List<Forecast.Entry> up = fc.upcoming(Forecast.dayOf(lvl), 1000);
        check(Lunar.events().size() == 8, "8 event files loaded (7 moons + default): " + Lunar.events().size());
        check(!up.isEmpty(), "forecast has events: " + up.size());
        check(up.stream().allMatch(en -> Lunar.events().containsKey(en.event())), "every forecast entry names a loaded event");
        for (int i = 1; i < up.size(); i++) check(up.get(i).day() - up.get(i - 1).day() > Lunar.MIN_BETWEEN, "gap " + i + " above minimum");
        Forecast.Entry first = up.get(0);
        fc.recompute(lvl);
        check(fc.upcoming(Forecast.dayOf(lvl), 1000).get(0).equals(first), "recompute is deterministic for the same seed");
        fc.force(lvl, "diaithirhostile:blood_moon");
        check(Forecast.night(lvl), "force moves the clock to night");
        check(fc.current(lvl).id.equals("diaithirhostile:blood_moon") || lvl.isRaining(), "forced blood moon is active tonight");
        check(Lunar.get("diaithirhostile:blood_moon").horde.cap > 0 && Lunar.get("diaithirhostile:blue_moon").xp == 2.0, "event values match the upstream defaults");
        Lunar.LOG.info("SELFTEST done, {} failures", fails);
        e.getServer().halt(false);
    }

    private SelfTest() {}
}
