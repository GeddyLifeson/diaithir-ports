package immersive_melodies.client.sound;

public final class MusicSuppression {
    public static final long TICK_REFRESH_MS = 10_000;

    private static long suppressedUntil;

    public static void suppress() {
        suppressedUntil = Math.max(suppressedUntil, System.currentTimeMillis() + TICK_REFRESH_MS);
    }

    public static boolean isActive() {
        return System.currentTimeMillis() < suppressedUntil;
    }
}
