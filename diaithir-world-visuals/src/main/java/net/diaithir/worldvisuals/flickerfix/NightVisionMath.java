package net.diaithir.worldvisuals.flickerfix;

public final class NightVisionMath {
    public static final int FLICKER_WINDOW = 200;

    private NightVisionMath() {}

    public static float scale(int duration, boolean hold, int fadeTicks, boolean smooth, boolean affectInfinite, float partial) {
        if (duration == -1) return affectInfinite ? 1.0f : -1.0f;
        if (duration <= 0) return 0.0f;
        if (hold) return 1.0f;
        int n = Math.max(1, fadeTicks);
        float remaining = duration - partial;
        if (remaining >= n) return 1.0f;
        float t = Math.max(0.0f, remaining / n);
        return smooth ? t * t * (3.0f - 2.0f * t) : t;
    }

    public static boolean inFlickerWindow(int duration) {
        return duration > 0 && duration <= FLICKER_WINDOW;
    }
}
