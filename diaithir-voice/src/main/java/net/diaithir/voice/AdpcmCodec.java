package net.diaithir.voice;

/**
 * IMA ADPCM, 4 bits per sample. Every frame carries the predictor and step index it starts from, so frames decode
 * on their own. Frame layout: int16 big-endian starting predictor, one byte step index, then the packed nibbles.
 * Pure Java, no native libraries.
 */
public final class AdpcmCodec {
    public static final int SAMPLE_RATE = 16000;
    public static final int FRAME_SAMPLES = 320; // 20 ms
    public static final int HEADER = 3;

    private static final int[] STEP = {
        7, 8, 9, 10, 11, 12, 13, 14, 16, 17, 19, 21, 23, 25, 28, 31, 34, 37, 41, 45, 50, 55, 60, 66, 73, 80, 88, 97, 107, 118,
        130, 143, 157, 173, 190, 209, 230, 253, 279, 307, 337, 371, 408, 449, 494, 544, 598, 658, 724, 796, 876, 963, 1060,
        1166, 1282, 1411, 1552, 1707, 1878, 2066, 2272, 2499, 2749, 3024, 3327, 3660, 4026, 4428, 4871, 5358, 5894, 6484,
        7132, 7845, 8630, 9493, 10442, 11487, 12635, 13899, 15289, 16818, 18500, 20350, 22385, 24623, 27086, 29794, 32767};
    private static final int[] INDEX = {-1, -1, -1, -1, 2, 4, 6, 8};

    private AdpcmCodec() {}

    public static int encodedSize(int samples) {
        return HEADER + (samples + 1) / 2;
    }

    /** Encoder memory carried from one frame to the next; the frame header records it so decoding stays per frame. */
    public static final class State {
        int predictor;
        int index;
    }

    public static byte[] encode(short[] pcm, int samples) {
        return encode(pcm, samples, new State());
    }

    public static byte[] encode(short[] pcm, int samples, State state) {
        byte[] out = new byte[encodedSize(samples)];
        int predictor = state.predictor;
        int index = state.index;
        out[0] = (byte) (predictor >> 8);
        out[1] = (byte) predictor;
        out[2] = (byte) index;
        for (int i = 0; i < samples; i++) {
            int step = STEP[index];
            int diff = pcm[i] - predictor;
            int code = 0;
            if (diff < 0) { code = 8; diff = -diff; }
            int delta = step >> 3;
            if (diff >= step) { code |= 4; diff -= step; delta += step; }
            if (diff >= step >> 1) { code |= 2; diff -= step >> 1; delta += step >> 1; }
            if (diff >= step >> 2) { code |= 1; delta += step >> 2; }
            predictor = clamp(predictor + ((code & 8) != 0 ? -delta : delta));
            index = Math.max(0, Math.min(88, index + INDEX[code & 7]));
            int at = HEADER + (i >> 1);
            if ((i & 1) == 0) out[at] = (byte) code; else out[at] |= (byte) (code << 4);
        }
        state.predictor = predictor;
        state.index = index;
        return out;
    }

    /** Decodes one frame; returns null when the data is shorter than a header or longer than a sane frame. */
    public static short[] decode(byte[] data) {
        if (data == null || data.length < HEADER || data.length > encodedSize(FRAME_SAMPLES * 4)) return null;
        int samples = (data.length - HEADER) * 2;
        short[] pcm = new short[samples];
        int predictor = (short) (((data[0] & 0xFF) << 8) | (data[1] & 0xFF));
        int index = Math.max(0, Math.min(88, data[2] & 0xFF));
        for (int i = 0; i < samples; i++) {
            int b = data[HEADER + (i >> 1)];
            int code = (i & 1) == 0 ? b & 0xF : (b >> 4) & 0xF;
            int step = STEP[index];
            int delta = step >> 3;
            if ((code & 4) != 0) delta += step;
            if ((code & 2) != 0) delta += step >> 1;
            if ((code & 1) != 0) delta += step >> 2;
            predictor = clamp(predictor + ((code & 8) != 0 ? -delta : delta));
            index = Math.max(0, Math.min(88, index + INDEX[code & 7]));
            pcm[i] = (short) predictor;
        }
        return pcm;
    }

    /** Root mean square of the samples, 0 to 32768. */
    public static double rms(short[] pcm, int samples) {
        if (samples == 0) return 0;
        double sum = 0;
        for (int i = 0; i < samples; i++) sum += (double) pcm[i] * pcm[i];
        return Math.sqrt(sum / samples);
    }

    /** Level in dB relative to full scale (0 is a full-scale square wave, silence is -96). */
    public static float levelDb(double rms) {
        return rms <= 0 ? -96f : (float) Math.max(-96.0, 20.0 * Math.log10(rms / 32768.0));
    }

    private static int clamp(int v) {
        return v > 32767 ? 32767 : Math.max(v, -32768);
    }
}
