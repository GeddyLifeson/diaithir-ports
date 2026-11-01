package net.diaithir.voiceinteraction;

/** The numbers behind who notices a voice. Pure maths so it can be tested without a game. */
public final class Hearing {
    private Hearing() {}

    /**
     * How far a voice carries in blocks. Speech at or above {@code loudDb} carries the full {@code maxRadius};
     * quieter speech carries less, down to a quarter of it at the threshold. Whispering carries a third as far.
     * Returns 0 when the voice is below {@code thresholdDb}.
     */
    public static double radius(float levelDb, boolean whisper, double thresholdDb, double loudDb, double maxRadius) {
        if (levelDb < thresholdDb) return 0;
        double t = loudDb <= thresholdDb ? 1.0 : Math.min(1.0, (levelDb - thresholdDb) / (loudDb - thresholdDb));
        double r = maxRadius * (0.25 + 0.75 * t);
        return whisper ? r / 3.0 : r;
    }

    /** Allows one event per {@code intervalTicks} for each speaker. */
    public static final class Throttle {
        private long next = Long.MIN_VALUE;

        public boolean allow(long gameTime, int intervalTicks) {
            if (gameTime < next) return false;
            next = gameTime + intervalTicks;
            return true;
        }
    }

    /** What a creature does on hearing a voice. */
    public enum Reaction {
        /** Turns its head toward the speaker. */
        LOOK,
        /** Takes a few steps away from the speaker. */
        FLEE,
        /** Ignores speech. */
        NONE;

        public static Reaction parse(String s) {
            try {
                return valueOf(s.trim().toUpperCase(java.util.Locale.ROOT));
            } catch (IllegalArgumentException e) {
                return NONE;
            }
        }
    }

    /** Parses lines such as "minecraft:wolf=look" into a reaction; returns null for a malformed line. */
    public static java.util.Map.Entry<String, Reaction> parseRule(String line) {
        int eq = line.indexOf('=');
        if (eq <= 0) return null;
        String id = line.substring(0, eq).trim();
        if (id.isEmpty() || !id.contains(":")) return null;
        Reaction r = Reaction.parse(line.substring(eq + 1));
        return new java.util.AbstractMap.SimpleEntry<>(id, r);
    }
}
