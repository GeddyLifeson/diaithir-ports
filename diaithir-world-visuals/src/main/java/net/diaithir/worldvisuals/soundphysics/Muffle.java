package net.diaithir.worldvisuals.soundphysics;

/** Pure maths, kept apart from the config so it can be tested without the game. */
public final class Muffle {
    private Muffle() {}

    /** Volume factor for n solid blocks between source and listener. */
    public static float factor(int solid, double perBlock, int max) {
        return (float) Math.pow(perBlock, Math.min(solid, max));
    }
}
