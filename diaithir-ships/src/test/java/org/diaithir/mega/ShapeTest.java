package org.diaithir.mega;

import static org.junit.jupiter.api.Assertions.*;

import org.diaithir.mega.shape.Shape;
import org.junit.jupiter.api.Test;

class ShapeTest {
    @Test
    void boxVolumeAndShell() {
        var f = Shape.BOX.fit(0, 0, 0, 5, 4, 6, 0, 0, 0);
        assertEquals(5 * 4 * 6, f.volume(true));
        assertEquals(5 * 4 * 6 - 3 * 2 * 4, f.volume(false));
    }

    @Test
    void sphereIsRoundAndSymmetric() {
        var f = Shape.SPHERE.fit(0, 0, 0, 21, 21, 21, 0, 0, 0);
        double ideal = 4.0 / 3 * Math.PI * 10.5 * 10.5 * 10.5;
        assertEquals(ideal, f.volume(true), ideal * 0.15);
        assertTrue(f.isInside(0, 0, 0));
        assertFalse(f.isInside(11, 0, 0));
        assertEquals(f.isInside(7, 3, 2), f.isInside(-7, -3, -2));
    }

    @Test
    void domesAreHalves() {
        var s = Shape.SPHERE.fit(0, 0, 0, 20, 20, 20, 0, 0, 0).volume(true);
        var t = Shape.TOP_DOME.fit(0, 0, 0, 20, 20, 20, 0, 0, 0).volume(true);
        var b = Shape.BOTTOM_DOME.fit(0, 0, 0, 20, 20, 20, 0, 0, 0).volume(true);
        assertTrue(t < s && b < s && t + b >= s, "halves cover the sphere");
    }

    @Test
    void tunnelCylinderPrismConeTorusHaveVolume() {
        for (Shape sh : Shape.values()) assertTrue(sh.fit(0, 0, 0, 15, 15, 15, 0, 0, 0).volume(true) > 0, sh.name());
        var cyl = Shape.CAPPED_CYLINDER.fit(0, 0, 0, 12, 10, 12, 0, 0, 0);
        double ideal = Math.PI * 36 * 10;
        assertEquals(ideal, cyl.volume(true), ideal * 0.15);
        assertTrue(Shape.CONE.fit(0, 0, 0, 15, 15, 15, 0, 0, 0).volume(true) < Shape.CYLINDER.fit(0, 0, 0, 15, 15, 15, 0, 0, 0).volume(true));
    }
}
