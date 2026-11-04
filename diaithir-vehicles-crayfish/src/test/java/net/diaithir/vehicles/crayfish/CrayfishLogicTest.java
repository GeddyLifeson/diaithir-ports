package net.diaithir.vehicles.crayfish;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CrayfishLogicTest {
    @Test
    void trailerHangsHitchLengthBehindTowBar() {
        // tow bar at origin, trailer far to the south (+z), hitch 2 m behind
        Hitch.Pose p = Hitch.follow(0, 0, 0, 0, 0, 5, -2.0, 0F);
        assertEquals(0.0, p.x(), 1e-6);
        assertEquals(2.0, Math.hypot(p.x(), p.z()), 1e-6);
        assertEquals(0F, p.pitch(), 1e-6);
    }

    @Test
    void trailerFacesTheBar() {
        Hitch.Pose p = Hitch.follow(10, 0, 0, 0, 0, 0, -2.0, 0F);
        // bar is on +x: yaw = atan2(0, 10) - 90 = -90
        assertEquals(-90F, p.yaw(), 1e-4);
        assertEquals(8.0, p.x(), 1e-6);
    }

    @Test
    void pitchFollowsSlopeAndIsClamped() {
        Hitch.Pose up = Hitch.follow(0, 10, 0, 0, 0, 3, -2.0, 0F);
        assertEquals(5F, up.pitch(), 1e-4); // clamp 20 * 0.25
        Hitch.Pose flat = Hitch.follow(0, 0, 0, 0, 0, 3, -2.0, 8F);
        assertEquals(6F, flat.pitch(), 1e-4);
    }

    @Test
    void healthAndFall() {
        VehicleHealth h = new VehicleHealth(VehicleHealth.DEFAULT_MAX);
        assertEquals(0, h.destroyedStage());
        assertFalse(h.hurt(30F));
        assertTrue(h.destroyedStage() >= 3);
        assertTrue(h.repair());
        assertEquals(75F, h.health());
        assertTrue(h.hurt(80F));
        assertEquals(0F, VehicleHealth.fallDamage(3.9, -2));
        assertEquals(0F, VehicleHealth.fallDamage(10, -0.5));
        assertEquals(5F, VehicleHealth.fallDamage(10, -2));
    }

    @Test
    void repairStopsAtMax() {
        VehicleHealth h = new VehicleHealth(10F);
        assertFalse(h.repair());
        h.hurt(3F);
        assertTrue(h.repair());
        assertEquals(10F, h.health());
    }
}
