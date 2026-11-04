package net.diaithir.vehicles.tracks;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TrackDriveTest {
    private static TrackDrive.Out run(TrackDrive d, double t, double s, int ticks, boolean fuel) {
        TrackDrive.Out o = null;
        for (int i = 0; i < ticks; i++) o = d.step(t, s, 0.05, fuel);
        return o;
    }

    @Test
    void straightReachesTopSpeedWithNoTurn() {
        TrackDrive d = new TrackDrive(TrackDrive.Spec.LIGHT_TANK);
        TrackDrive.Out o = run(d, 1, 0, 400, true);
        assertEquals(10.0, o.speed(), 0.01);
        assertEquals(0.0, o.yawRate(), 1e-9);
    }

    @Test
    void pivotTurnsOnTheSpotAndRightSteerYawsRight() {
        TrackDrive d = new TrackDrive(TrackDrive.Spec.LIGHT_TANK);
        TrackDrive.Out o = run(d, 0, 1, 400, true);
        assertEquals(0.0, o.speed(), 1e-9);
        assertTrue(o.yawRate() > 0);
        assertEquals(2 * 5.0 / 2.4, o.yawRate(), 1e-6); // pivot factor 0.5 of 10 m/s, each way
    }

    @Test
    void steeringWhileDrivingSlowsTheInnerTrack() {
        TrackDrive d = new TrackDrive(TrackDrive.Spec.LIGHT_TANK);
        run(d, 1, 0.5, 400, true);
        assertTrue(d.leftSpeed() > d.rightSpeed());
        assertEquals(10.0, d.leftSpeed(), 0.01);
    }

    @Test
    void dryTankCoastsToAStop() {
        TrackDrive d = new TrackDrive(TrackDrive.Spec.LIGHT_TANK);
        run(d, 1, 0, 400, true);
        TrackDrive.Out o = run(d, 1, 0, 1, false);
        assertTrue(o.speed() < 10.0);
        assertEquals(0.0, o.powerW());
        o = run(d, 1, 0, 400, false);
        assertEquals(0.0, o.speed(), 1e-9);
    }

    @Test
    void weakEngineAcceleratesSlower() {
        TrackDrive.Spec weak = new TrackDrive.Spec(12_000, 10.0, 2.5, 5.0, 2.4, 40_000, 0.35, 0.06, 0.35, 0.5);
        TrackDrive strong = new TrackDrive(TrackDrive.Spec.LIGHT_TANK), slow = new TrackDrive(weak);
        double vs = run(strong, 1, 0, 40, true).speed(), vw = run(slow, 1, 0, 40, true).speed();
        assertTrue(vw < vs, vw + " vs " + vs);
        assertTrue(vw > 0);
    }

    @Test
    void powerNeverExceedsTheEngine() {
        TrackDrive d = new TrackDrive(TrackDrive.Spec.LIGHT_TANK);
        for (int i = 0; i < 400; i++) assertTrue(d.step(1, 0.3, 0.05, true).powerW() <= 300_000 + 1);
    }

    @Test
    void reverseIsNegative() {
        TrackDrive d = new TrackDrive(TrackDrive.Spec.LIGHT_TANK);
        assertTrue(run(d, -1, 0, 100, true).speed() < 0);
    }
}
