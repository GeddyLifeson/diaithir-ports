package org.diaithir.mega;

import static org.diaithir.mega.gantry.GantryAssembly.Kind.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import org.diaithir.mega.gantry.GantryAssembly;
import org.diaithir.mega.gantry.GantryAssembly.*;
import org.junit.jupiter.api.Test;

class GantryTest {
    // facing 1 = +x. A shaft run along x with a carriage at the end facing +x (a shaft pulls in a carriage facing away from it), rotation axis x, payload ahead.
    private static Grid world() {
        Map<Pos, Kind> k = new HashMap<>();
        Map<Pos, Integer> f = new HashMap<>();
        for (int x = 0; x < 5; x++) { k.put(new Pos(x, 0, 0), SHAFT); f.put(new Pos(x, 0, 0), 1); }
        k.put(new Pos(5, 0, 0), CARRIAGE); f.put(new Pos(5, 0, 0), 1);   // faces +x, away from the shaft
        k.put(new Pos(6, 0, 0), OTHER);   // payload ahead of the carriage
        k.put(new Pos(9, 0, 0), SHAFT); f.put(new Pos(9, 0, 0), 1);       // detached shaft
        return GantryAssembly.of(p -> k.getOrDefault(p, AIR), p -> f.getOrDefault(p, 0), p -> 0);
    }

    @Test
    void shaftChainAndCarriageAreOneAssembly() {
        var set = GantryAssembly.collect(world(), new Pos(0, 0, 0), 100);
        assertNotNull(set);
        assertEquals(7, set.size(), "5 shafts, the carriage and its payload");
        assertFalse(set.contains(new Pos(9, 0, 0)), "detached shaft stays behind");
    }

    @Test
    void sizeCapRefusesRunawayAssembly() {
        assertNull(GantryAssembly.collect(world(), new Pos(0, 0, 0), 3));
    }

    @Test
    void carriageFromEitherEndFindsSameSet() {
        assertEquals(GantryAssembly.collect(world(), new Pos(0, 0, 0), 100), GantryAssembly.collect(world(), new Pos(5, 0, 0), 100));
    }
}
