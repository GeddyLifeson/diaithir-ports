package net.diaithir.voice;

import net.diaithir.voice.client.Jitter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VoiceTest {
    @Test
    void adpcmRoundTripKeepsTheTone() {
        short[] in = new short[AdpcmCodec.FRAME_SAMPLES];
        for (int i = 0; i < in.length; i++) in[i] = (short) (8000 * Math.sin(2 * Math.PI * 440 * i / 16000.0));
        AdpcmCodec.State st = new AdpcmCodec.State();
        AdpcmCodec.encode(in, in.length, st); // warm up, as a second frame of a real stream would be
        byte[] enc = AdpcmCodec.encode(in, in.length, st);
        assertEquals(163, enc.length);
        short[] out = AdpcmCodec.decode(enc);
        assertEquals(in.length, out.length);
        double err = 0;
        for (int i = 0; i < in.length; i++) err += Math.pow(in[i] - out[i], 2);
        double snr = 10 * Math.log10(AdpcmCodec.rms(in, in.length) * AdpcmCodec.rms(in, in.length) * in.length / Math.max(err, 1));
        assertTrue(snr > 20, "signal to noise " + snr);
    }

    @Test
    void decodeRejectsGarbage() {
        assertNull(AdpcmCodec.decode(new byte[1]));
        assertNull(AdpcmCodec.decode(new byte[100000]));
        assertNotNull(AdpcmCodec.decode(new byte[] {0, 0, (byte) 200, 0x77}));
    }

    @Test
    void silenceIsQuietAndLoudIsLoud() {
        assertEquals(-96f, AdpcmCodec.levelDb(0));
        assertTrue(AdpcmCodec.levelDb(8000) > -13 && AdpcmCodec.levelDb(8000) < -11);
    }

    @Test
    void falloff() {
        assertEquals(1f, Spatial.gain(0, 4, 32));
        assertEquals(1f, Spatial.gain(4, 4, 32));
        assertEquals(0f, Spatial.gain(32, 4, 32));
        assertEquals(0f, Spatial.gain(100, 4, 32));
        float mid = Spatial.gain(18, 4, 32);
        assertEquals(0.25f, mid, 1e-5);
        assertTrue(Spatial.gain(10, 4, 32) > Spatial.gain(20, 4, 32));
        assertEquals(255, Spatial.quantise(1f));
        assertEquals(0, Spatial.quantise(-1f));
    }

    @Test
    void panFollowsTheListenersFacing() {
        // facing +Z (yaw 0): +X is to the left, -X to the right
        assertTrue(Spatial.pan(5, 0, 0) < -0.99f);
        assertTrue(Spatial.pan(-5, 0, 0) > 0.99f);
        assertEquals(0f, Spatial.pan(0, 5, 0), 1e-5);
        // turn to face -X (yaw 90): a source at +Z is now on the left? facing -X, +Z is to the left
        assertTrue(Spatial.pan(0, 5, 90) < -0.99f);
        assertEquals(0f, Spatial.pan(0.1, 0.1, 33f));
        float[] left = Spatial.stereo(-1f), mid = Spatial.stereo(0f);
        assertEquals(1f, left[0], 1e-5);
        assertEquals(0f, left[1], 1e-5);
        assertEquals(1.0, mid[0] * mid[0] + mid[1] * mid[1], 1e-5);
    }

    @Test
    void rateLimiterAllowsATalkerAndStopsAFlood() {
        RateLimiter r = new RateLimiter(60, 10);
        long t = 1;
        int ok = 0;
        for (int i = 0; i < 50; i++) { t += 20_000_000L; if (r.tryAcquire(t)) ok++; }
        assertEquals(50, ok);
        r = new RateLimiter(60, 10);
        ok = 0;
        for (int i = 0; i < 1000; i++) { t += 1_000_000L; if (r.tryAcquire(t)) ok++; }
        assertTrue(ok < 80, "flood let through " + ok);
    }

    @Test
    void jitterBufferPrefillsDropsAndRecovers() {
        Jitter<Integer> j = new Jitter<>(3, 6);
        j.push(1); j.push(2);
        assertNull(j.pop());
        j.push(3);
        assertEquals(1, j.pop());
        assertEquals(2, j.pop());
        assertEquals(3, j.pop());
        assertNull(j.pop());
        assertEquals(1, j.underruns);
        for (int i = 0; i < 10; i++) j.push(i);
        assertEquals(6, j.depth());
        assertEquals(4, j.dropped);
        assertEquals(4, j.pop());
    }
}
