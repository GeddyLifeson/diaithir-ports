package net.diaithir.linkedstorage;

/** Three dye slots, 16 colours each: 4096 channels. Pure maths, no game types. */
public final class Frequency {
    public static final int CHANNELS = 16 * 16 * 16;

    private Frequency() {}

    public static int pack(int a, int b, int c) {
        return ((a & 15) << 8) | ((b & 15) << 4) | (c & 15);
    }

    public static int slot(int packed, int i) {
        return (packed >> (8 - 4 * i)) & 15;
    }

    /** Sets dye slot i (0..2) to colour 0..15 and returns the new packed frequency. */
    public static int with(int packed, int i, int colour) {
        int shift = 8 - 4 * i;
        return (packed & ~(15 << shift)) | ((colour & 15) << shift);
    }

    /** Steps dye slot i to the next colour, wrapping. */
    public static int cycle(int packed, int i) {
        return with(packed, i, (slot(packed, i) + 1) & 15);
    }
}
