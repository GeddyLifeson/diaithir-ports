package net.diaithir.yards;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class YardTest {
    private static YardDef loco() {
        return new YardDef("steam_loco", YardDef.Site.GROUND, List.of(), "loco",
                List.of(new YardDef.Stage("frame", List.of(new YardDef.Slot("bogie", 2)), 40, "g1"),
                        new YardDef.Stage("power", List.of(new YardDef.Slot("boiler", 1)), 100, "g2"),
                        new YardDef.Stage("cab", List.of(new YardDef.Slot("cab", 1)), 40, "g3")), 1.0);
    }

    @Test
    void buildsAndSums() {
        BuildState b = BuildState.start(loco());
        PartDef bogie = PartDef.of("bogie", "bogie", 300).hp(50);
        PartDef boiler = PartDef.of("boiler", "boiler", 4000).power(900_000).hp(80);
        PartDef cab = PartDef.of("cab", "cab", 200).hp(20);
        assertEquals(BuildState.InstallResult.WRONG_STAGE, b.install(boiler, 5, 9999));
        assertEquals(BuildState.InstallResult.OK, b.install(bogie, 5, 0));
        assertFalse(b.advance(100, 1));
        assertEquals(BuildState.InstallResult.OK, b.install(bogie, 5, 0));
        assertEquals(BuildState.InstallResult.NO_SLOT, b.install(bogie, 5, 0));
        assertTrue(b.advance(40, 1));
        assertEquals(BuildState.InstallResult.NEEDS_CRANE, b.install(boiler, 5, 0));
        assertEquals(BuildState.InstallResult.AGE_LOCKED, b.install(boiler.age(7), 5, 9999));
        assertEquals(BuildState.InstallResult.OK, b.install(boiler, 5, 5000));
        assertFalse(b.advance(50, 1));
        assertTrue(b.advance(50, 2.0), "crane doubles work");
        assertEquals(BuildState.InstallResult.OK, b.install(cab, 5, 0));
        assertTrue(b.advance(40, 1));
        assertTrue(b.complete());
        Stats s = b.stats();
        assertEquals(4800, s.massKg());
        assertEquals(900_000, s.powerW());
        assertEquals(20, s.hp());
        Object[] out = new Object[1];
        YardRegistry.register(loco());
        YardRegistry.rollout("steam_loco", (w, p, d, st, bs) -> out[0] = st);
        assertSame(out[0] = YardRegistry.finish("steam_loco", null, null, b), out[0]);
        assertNotNull(out[0]);
    }
}
