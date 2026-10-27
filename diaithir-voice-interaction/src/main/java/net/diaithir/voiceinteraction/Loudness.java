package net.diaithir.voiceinteraction;

/** Level of a block of 16 bit samples in dB below full scale; silence is -96. */
public final class Loudness {
    private Loudness() {}

    public static float db(short[] pcm) {
        if (pcm == null || pcm.length == 0) return -96f;
        double sum = 0;
        for (short s : pcm) sum += (double) s * s;
        double rms = Math.sqrt(sum / pcm.length);
        return rms <= 0 ? -96f : (float) Math.max(-96.0, 20.0 * Math.log10(rms / 32768.0));
    }
}
