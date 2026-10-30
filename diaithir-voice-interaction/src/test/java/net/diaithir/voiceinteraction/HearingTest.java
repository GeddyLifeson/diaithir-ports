package net.diaithir.voiceinteraction;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HearingTest {
    @Test
    void quietVoicesAreNotNoticedAndLoudOnesCarryFar() {
        assertEquals(0, Hearing.radius(-60f, false, -45, -20, 16));
        assertEquals(16, Hearing.radius(-20f, false, -45, -20, 16), 1e-9);
        assertEquals(16, Hearing.radius(-5f, false, -45, -20, 16), 1e-9);
        assertEquals(4, Hearing.radius(-45f, false, -45, -20, 16), 1e-9);
        assertTrue(Hearing.radius(-30f, false, -45, -20, 16) > Hearing.radius(-40f, false, -45, -20, 16));
    }

    @Test
    void whisperCarriesAThirdAsFar() {
        assertEquals(16 / 3.0, Hearing.radius(-20f, true, -45, -20, 16), 1e-9);
    }

    @Test
    void throttleLimitsPerSpeaker() {
        Hearing.Throttle t = new Hearing.Throttle();
        assertTrue(t.allow(100, 10));
        assertFalse(t.allow(105, 10));
        assertTrue(t.allow(110, 10));
    }

    @Test
    void rulesParse() {
        var e = Hearing.parseRule("minecraft:wolf=look");
        assertEquals("minecraft:wolf", e.getKey());
        assertEquals(Hearing.Reaction.LOOK, e.getValue());
        assertEquals(Hearing.Reaction.FLEE, Hearing.parseRule(" minecraft:fox = Flee ").getValue());
        assertEquals(Hearing.Reaction.NONE, Hearing.parseRule("minecraft:cow=dance").getValue());
        assertNull(Hearing.parseRule("wolf"));
        assertNull(Hearing.parseRule("=look"));
    }

    @Test
    void loudnessOfSamples() {
        assertEquals(-96f, Loudness.db(new short[0]));
        assertEquals(-96f, Loudness.db(new short[10]));
        short[] full = new short[100];
        java.util.Arrays.fill(full, (short) 32767);
        assertEquals(0f, Loudness.db(full), 0.01);
        short[] half = new short[100];
        java.util.Arrays.fill(half, (short) 16384);
        assertEquals(-6.02f, Loudness.db(half), 0.05);
    }
}
