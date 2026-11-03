package net.diaithir.mega;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;
import net.diaithir.mega.cleanroom.*;
import org.junit.jupiter.api.Test;

class CleanroomTest {
    @Test
    void quarryVisitsEveryCellOnceTopDown() {
        QuarryPlan q = new QuarryPlan(10, 20, 4, 3, 70, 68);
        Set<String> seen = new HashSet<>();
        int lastY = Integer.MAX_VALUE;
        int[] c;
        while ((c = q.next()) != null) {
            assertTrue(seen.add(c[0] + "," + c[1] + "," + c[2]));
            assertTrue(c[1] <= lastY, "never goes back up");
            lastY = c[1];
            assertTrue(c[0] >= 10 && c[0] < 14 && c[2] >= 20 && c[2] < 23);
        }
        assertEquals(q.totalCells(), seen.size());
        assertEquals(36, seen.size());
        assertTrue(q.done());
    }

    @Test
    void headsCutOnlyWhatTheyCanAndWear() {
        assertFalse(DrillHead.IRON.canCut("rock"));
        assertTrue(DrillHead.CARBIDE.canCut("rock"));
        assertEquals(0.0, DrillHead.IRON.rateFactor("ore") * 0 + DrillHead.IRON.rateFactor("regolith"));
        assertTrue(DrillHead.DIAMOND.wear("rock", 1000) < DrillHead.STEEL.wear("soil", 1000) * 4);
        assertTrue(DrillHead.STEEL.wear("rock", 100) > DrillHead.STEEL.wear("soil", 100));
    }

    @Test
    void tracksPressureSpeedAndSteering() {
        // 40 t dozer, 250 kW, 4 m2 of track, 2.2 m gauge
        TrackDrive t = new TrackDrive(40_000, 250_000, 4.0, 2.2, 0.9, 0.08);
        assertEquals(98.1, t.groundPressureKPa(), 0.1);
        assertFalse(t.ground(50), "too heavy for soft soil");
        assertTrue(t.ground(150));
        assertEquals(250_000 / (0.08 * 40_000 * 9.81), t.topSpeed(), 1e-9);
        assertEquals(0.9 * 40_000 * 9.81, t.tractiveForce(0.0), 1e-6);
        assertEquals(0.0, t.yawRate(2, 2), 1e-12);
        assertEquals(2.0 / 2.2, t.yawRate(-1, 1), 1e-9);
        assertTrue(t.maxGradient() > 1.0);
    }
}
