package net.diaithir.forcefield;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class GeometryTest {
    @Test
    void cubeShellCount() {
        // outer 13^3 minus inner 11^3
        assertEquals(13 * 13 * 13 - 11 * 11 * 11, Geometry.count(Geometry.Shape.CUBE, 6, 1, 0, -1));
        assertEquals(Geometry.count(Geometry.Shape.CUBE, 6, 1, 0, -1) * 3, Geometry.shell(Geometry.Shape.CUBE, 6, 1, 0, -1).length);
    }

    @Test
    void shellIsHollow() {
        assertTrue(Geometry.inShell(Geometry.Shape.CUBE, 6, 0, 0, 6, 1, 0, -1));
        assertFalse(Geometry.inShell(Geometry.Shape.CUBE, 5, 0, 0, 6, 1, 0, -1));
        assertFalse(Geometry.inShell(Geometry.Shape.SPHERE, 0, 0, 0, 6, 1, 0, -1));
        assertTrue(Geometry.inShell(Geometry.Shape.SPHERE, 6, 0, 0, 6, 1, 0, -1));
    }

    @Test
    void capShrinksRadius() {
        int r = Geometry.fit(Geometry.Shape.CUBE, 32, 1, 0, -1, 1000);
        assertTrue(r < 32);
        assertTrue(Geometry.count(Geometry.Shape.CUBE, r, 1, 0, -1) <= 1000);
        assertTrue(Geometry.count(Geometry.Shape.CUBE, r + 1, 1, 0, -1) > 1000);
        assertEquals(1, Geometry.fit(Geometry.Shape.CUBE, 10, 1, 0, -1, 1));
    }

    @Test
    void wallIsInFrontOnly() {
        // facing north: normal (0,-1)
        assertTrue(Geometry.inShell(Geometry.Shape.WALL, 0, 0, -Geometry.WALL_GAP, 4, 1, 0, -1));
        assertFalse(Geometry.inShell(Geometry.Shape.WALL, 0, 0, Geometry.WALL_GAP, 4, 1, 0, -1));
        assertFalse(Geometry.inShell(Geometry.Shape.WALL, 5, 0, -Geometry.WALL_GAP, 4, 1, 0, -1));
        assertEquals(9 * 9, Geometry.count(Geometry.Shape.WALL, 4, 1, 0, -1));
    }

    @Test
    void ejectLandsOutside() {
        for (Geometry.Shape s : Geometry.Shape.values()) {
            double[] to = Geometry.eject(s, 3, 0, -1, 6, 2, 0, -1);
            assertFalse(Geometry.inVolume(s, to[0], to[1], to[2], 6, 2, 0, -1), s.name());
        }
        double[] c = Geometry.eject(Geometry.Shape.CUBE, 6, 0, 0, 6, 1, 0, -1);
        assertTrue(c[0] > 7);
    }
}
