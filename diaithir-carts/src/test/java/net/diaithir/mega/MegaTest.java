package net.diaithir.mega;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStreamReader;
import java.util.List;
import org.junit.jupiter.api.Test;

class MegaTest {
    private static List<MegaSpec> specs() {
        return MegaSpec.load(new InputStreamReader(MegaTest.class.getResourceAsStream("/data/diaithirmega/catalog.json")));
    }

    private static MegaSpec spec(String name) {
        return specs().stream().filter(s -> s.id().equals("megamachines:" + name)).findFirst().orElseThrow();
    }

    @Test
    void catalogRatesMatchPhysics() {
        for (MegaSpec s : specs()) if (s.digM3s() > 0) assertEquals(s.digM3s(), s.maxRate(), s.digM3s() * 0.25, s.id());
    }

    @Test
    void digIsTickSlicedAndDepletes() {
        MegaSpec bwe = spec("bucket_wheel_excavator");
        Dig dig = new Dig(bwe);
        Dig.Deposit d = new Dig.Deposit(100);
        int total = 0;
        for (int t = 0; t < 20 * 10; t++) total += dig.tick(bwe.powerMW() * 1e6, 1000, d);   // 10 s at full power
        assertEquals(bwe.maxRate() * 10, total, 1.0);
        assertEquals(100 - total, d.left(), 1e-9);
        assertTrue(d.fraction() < 1);
    }

    @Test
    void hopperAndDepositLimitOutput() {
        MegaSpec bwe = spec("bucket_wheel_excavator");
        Dig dig = new Dig(bwe);
        Dig.Deposit d = new Dig.Deposit(5);
        assertEquals(0, dig.tick(bwe.powerMW() * 1e6, 0, d), "full hopper digs nothing");
        int total = 0;
        for (int t = 0; t < 400; t++) total += dig.tick(bwe.powerMW() * 1e6, 1000, d);
        assertEquals(5, total, "cannot dig more than the deposit holds");
        assertEquals(0, d.left());
    }

    @Test
    void lessPowerDigsSlower() {
        MegaSpec dl = spec("walking_dragline");
        Dig a = new Dig(dl), b = new Dig(dl);
        Dig.Deposit da = new Dig.Deposit(1e6), db = new Dig.Deposit(1e6);
        int full = 0, half = 0;
        for (int t = 0; t < 200; t++) { full += a.tick(dl.powerMW() * 1e6, 1000, da); half += b.tick(dl.powerMW() * 0.5e6, 1000, db); }
        assertTrue(half < full);
    }

    @Test
    void autonomyCapableFlagMatchesAgeVIIPlusAndCrewSeatsStay() {
        assertTrue(spec("autonomous_haul_fleet").autonomyCapable());
        assertFalse(spec("bucket_wheel_excavator").autonomyCapable());
        for (MegaSpec s : specs()) assertTrue(s.crew() > 0, s.id() + " keeps crew seats");
    }
}
